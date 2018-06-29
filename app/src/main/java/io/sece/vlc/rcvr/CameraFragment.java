package io.sece.vlc.rcvr;

import org.json.JSONObject;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.Manifest;
import android.support.v4.app.Fragment;
import android.support.v4.app.ActivityCompat;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.util.concurrent.LinkedBlockingQueue;

import io.sece.vlc.FSK2Modulator;
import io.sece.vlc.FSK4Modulator;
import io.sece.vlc.FSK8Modulator;
import io.sece.vlc.rcvr.modules.AreaOfInterest;


public class CameraFragment extends Fragment implements CvCameraViewListener2, ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = "CameraFragment";
    private static final String FRAGMENT_DIALOG = "dialog";

    private CameraBridgeViewBase cameraView = null;

    Mat mRgba;

    int rHeight = 40;
    int rWidth = 40;
    int areaX = 200;
    int areaY = 200;

    public static Context context;
    static int currHueValue = -1;

    /*
        Variables needed for synchronized Thread Approach
        - circularBuffer contains the latest Frames in form of a matrix
        - syncBlockingQueue contains all frames matrices in periodically intervals
     */

    CircularBuffer<Mat> circularBuffer;
    LinkedBlockingQueue syncBlockingQueue;

    public static long timeToStartSynchronized;
    long firstTimeStamp = 0;
    int bqCounter = 0;

    ReceiverClass receiverClass;

    private void requestCameraPermission() {
        Log.d(TAG, "Requesting camera permission...");
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, Receiver.REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Receiver.REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialog.newInstance(getString(R.string.request_permission))
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            } else {
                initCamera();
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {
        enableRectangleSelection(view);
        enableRectangleSizing(view);
        initTransmitterUI(view);

        cameraView = view.findViewById(R.id.JCV);
        initCamera();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

        circularBuffer = new CircularBuffer<>(20);
        syncBlockingQueue =  new LinkedBlockingQueue<CvCameraViewFrame>();
        context = getActivity();



        receiverClass = new ReceiverClass(new FSK2Modulator());

        SyncFramesProcessor syncFramesProcessor = new SyncFramesProcessor(syncBlockingQueue, getActivity(), receiverClass);
        Thread SyncFrameProcessorThread = new Thread(syncFramesProcessor);
        SyncFrameProcessorThread.start();

    }

    public void initTransmitterUI(View view) {
        EditText editText = view.findViewById(R.id.etTransmitterColorValue);
        (view.findViewById(R.id.btSetTransmitterColor)).setOnClickListener(v -> {

            RequestQueue queue = Volley.newRequestQueue(context);
            String url ="http://192.168.1.102:8000/calibration";
            JSONObject jsonObject = new JSONObject();
            currHueValue = Integer.parseInt(editText.getText().toString());
            try{
                jsonObject = new JSONObject("{duration:15,hueValue:[" + editText.getText() + "]}");
            }catch(Exception e) {
                System.out.println(e);
            }
            System.out.println("Folgendes:" + jsonObject.toString());

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Toast.makeText(context, "Response received: " + response.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }, error -> Toast.makeText(context, "Request failed " + error, Toast.LENGTH_SHORT).show());
            queue.add(jsonObjectRequest);

        });

        Spinner spinner = (Spinner) getActivity().findViewById(R.id.spModulation);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.modulations_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] array = getResources().getStringArray(R.array.modulations_array);
//                receiverClass.setModulator();
                System.out.println(array[position]);
                switch (array[position]){
                    case "ASK2": break;
                    case "ASK4": break;
                    case "ASK8": break;
                    case "FSK2":
                        receiverClass.setModulator(new FSK2Modulator());
                        break;
                    case "FSK4":
                        receiverClass.setModulator(new FSK4Modulator());
                        break;
                    case "FSK8":
                        receiverClass.setModulator(new FSK8Modulator());
                        break;
                    case "OOK": break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void initCamera() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
        }

        cameraView.setCvCameraViewListener(this);
        cameraView.setVisibility(SurfaceView.VISIBLE)
        ;
    }

    public void enableRectangleSelection(View v) {
        /*
            OnTouch Listener for Changing Area to be selected
         */
        JavaCamera2View view = v.findViewById(R.id.JCV);
        view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getActionMasked();
                if(action == MotionEvent.ACTION_DOWN) {
                    DisplayMetrics dm = getResources().getDisplayMetrics();
                    areaX = (int)(event.getX() / dm.widthPixels  * 640);
                    areaY = (int)(event.getY()/ dm.heightPixels  * 480);

                    System.out.println("X " + dm.widthPixels);
                    System.out.println("Y " + dm.heightPixels);
                    System.out.println("CLICKED DOWN " + areaX + " " + areaY);
                }
                return true;
            }
        });
    }

    public void enableRectangleSizing(View view) {
        SeekBar seekbarSize = view.findViewById(R.id.seekbarSize);
        seekbarSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rWidth = progress + 8;
                rHeight = progress + 8;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (cameraView != null)
            cameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        cameraView.enableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (cameraView != null)
            cameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Frame a = (Frame)inputFrame;
        a.timestamp = System.currentTimeMillis();

        mRgba = inputFrame.rgba();
        AreaOfInterest areaOfInterest = new AreaOfInterest(areaX, areaY, rWidth, rHeight);

        Rect currRect = areaOfInterest.getRectangle();
        circularBuffer.put(mRgba.submat(currRect).clone());


        if(receiverClass.isTransmissionStarted()){

            if(timeToStartSynchronized < System.currentTimeMillis()){
                long currDiff = (System.currentTimeMillis() - firstTimeStamp - ((bqCounter - 1) * receiverClass.getDelay()));
//                System.out.println("Curr: " + currDiff);

                if(firstTimeStamp == 0){
//            for synchronization of following frames we need to store the first timestamp
                    firstTimeStamp = System.currentTimeMillis();
                    System.out.println("first " + firstTimeStamp);
                }else if (currDiff >= 0) {
                    bqCounter++;
                    addToBlockingQueue();
                }
            }

        }else{
            /*
                to determine the best time for starting the transmission
                we have to store all incoming frames and timestamps
                as fast as we can
             */

            addToBlockingQueue();

        }

//     Draw the rectangle frame for led in entire matrix for preview
        Imgproc.rectangle(mRgba, new Point(currRect.x, currRect.y), new Point(currRect.x + currRect.width, currRect.y + currRect.height), new Scalar(255, 255, 255), 1);

        return mRgba;
    }

    public void addToBlockingQueue() {
        /*
            Limitation of BlockingQueue Size ?
            -> BlockingQueue put() waiting if necessary for space to become available
         */
//        System.out.println(Thread.currentThread().getId() + " " + System.currentTimeMillis()+" SyncFramesReceived " + bqCounter);

        try{
            syncBlockingQueue.put(circularBuffer.get());
        }catch(InterruptedException e){
            System.out.println(e);
        }
     }


}
