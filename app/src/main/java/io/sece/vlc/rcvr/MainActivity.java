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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.ArrayList;

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

    String output = "";

    /*
        Variables needed for synchronized Thread Approach
        - currentFrameFGBA contains the latest preview frame matrix
        - synchronizedFrames contains all frames matrices in periodically intervals
     */
    Mat currentFrameRGBA;
    ArrayList<Mat> synchronizedFrames;

    /*
        ReceivingClockThread stores in defined intervals the latestFrame to synchronizedFrames
        FrameProcessingThread is a workerthread waiting for frames stored in synchronizedFrames to process and remove from it
     */
    ReceivingClockThread clockThread;
    FrameProcessingThread frameProcessingThread;

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

        synchronizedFrames = new ArrayList<Mat>();

        colors[RED] = 0;
        colors[GREEN] = 0;
        colors[BLUE] = 0;
        colors[YELLOW] = 0;
        colors[PURPLE] = 0;
        colors[TURQUOISE] = 0;

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.JCV);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

//        mOpenCvCameraView.enableFpsMeter();

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

        /*
            Start Background-Threads for:
             - regular Interval Frame Receiving
             - Processing as soon as they are stored in synchronizedFrames
         */

        int fps = 10;
        clockThread = new ReceivingClockThread();
        clockThread.delay = 1000 / fps;
        clockThread.start();

        frameProcessingThread = new FrameProcessingThread();
        frameProcessingThread.start();
    }

    public void onCameraViewStopped()
    {
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame)
    {
//      Store a copy of the current frame matrix into currentFrameRGBA variable
        mRgba = inputFrame.rgba();
        currentFrameRGBA = mRgba.clone();

//      Draw the rectangle frame for led in entire matrix for preview
        Imgproc.rectangle(mRgba, new Point(mRgba.cols() / 2 - rWidth / 2, mRgba.rows() / 2 - rHeight / 2), new Point(mRgba.cols() / 2 + rWidth / 2, mRgba.rows() / 2 + rHeight / 2), new Scalar(255, 255, 255), 1);

        return mRgba;
    }

    public void processFrame(Mat pFrameRGBA) {

//      Define the small area to be processed as rectangle and store this submatrix
        Rect rect = new Rect(((pFrameRGBA.cols() / 2) - (rWidth / 2)), ((pFrameRGBA.rows() / 2) - (rHeight / 2)), rWidth, rHeight);
        Mat imgRectangleContent = pFrameRGBA.submat(rect);

//      Write HSV-Colors from submatrix into imgHSV
        Imgproc.cvtColor(imgRectangleContent, imgHSV, Imgproc.COLOR_BGR2HSV);

//      imgHSV is seperated into specific Color-Ranges and saved into Matrixes for every Color
        Core.inRange(imgHSV, new Scalar(100, 20, 10), new Scalar(130, 255, 255), imgRed);
        Core.inRange(imgHSV, new Scalar(0, 20, 10), new Scalar(10, 255, 255), imgBlue);
        Core.inRange(imgHSV, new Scalar(55, 30, 15), new Scalar(65, 255, 255), imgGreen);
        Core.inRange(imgHSV, new Scalar(80, 50, 50), new Scalar(90, 255, 255), imgYellow);
        Core.inRange(imgHSV, new Scalar(140, 20, 10), new Scalar(160, 255, 255), imgPurple);
        Core.inRange(imgHSV, new Scalar(27, 20, 10), new Scalar(33, 255, 255), imgTurquoise);

//      Count all Pixels matching specific Colors
        colors[RED] = Core.countNonZero(imgRed);
        colors[GREEN] = Core.countNonZero(imgGreen);
        colors[BLUE] = Core.countNonZero(imgBlue);
        colors[YELLOW] = Core.countNonZero(imgYellow);
        colors[PURPLE] = Core.countNonZero(imgPurple);
        colors[TURQUOISE] = Core.countNonZero(imgTurquoise);

        /*
            Read out the index of the color which is most likely
         */
        int maxIndex = 0;
        for (int i = 0; i < colors.length; i++) {
            if (colors[i] > colors[maxIndex]) {
                maxIndex = i;
            }
        }

//      Regarding the experienced limits of the value of different colors we can check if all of them are off
//      values: last Average values from tests divided by 2
        if (colors[RED] < 188 && colors[GREEN] < 217 && colors[BLUE] < 308){
            System.out.println("OFF");
            output += 0;
        } else {
            switch (maxIndex) {
                case RED: {
                    System.out.println("RED");
                    output += 1;
                    break;
                }
                case BLUE: {
                    /*
                        STOP Condition
                     */
                    clockThread.stopThread();
                    System.out.println("BLUE");
                    measureErrorRateByOrder(output);
                    System.out.println(output.length() + " " + output);
                    break;
                }
                case GREEN: {
                     /*
                        START Condition
                     */
                    System.out.println("GREEN");
                    break;
                }
                case PURPLE: {
                    System.out.println("PURPLE");
                    break;
                }
            }
        }

        /*
            Temporary solution to delete leading '0' in output
            (all frames are stored currently)
         */
        if(output.length() > 0 && output.charAt(0) == '0'){
            output = output.substring(1, output.length());
        }
    }



    /*
        This Thread is in Charge to store the current frame in a regular interval
     */

    class ReceivingClockThread extends Thread {
        int delay;
        int counter = 0;
        long startTime = 0;
        long currTime = 0;
        long diff = 0;
        int diffCounter = 0;
        boolean running = true;
        public void stopThread() {
            running = false;
        }
        public void run() {
            while(running){


                try{
                    if(currentFrameRGBA != null){
                        counter++;

                        /*
                            Save timestamp of first received frame for synchronization of following frames
                         */

                        if(startTime == 0){
                            startTime = System.currentTimeMillis();
                        }
                        currTime = System.currentTimeMillis();

                        System.out.println(Thread.currentThread().getId() + "## Adding Frame ## " + System.currentTimeMillis() + " " + counter);

                        /*
                            Calculating the difference between currentframe and startframe
                         */
                        diff = currTime - startTime - ((counter - 1) * delay);
                        /*
                            Adding latest frame to synchronizedFrames
                         */
                        synchronizedFrames.add(currentFrameRGBA);


//                        Measuring the avg error of sleep
//                        System.out.println("DIFF: " + diff);
//                        diffCounter += diff;
//                        if(counter == 1000){
//                            System.out.println("AVG Diff: " + ((float)diffCounter / (float) counter));
//                        }

                    }
                  sleep(delay - diff);
                }catch(Exception e){
                    System.out.println(e);
                }
            }

        }
    }


    /*
        This Thread is waiting for stored Frames to be processed
     */

    class FrameProcessingThread extends Thread {
        public void run() {
            while(true){
                if(synchronizedFrames.size() > 0 && synchronizedFrames.get(0) != null){
                    System.out.println("## Processing Frame ## " + System.currentTimeMillis());
                    processFrame(synchronizedFrames.get(0));
                    synchronizedFrames.remove(0);
                }
            }

        }
    }



    public void measureErrorRateByOrder(String receivedBits){
        int errors = 0;

        char prev = '0';
        for(int i = 0; i < receivedBits.length(); i++){
            if(receivedBits.charAt(i) == prev){
                errors++;
            }
            prev = receivedBits.charAt(i);
        }
        System.out.println("Total: " + receivedBits.length() + " ErrorCount: " +errors);

    }
}
