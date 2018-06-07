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
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.util.concurrent.LinkedBlockingQueue;

import io.sece.vlc.rcvr.blocks.AreaOfInterest;


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

    int delay = 300;
    long firstTimeStamp = 0;
    int bqCounter = 0;

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

        SyncFramesProcessor syncFramesProcessor = new SyncFramesProcessor(syncBlockingQueue, getActivity());
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
    }
//    public void initTransmitterUI(View view) {
//         Uncomment to set SyncFramesProcessors Color Offset via UI
//        EditText editText = view.findViewById(R.id.etTransmitterColorValue);
//        (view.findViewById(R.id.btSetTransmitterColor)).setOnClickListener(v -> {
//            SyncFramesProcessor.gray_elimination_offset = Integer.parseInt(editText.getText().toString());
//
//        });
//    }

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
        mRgba = inputFrame.rgba();
        AreaOfInterest areaOfInterest = new AreaOfInterest(areaX, areaY, rWidth, rHeight);

        Rect currRect = areaOfInterest.getRectangle();
        circularBuffer.put(mRgba.submat(currRect).clone());


        long currDiff = (System.currentTimeMillis() - firstTimeStamp - ((bqCounter - 1) * delay));
//        System.out.println("Curr: " + currDiff);

        if(firstTimeStamp == 0){
//            for synchronization of following frames we need to store the first timestamp
            firstTimeStamp = System.currentTimeMillis();
            System.out.println("first " + firstTimeStamp);
        }else if (currDiff >= 0) {
            bqCounter++;
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
        System.out.println(Thread.currentThread().getId() + " " + System.currentTimeMillis()+" SyncFramesReceived " + bqCounter);

        try{
            syncBlockingQueue.put(circularBuffer.get());
        }catch(InterruptedException e){
            System.out.println(e);
        }
     }


}
