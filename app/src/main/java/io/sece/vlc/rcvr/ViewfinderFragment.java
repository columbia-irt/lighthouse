package io.sece.vlc.rcvr;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.ActivityCompat;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.eventbus.Subscribe;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import io.sece.vlc.BitVector;
import io.sece.vlc.Color;
import io.sece.vlc.modem.FSK4Modem;
import io.sece.vlc.rcvr.processing.Frame;
import io.sece.vlc.rcvr.processing.Processing;
import io.sece.vlc.rcvr.camera.CameraException;
import io.sece.vlc.rcvr.camera.CameraHelper;
import io.sece.vlc.rcvr.camera.CameraSession;


// FIXME: An error dialog is shown when the orientation of the display is changed while the permission confirmation dialog is shown.

public class ViewfinderFragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = "ViewfinderFragment";
    private static final String FRAGMENT_DIALOG = "dialog";

    private static final int NO_SESSION = 0;
    private static final int SESSION_STARTING = 1;
    private static final int SESSION_RUNNING = 2;

    private int state = NO_SESSION;

    private SurfaceView surfaceView;

    private CompletableFuture<CameraSession> session;
    private ImageReader imageReader;

    private CompletableFuture<Boolean> permission;
    private CompletableFuture<Boolean> permissionWriting;
    private ViewfinderModel model;
    private GestureControl gestureControl;
    private GraphicOverlay<GraphicOverlay.Graphic> overlay;
    private RoIIndicator roi;
    private TextView rxColor;
    private TransmitterAPI trx;
    private Processing processing;

    private boolean writingActivated = false;

    private FSK4Modem modem = new FSK4Modem();

    private long timestamp_session_started = 0;
    private int amount_measurements = 1;
    private int counter_measurements = 0;

    public static float zooming = 0;

    private static Size selectFrameResolution(Size[] choices, Size target) throws CameraException {
        return selectFrameResolution(choices, target, new Size(0, 0));
    }


    private static Size selectFrameResolution(Size[] sizes, Size target, Size max) throws CameraException {
        List<Size> big = new ArrayList<>();
        List<Size> small = new ArrayList<>();
        List<Size> l = null;

        int w, h;
        for (Size o : sizes) {
            w = o.getWidth();
            h = o.getHeight();

            if (max.getWidth() > 0 && w > max.getWidth()) continue;
            if (max.getHeight() > 0 && h > max.getHeight()) continue;

            if (w >= target.getWidth() && h >= target.getHeight()) {
                Log.d(TAG, "Frame size " + w + "x" + h + " is big enough");
                big.add(o);
            } else {
                Log.d(TAG, "Frame size " + w + "x" + h + " is too small");
                small.add(o);
            }
        }

        if (big.size() > 0) l = big;
        else if (small.size() > 0) l = small;

        if (l != null)
            return Collections.min(l, (a, b) -> Long.signum(
                    (long) a.getWidth() * a.getHeight() - b.getWidth() * b.getHeight()));

        throw new CameraException("Couldn't find suitable frame size");
    }


    /**
     * Given target frame size, select the most suitable camera device and frame resolution.
     * We only select back facing cameras and pick the minimum frame size that is larger than
     * the target. The id of the selected camera and the selected frame resolution will be stored
     * in the session passed to the function in the first argument.
     */
    private void selectCameraSource(CameraSession session, Size targetSize, int format) throws CameraException {
        Log.d(TAG, "Selecting suitable camera device");

        Context c = getContext();
        if (null == c)
            throw new CameraException("Couldn't get Android context");

        CameraManager mgr = CameraHelper.getCameraManager(c);
        CameraHelper camera;
        try {
            for (String id : mgr.getCameraIdList()) {
                Log.d(TAG, "Found camera ID: " + id);
                camera = new CameraHelper(getContext(), id);

                if (!camera.backFacing) {
                    Log.d(TAG, "Skipping a camera that is not back facing");
                    continue;
                }

                Log.d(TAG, camera.toString());

                Size resolution;
                try {
                    resolution = selectFrameResolution(camera.getStreamMap().getOutputSizes(format), targetSize);
                } catch (CameraException e) {
                    Log.d(TAG, "Camera device " + id + " provides no suitable frame resolution, skipping");
                    continue;
                }
                Log.d(TAG, "Selected frame resolution " + resolution.getWidth() + "x" + resolution.getHeight());

                session.camera = camera;
                session.frameResolution = resolution;

                // FIXME: Get rid of the following hack
                session.fps = camera.maxFPS() > 60 ? 60: camera.maxFPS();
                return;
            }
        } catch (CameraAccessException e) {
            throw new CameraException(getString(R.string.camera_access_denied), e);
        } catch (NullPointerException e) {
            // This was taken from the official camera2 example app. Apparently, the API throws
            // a null pointer exception if the Camera2 API is not supported by device.
            throw new CameraException(getString(R.string.camera2_not_supported), e);
        }
        throw new CameraException("No suitable camera configuration found");
    }


    private ImageReader createImageReader(Processing processing, Size resolution) {
        ImageReader ir = ImageReader.newInstance(resolution.getWidth(), resolution.getHeight(), ImageFormat.YUV_420_888, 2);
        ir.setOnImageAvailableListener(reader -> {
            Image img = reader.acquireLatestImage();
            if (null == img) return;
            try {
                processing.submit(img);
            } finally {
                img.close();
            }
        }, new Handler(processing.getLooper()));
        return ir;
    }


    private void initRoIIndicator(GestureControl gc) {
        if (null != roi)
            overlay.remove(roi);

        if (model.roiCenter.equals(ViewfinderModel.DEFAULT_ROI_CENTER)) {
            int center = overlay.getWidth() / 2;
            model.roiCenter.x = center;
            model.roiCenter.y = center;
        }

        roi = new RoIIndicator(overlay, model.roiCenter, model.roiRadius);

        // The following code is temporarily commented, until we figure out how to rotate the
        // RoI coordinates properly since the camera image is usually rotated wrt to default
        // device orientation.

//        gc.onTap((x, y) -> {
//            Point old = model.roiCenter;
//            model.roiCenter = roi.center(new Point(x, y));
//            if (!old.equals(model.roiCenter))
//                Bus.send(new Bus.RoIEvent(overlay.normalizeBoundingBox(roi.boundingBox())));
//        });

        gc.onZoom(f -> {
            int old = model.roiRadius;
            model.roiRadius = roi.radius(Math.round(model.roiRadius * f));
            if (old != model.roiRadius)
                Bus.send(new Bus.RoIEvent(overlay.normalizeBoundingBox(roi.boundingBox())));
        });

        overlay.add(roi);
    }


    private CompletableFuture<CameraSession> start(SurfaceView surface, Size targetSize) {
        CameraSession S = new CameraSession(getContext(), model.cameraParams);

        // Make sure that we have camera permissions and and if we do, select the camera device
        // and a supported resolution.
        final CompletableFuture<Void> haveSource = requestCameraPermission()
                .thenAccept(granted -> {
                    try {
                        if (!granted)
                            throw new CameraException(getString(R.string.camera_access_denied));
                        selectCameraSource(S, targetSize, ImageFormat.YUV_420_888);

                        // Set transmission baud rate to half the camera frame rate by default.
                        if (model.getBaudRate() == model.NO_BAUD_RATE) {
                            model.setBaudRate(S.fps / 2);
                            EditText t = getView().findViewById(R.id.txFPS);
                            t.setText(Integer.toString(model.getBaudRate()));
                        }
                    } catch (CameraException e) {
                        throw new CompletionException(e);
                    }
                });

        // Once the target frame size is known, create the image reader object
        CompletableFuture f1 = haveSource.thenApply(v -> {
            processing = new Processing(overlay.normalizeBoundingBox(roi.boundingBox()), model.getBaudRate(), modem);
            processing.start();
            imageReader = createImageReader(processing, S.frameResolution);
            S.setSurfaces(Arrays.asList(surface.getHolder().getSurface(), imageReader.getSurface()));
            return null;
        });

        CompletableFuture<CameraDevice> device = haveSource.thenCompose(v -> S.camera.open());

        CompletableFuture f2 = device.thenApply(dev -> {
            try {
                S.setDevice(dev);
                return S.start();
            } catch (CameraException | CameraAccessException e) {
                throw new CompletionException(e);
            }
        });

        CompletableFuture f21 = f2.thenAccept(v -> {
            View view = getView();
            if (null == view)
                throw new CompletionException(new Exception("Couldn't get current view"));

            SeekBar s = view.findViewById(R.id.exposureSlider);
            if (null == s)
                throw new CompletionException(new Exception("Exposure slider view not found"));

            new ExposureControl(S, s);
        });

        CompletableFuture f22 = f2.thenAccept(v -> {
            View view = getView();
            if (null == view)
                throw new CompletionException(new Exception("Couldn't get current view"));

            SeekBar s = view.findViewById(R.id.zoomSlider);
            if (null == s)
                throw new CompletionException(new Exception("Zoom slider view not found"));

            new ZoomControl(S, s);
        });

        CompletableFuture f3 = f2.whenComplete((cs, ex) -> {
            if (null == ex) return;
            // Make sure to clean up the opened camera device on exception.
            if (S.cameraDevice != null) {
                S.cameraDevice.close();
                S.cameraDevice = null;
            }
        });

        return CompletableFuture.allOf(f1, f21, f22, f3).thenApply(v -> S);
    }


    private static void stop(CameraSession S) {
        S.stop();
        S.closeDevice();
    }


    private CompletableFuture<Boolean> requestCameraPermission() {
        if (null != permission) return permission;

        // If we have camera permission already, complete the future immediately and return.

        Context ctx = getContext();
        if (null == ctx) {
            permission.completeExceptionally(new CameraException("Couldn't obtain Android context"));
            return permission;
        }

        permission = new CompletableFuture<>();
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            permission.complete(true);
            return permission;
        }

        // If we don't have camera permission yet, request it, and return the future that will
        // complete after the corresponding callback has fired.

        Log.d(TAG, "Requesting camera permission...");
        requestPermissions(new String[]{Manifest.permission.CAMERA}, ReceiverApp.REQUEST_CAMERA_PERMISSION);
        return permission;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == ReceiverApp.REQUEST_CAMERA_PERMISSION) {
            if (null == permission) {
                Log.w(TAG, "Received unexpected permission result callback");
                return;
            }

            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                permission.complete(false);
            } else {
                permission.complete(true);
            }
        } else if (requestCode == ReceiverApp.REQUEST_WRITING_PERMISSION) {
            if (null == permissionWriting) {
                Log.w(TAG, "Received unexpected permissionWriting result callback");
                return;
            }

            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                permissionWriting.complete(false);
            } else {
                permissionWriting.complete(true);
            }
        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private CompletableFuture<Boolean> requestWritingPermission() {
        if (null != permissionWriting) return permissionWriting;

        // If we have writing permission already, complete the future immediately and return.

        Context ctx = getContext();
        if (null == ctx) {
            permissionWriting.completeExceptionally(new Exception("Couldn't obtain Android context"));
            return permissionWriting;
        }

        permissionWriting = new CompletableFuture<>();
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            permissionWriting.complete(true);
            return permissionWriting;
        }

        // If we don't have camera permission yet, request it, and return the future that will
        // complete after the corresponding callback has fired.

        Log.d(TAG, "Requesting writing permission...");
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, ReceiverApp.REQUEST_WRITING_PERMISSION);
        return permissionWriting;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ViewModelProviders.of(this).get(ViewfinderModel.class);
        if (model.receiver == null)
            model.receiver = new Receiver(new FSK4Modem());
        requestWritingPermission();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_viewfinder, container, false);
    }


    @Override
    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {
        overlay = view.findViewById(R.id.overlay);
        gestureControl = new GestureControl(overlay);
        surfaceView = view.findViewById(R.id.surface);
        rxColor = view.findViewById(R.id.rxColor);
    }


    @Override
    public void onResume() {
        super.onResume();
        Bus.subscribe(this);

        initTrxControl(getView().findViewById(R.id.txFPS), getView().findViewById(R.id.txButton));

        Button btWrite =((Button)getView().findViewById(R.id.writeButton));
        btWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!writingActivated){
                    writingActivated=true;
                    btWrite.setText("Stop");
                }else{
                    writingActivated=false;
                    btWrite.setText("Write");
                }
                Bus.send(new Bus.WriteEvent(writingActivated));
            }
        });

        model.receiver.start();

        onSurfaceReady(surfaceView, surface -> {
            if (state != NO_SESSION) return;
            state = SESSION_STARTING;

            // The following function must be called after the size of the graphic overlay view
            // is known. It uses the overlay's dimension to place the RoI indicator in the center
            initRoIIndicator(gestureControl);

            session = start(surfaceView, model.targetResolution);
            session.thenAccept(v -> state = SESSION_RUNNING);
            session.exceptionally(ex -> {
                state = NO_SESSION;
                session = null;
                Log.e(TAG, "Error while creating camera session", ex);
                ConfirmationDialog d = ConfirmationDialog.newInstance(ex.getCause().getMessage(), false);
                d.show(getChildFragmentManager(), FRAGMENT_DIALOG);
                d.completed.whenComplete((v, t) -> {
                    Activity a = getActivity();
                    if (null != a) a.finish();
                });
                return null;
            });
        });
    }


    @Override
    public void onPause() {
        super.onPause();
        Bus.unsubscribe(this);

        model.receiver.stop();

        if (null != imageReader) {
            imageReader.setOnImageAvailableListener(null, null);
            imageReader.close();
            imageReader = null;
        }

        if (null != processing) {
            try {
                processing.shutdown();
            } catch(InterruptedException e) {
                Log.w(TAG, "Interrupted while shutting down processing pipeline", e);
            }
            processing = null;
        }

        if (null != trx) {
            trx.cancelPending();
            trx = null;
        }

        if (state == SESSION_RUNNING) {
            session.whenComplete((s, ex) -> {
                if (s != null) stop(s);
                session = null;
                state = NO_SESSION;
            });
        }
    }


    private void initTrxControl(EditText fpsText, Button button) {
        if (model.transmissionID != null) {
            button.setText("Stop");
            fpsText.setEnabled(false);
        } else {
            button.setText("Start");
            fpsText.setEnabled(true);
        }
        fpsText.setText(Integer.toString(model.getBaudRate()));

        fpsText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    model.setBaudRate(Integer.parseInt(s.toString()));
                } catch(NumberFormatException e) { }
            }

            @Override
            public void afterTextChanged(Editable s) { }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            });

        trx = new TransmitterAPI(getContext(),"http://192.168.1.102:8000/");
        button.setOnClickListener(v -> {
            final int fps = Integer.parseInt(fpsText.getText().toString());

            button.setText(R.string.working);
            button.setEnabled(false);
            fpsText.setEnabled(false);

            if (model.transmissionID != null) {
                CompletableFuture<String> f = trx.stop(model.transmissionID);
                f.whenComplete((response, ex) -> {
                    model.transmissionID = null;
                    button.setText("Start");
                    button.setEnabled(true);
                    fpsText.setEnabled(true);
                    if (null != ex)
                        Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                });
            } else {
                CompletableFuture<String> f = trx.transmit(fps, 60 * 60 * 24, "fsk4");
                f.whenComplete((obj, ex) -> {
                    if (obj != null) {
                        model.transmissionID = obj;
                        button.setText("Stop");
                        fpsText.setEnabled(false);
                        if(timestamp_session_started == 0){
                            timestamp_session_started = System.nanoTime();
                        }
                    } else {
                        button.setText("Start");
                        resetTimeMeasurement();
                        fpsText.setEnabled(true);
                        Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    button.setEnabled(true);
                });
            }
        });



//        Spinner spinner = (Spinner) getActivity().findViewById(R.id.spModulation);
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
//                R.array.modulations_array, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(adapter);
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String[] array = getResources().getStringArray(R.array.modulations_array);
//                //                receiverClass.setModulator();
//                System.out.println(array[position]);
//                switch (array[position]) {
//                    case "ASK2":
//                        break;
//                    case "ASK4":
//                        break;
//                    case "ASK8":
//                        break;
//                    case "FSK2":
//                        receiverClass.setModulator(new FSK2Modulator());
//                        break;
//                    case "FSK4":
//                        receiverClass.setModulator(new FSK4Modulator());
//                        break;
//                    case "FSK8":
//                        receiverClass.setModulator(new FSK8Modulator());
//                        break;
//                    case "OOK":
//                        break;
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
    }


    private interface SurfaceReadyListener {
        void onSurfaceReady(Surface surface);
    }


    private void onSurfaceReady(SurfaceView view, SurfaceReadyListener listener) {
        view.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                listener.onSurfaceReady(holder.getSurface());
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) { }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }
        });
    }


    private class ExposureControl {
        private Slider slider;

        ExposureControl(CameraSession session, SeekBar widget) {
            Log.d(TAG, String.format("Camera AE compensation range %d-%d, step: %d/%d",
                    session.camera.aeRange.getLower(), session.camera.aeRange.getUpper(),
                    session.camera.aeStep.getNumerator(), session.camera.aeStep.getDenominator()));

            slider = new Slider(session.camera.aeRange.getLower(), session.camera.aeRange.getUpper(), session.params.aeCompensation(), widget);

            slider.onChange(v -> {
                session.params.aeCompensation(v);
                session.sync();
            });
        }
    }


    private class ZoomControl {
        private static final int SCALE_FACTOR = 1000;
        private Slider slider;

        ZoomControl(CameraSession session, SeekBar widget) {
            Log.d(TAG, String.format("Camera max zoom: %f", session.camera.maxDigitalZoom));

            slider = new Slider(SCALE_FACTOR, (int)session.camera.maxDigitalZoom * SCALE_FACTOR, (int)session.params.zoom() * SCALE_FACTOR, widget);

            slider.onChange(v -> {
                session.params.zoom((float)v / (float)SCALE_FACTOR);
                zooming = session.params.zoom();
                System.out.println(zooming);
                session.sync();
            });
        }
    }


    @Subscribe
    public void onProcessingResult(Processing.Result ev) {
        Color c = ev.frame.getColorAttr(Frame.HUE);
        c = modem.detect(c);
        rxColor.setBackgroundColor(android.graphics.Color.rgb(c.red, c.green, c.blue));
        rxColor.setText("RX: " + ev.frame.getColorAttr(Frame.HUE).hue);
    }


