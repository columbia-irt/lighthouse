package io.sece.vlc.rcvr;




import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Range;
import android.util.Rational;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2 {
    private static final String TAG = "MainActivity";
    private CameraBridgeViewBase mOpenCvCameraView;

    /*
        Matrices used for storing images to different colors
     */

    Mat imgRed, imgBlue, imgGreen, imgYellow, imgPurple, imgTurquoise;

    /*
        Matrices used for storing images for color detection
     */
    Mat mRgba, imgHSV, imgRectangleContent;

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


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    break;
                }
                default:
                {
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        circularBuffer = new CircularBuffer(20);

        syncBlockingQueue =  new LinkedBlockingQueue<CvCameraViewFrame>();

        context = this;

     /*
                Start Background-Thread for:
                 - Processing frames as soon as they are stored in syncBlockingQueue
     */

        SyncFramesProcessor syncFramesProcessor = new SyncFramesProcessor(syncBlockingQueue, this);
        Thread SyncFrameProcessorThread = new Thread(syncFramesProcessor);
        SyncFrameProcessorThread.start();


        if (checkSelfPermission(Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED){
            initCamera();
        }else{
            requestPermissions(new String[] {Manifest.permission.CAMERA}, 100);
        }
        enableRectangleSelection();
        enableRectangleSizing();

        initTransmitterUI();

    }
    public void initTransmitterUI(){
        EditText editText = (EditText)findViewById(R.id.etTransmitterColorValue);
        ((Button)findViewById(R.id.btSetTransmitterColor)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                RequestQueue queue = Volley.newRequestQueue(context);
                String url ="http://192.168.1.102:8000/calibration";
                int[] hueValues = new int[1];
                hueValues[0] = 180;
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
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(context, "Request failed " + error, Toast.LENGTH_SHORT).show();

                            }
                        });
                queue.add(jsonObjectRequest);

            }
        });


    }
    public void initCamera() {
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.JCV);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    public void enableRectangleSelection(){
        /*
            OnTouch Listener for Changing Area to be selected
         */
        JavaCamera2View view = findViewById(R.id.JCV);
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

    public void enableRectangleSizing() {
        SeekBar seekbarSize = (SeekBar)findViewById(R.id.seekbarSize);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initCamera();
            }else{
                Toast.makeText(this, "Camera permission denied - Application closing", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initDebug();
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height)
    {
        /*
            Size of the rectangle frame for led
         */

        mRgba = new Mat(height, width, CvType.CV_8UC4);

        imgRed = new Mat(rHeight,rWidth, mRgba.type());
        imgBlue= new Mat(rHeight,rWidth, mRgba.type());
        imgGreen= new Mat(rHeight,rWidth, mRgba.type());
        imgYellow= new Mat(rHeight,rWidth, mRgba.type());
        imgPurple= new Mat(rHeight,rWidth, mRgba.type());
        imgTurquoise= new Mat(rHeight,rWidth, mRgba.type());
        imgHSV = new Mat(rHeight,rWidth, CvType.CV_8UC4);

    }

    public void onCameraViewStopped()
    {
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame)
    {
    /**
     *  This is called on a seperate Thread created by JavaCamera2View (CV) Class
     */

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


//      Draw the rectangle frame for led in entire matrix for preview
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
