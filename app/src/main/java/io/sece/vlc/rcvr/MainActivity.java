package io.sece.vlc.rcvr;


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
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;

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



    /*
        Array to store amount of pixels to different colors in each frame
     */
    Integer[] colors= new Integer[6];
    final int RED = 0;
    final int GREEN = 1;
    final int BLUE = 2;
    final int YELLOW = 3;
    final int PURPLE = 4;
    final int TURQUOISE = 5;


    /*
        Variables needed for synchronized Thread Approach
        - circularBuffer contains the latest Frames in form of a matrix
        - syncBlockingQueue contains all frames matrices in periodically intervals
     */

    CircularBuffer<Mat> circularBuffer;
    LinkedBlockingQueue syncBlockingQueue;

    int delay = 50;
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


     /*
                Start Background-Thread for:
                 - Processing frames as soon as they are stored in syncBlockingQueue
     */

        SyncFramesProcessor syncFramesProcessor = new SyncFramesProcessor(syncBlockingQueue);
        Thread SyncFrameProcessorThread = new Thread(syncFramesProcessor);
        SyncFrameProcessorThread.start();

        colors[RED] = 0;
        colors[GREEN] = 0;
        colors[BLUE] = 0;
        colors[YELLOW] = 0;
        colors[PURPLE] = 0;
        colors[TURQUOISE] = 0;

        if (checkSelfPermission(Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED){
            initCamera();
        }else{
            requestPermissions(new String[] {Manifest.permission.CAMERA}, 100);
        }
        enableRectangleSelection();
        enableRectangleSizing();
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
//                    areaY = (int)event.getY();

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

        rHeight = height/12;
        rWidth = width/16;

        mRgba = new Mat(height, width, CvType.CV_8UC4);

        imgRed = new Mat(rHeight,rWidth, mRgba.type());
        imgBlue= new Mat(rHeight,rWidth, mRgba.type());
        imgGreen= new Mat(rHeight,rWidth, mRgba.type());
        imgYellow= new Mat(rHeight,rWidth, mRgba.type());
        imgPurple= new Mat(rHeight,rWidth, mRgba.type());
        imgTurquoise= new Mat(rHeight,rWidth, mRgba.type());
        imgHSV = new Mat(rHeight,rWidth, CvType.CV_8UC4);

        imgRectangleContent = new Mat(rHeight,rWidth, CvType.CV_8UC4);

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
//        Imgproc.rectangle(mRgba, new Point(mRgba.cols() / 2 - rWidth / 2, mRgba.rows() / 2 - rHeight / 2), new Point(mRgba.cols() / 2 + rWidth / 2, mRgba.rows() / 2 + rHeight / 2), new Scalar(255, 255, 255), 1);

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

    public void processFrame(Mat pFrameRGBA) throws InterruptedException{
//        System.out.println("## Processing Frame ## " + System.currentTimeMillis());
//
//        Mat imgRectangleContent = pFrameRGBA;
//
////      Write HSV-Colors from submatrix into imgHSV
//        Imgproc.cvtColor(imgRectangleContent, imgHSV, Imgproc.COLOR_BGR2HSV);
//
////      imgHSV is seperated into specific Color-Ranges and saved into Matrixes for every Color
//        Core.inRange(imgHSV, new Scalar(100, 20, 10), new Scalar(130, 255, 255), imgRed);
//        Core.inRange(imgHSV, new Scalar(0, 20, 10), new Scalar(10, 255, 255), imgBlue);
//        Core.inRange(imgHSV, new Scalar(55, 30, 15), new Scalar(65, 255, 255), imgGreen);
//        Core.inRange(imgHSV, new Scalar(80, 50, 50), new Scalar(90, 255, 255), imgYellow);
//        Core.inRange(imgHSV, new Scalar(140, 20, 10), new Scalar(160, 255, 255), imgPurple);
//        Core.inRange(imgHSV, new Scalar(27, 20, 10), new Scalar(33, 255, 255), imgTurquoise);
//
////      Count all Pixels matching specific Colors
//        colors[RED] = Core.countNonZero(imgRed);
//        colors[GREEN] = Core.countNonZero(imgGreen);
//        colors[BLUE] = Core.countNonZero(imgBlue);
//        colors[YELLOW] = Core.countNonZero(imgYellow);
//        colors[PURPLE] = Core.countNonZero(imgPurple);
//        colors[TURQUOISE] = Core.countNonZero(imgTurquoise);
//
//        /*
//            Read out the index of the color which is most likely
//         */
//        int maxIndex = 0;
//        for (int i = 0; i < colors.length; i++) {
//            if (colors[i] > colors[maxIndex]) {
//                maxIndex = i;
//            }
//        }
//
////      Regarding the experienced limits of the value of different colors we can check if all of them are off
////      values: last Average values from tests divided by 2
//        if (colors[RED] < 188 && colors[GREEN] < 217 && colors[BLUE] < 308){
//            System.out.println("OFF");
//            output += 0;
//        } else {
//            switch (maxIndex) {
//                case RED: {
//                    System.out.println("RED");
//                    output += 1;
//                    break;
//                }
//                case BLUE: {
//                    /*
//                        STOP Condition
//                     */
////                    clockThread.stopThread();
//                    System.out.println("BLUE");
////                    measureErrorRateByOrder(output);
//                    System.out.println(output.length() + " " + output);
//                    break;
//                }
//                case GREEN: {
//                     /*
//                        START Condition
//                     */
//                    System.out.println("GREEN");
//                    break;
//                }
//                case PURPLE: {
//                    System.out.println("PURPLE");
//                    break;
//                }
//            }
//        }
//
//
//        /*
//            Temporary solution to delete leading '0' in output
//            (all frames are stored currently)
//         */
//        if(output.length() > 0 && output.charAt(0) == '0'){
//            output = output.substring(1, output.length());
//        }
    }





}