//    public void setManualExposureSettingUI() {
//        Button btManualExposure = (Button) getView().findViewById(R.id.btManualExposure);
//        btManualExposure.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Range<Long> exposureTimeRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
//                System.out.println("Range " + exposureTimeRange.toString());
//
//                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
//                builder.setTitle("Manual Exposure - Choose between " + exposureTimeRange.getLower() + " and " + exposureTimeRange.getUpper());
//                final EditText input = new EditText(activity);
//                input.setInputType(InputType.TYPE_CLASS_NUMBER);
//                builder.setView(input);
//                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        long curr_manual = Integer.parseInt(input.getText().toString());
//                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
//                                CaptureRequest.CONTROL_AE_MODE_OFF);
//                        mPreviewRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, (curr_manual));
//                        try {
//                            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
//                        } catch (CameraAccessException e) {
//                            e.printStackTrace();
//                        }
//                        Toast.makeText(activity, "Set to: " + curr_manual, Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//                builder.show();
//
//
//            }
//        });
//    }


    private static int hammingDistance(byte[] a, byte[] b) {
        if (a.length != b.length)
            throw new IllegalArgumentException("a.length != b.length");

        int rv = 0;
        for (int i = 0; i < a.length; i++)
            rv += Integer.bitCount((a[i] & 0xff) ^ (b[i] & 0xff));
        return rv;
    }


    @Subscribe
    public void onTransferCompleted(Bus.TransferCompleted ev) {
        String msg = "Failure";
        if (hammingDistance(ev.data, BitVector.DEFAULT_DATA.data) == 0){
            msg = "Success";
            counter_measurements++;
            if(amount_measurements == counter_measurements){
                long diff = System.nanoTime() - timestamp_session_started;
                msg = "Success " + diff/1000000;
            }
        }

        if(counter_measurements == amount_measurements){
            ConfirmationDialog d = ConfirmationDialog.newInstance(msg, false);
            d.show(getChildFragmentManager(), FRAGMENT_DIALOG);
            d.completed.whenComplete((v, t) -> {
                model.receiver.reset();
                model.receiver.start();
                resetTimeMeasurement();
            });
        }else{
            model.receiver.reset();
            model.receiver.start();
            Toast.makeText(getContext(), "Completed", Toast.LENGTH_SHORT).show();
        }



    }

    public void resetTimeMeasurement(){
        counter_measurements = 0;
        timestamp_session_started = 0;
    }
}
