package io.sece.vlc.rcvr;

import android.app.Activity;
import android.widget.TextView;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.concurrent.LinkedBlockingQueue;

import io.sece.vlc.Color;

/**
 * Created by alex on 5/15/18.
 */

public class SyncFramesProcessor implements Runnable
{
    private LinkedBlockingQueue syncBlockingQueue;
    private Activity activity;

    /*
        temporary variables used for color experiments
     */
    int r_curr = 0;
    int g_curr = 0;
    int b_curr = 0;
    int counter_curr = 0;
    int r = 0;
    int g = 0;
    int b = 0;
    int counter = 0;
    static float[] hsv_red;
    static float[] hsv_curr;

    public SyncFramesProcessor(LinkedBlockingQueue syncBlockingQueue, Activity activity) {
        this.syncBlockingQueue = syncBlockingQueue;
        this.activity = activity;
        hsv_red = new float[3];
        hsv_curr = new float[3];
    }

    @Override
    public void run() {
        while(true) {
            try {
                System.out.println("Pre Process ");
                processFrameByPixelAmount((Mat)syncBlockingQueue.take());
            }catch (InterruptedException e){
                System.out.println(e);
            }
        }
    }

    public void processFrameByPixelAmount(Mat pFrame) {
        Mat originalFrame = pFrame.clone();
        if(pFrame != null){
            Mat imgRed = new Mat(pFrame.rows(),pFrame.cols(), CvType.CV_8UC4);
            Mat imgBlue= new Mat(pFrame.rows(),pFrame.cols(), CvType.CV_8UC4);
            Mat imgGreen= new Mat(pFrame.rows(),pFrame.cols(), CvType.CV_8UC4);
            Mat imgYellow= new Mat(pFrame.rows(),pFrame.cols(), CvType.CV_8UC4);
            Mat imgPurple= new Mat(pFrame.rows(),pFrame.cols(), CvType.CV_8UC4);
            Mat imgTurquoise= new Mat(pFrame.rows(),pFrame.cols(), CvType.CV_8UC4);
            Mat imgHSV = new Mat(pFrame.rows(),pFrame.cols(), CvType.CV_8UC4);

            //      Write HSV-Colors from submatrix into imgHSV
            Imgproc.cvtColor(pFrame, imgHSV, Imgproc.COLOR_BGR2HSV);

//      imgHSV is seperated into specific Colors-Ranges and saved into Matrixes for every Colors
            Core.inRange(imgHSV, new Scalar(100, 20, 10), new Scalar(130, 255, 255), imgRed);
            Core.inRange(imgHSV, new Scalar(0, 20, 10), new Scalar(10, 255, 255), imgBlue);
            Core.inRange(imgHSV, new Scalar(55, 30, 15), new Scalar(65, 255, 255), imgGreen);
            Core.inRange(imgHSV, new Scalar(80, 50, 50), new Scalar(90, 255, 255), imgYellow);
            Core.inRange(imgHSV, new Scalar(140, 20, 10), new Scalar(160, 255, 255), imgPurple);
            Core.inRange(imgHSV, new Scalar(27, 20, 10), new Scalar(33, 255, 255), imgTurquoise);

//      Count all Pixels matching specific Colors
            Integer[] colors= new Integer[6];
            colors[Colors.RED] = 0;
            colors[Colors.GREEN] = 0;
            colors[Colors.BLUE] = 0;
            colors[Colors.YELLOW] = 0;
            colors[Colors.PURPLE] = 0;
            colors[Colors.TURQUOISE] = 0;

//            Count all Pixels matching specific Colors
            colors[Colors.RED] = Core.countNonZero(imgRed);
            colors[Colors.GREEN] = Core.countNonZero(imgGreen);
            colors[Colors.BLUE] = Core.countNonZero(imgBlue);
            colors[Colors.YELLOW] = Core.countNonZero(imgYellow);
            colors[Colors.PURPLE] = Core.countNonZero(imgPurple);
            colors[Colors.TURQUOISE] = Core.countNonZero(imgTurquoise);

            System.out.println("Red: " + colors[Colors.RED]);
            System.out.println("Green: " + colors[Colors.GREEN]);
            System.out.println("Blue: " + colors[Colors.BLUE]);
            System.out.println("Yellow: " + colors[Colors.YELLOW]);
            System.out.println("Purple: " + colors[Colors.PURPLE]);
            System.out.println("Turquoise: " + colors[Colors.TURQUOISE]);
            System.out.println("Ausgabe");

            r_curr = 0;
            g_curr = 0;
            b_curr = 0;
            counter_curr = 0;
            for (int i = 0; i < originalFrame.cols(); i++) {
                for (int j = 0; j < originalFrame.rows(); j++) {
                        r_curr += originalFrame.get(i,j)[0];
                        g_curr += originalFrame.get(i,j)[1];
                        b_curr += originalFrame.get(i,j)[2];
                        counter_curr += 1;
                }
            }
            r_curr = r_curr/counter_curr;
            g_curr = g_curr/counter_curr;
            b_curr = b_curr/counter_curr;
            hsv_curr = Color.RGBtoHSB(r_curr,g_curr,b_curr, null);

            /*
                Calculation of Average Colors

                Step 1: Detect indices of Vectors which are in the treshold (in predefined color range)
             */

            for (int i = 0; i < imgRed.cols(); i++) {
                for (int j = 0; j < imgRed.rows(); j++) {
                    if((int)imgRed.get(i,j)[0] == 255){
                        r += originalFrame.get(i,j)[0];
                        g += originalFrame.get(i,j)[1];
                        b += originalFrame.get(i,j)[2];
                        counter += 1;
                        System.out.println("avg: (" + r/counter + " ," + g/counter + " ," + b/counter + ")");
                        hsv_red = Color.RGBtoHSB(r,g,b, null) ;
                        System.out.println(hsv_red);
                    }
                }
            }
            /*
                Just for the output on the UI - slows down the Thread
             */
            try {
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        ((TextView)activity.findViewById(R.id.tvColorDetected)).setText("Current\nRGB: (" + r_curr + " ," + g_curr + " ," + b_curr + ")\nHSV: (" + hsv_curr[0] + " ," + hsv_curr[1] + " ," + hsv_curr[2]  + ")");
                        (activity.findViewById(R.id.tvReceivedColor)).setBackgroundColor(android.graphics.Color.rgb(r_curr,g_curr,b_curr));
                        if(MainActivity.currHueValue != -1){
                            Color colorSent = Color.hsvToRGB(MainActivity.currHueValue, 100, 100);
                            (activity.findViewById(R.id.tvTransmitterSentColor)).setBackgroundColor(android.graphics.Color.rgb(colorSent.getRed(),colorSent.getGreen(),colorSent.getBlue()));
                        }

//                        if(counter != 0){
//                            ((TextView)activity.findViewById(R.id.tvColorDetected)).setText("curr-RGB:curr-hsv: (" + hsv_curr[0] + " ," + hsv_curr[1] + " ," + hsv_curr[2]  + ")\nred-rgb: (" + r/counter + " ," + g/counter + " ," + b/counter + ")\nred-hsv: (" + hsv_red[0] + " ," + hsv_red[1] + " ," + hsv_red[2]  + ")");
//                        }else{
//                            ((TextView)activity.findViewById(R.id.tvColorDetected)).setText("Red: " + colors[Colors.RED] + "\nGreen: " + colors[Colors.GREEN] + "\nBlue: " + colors[Colors.BLUE] + "\nYellow: " + colors[Colors.YELLOW] + "\nPurple: " + colors[Colors.PURPLE] + "\nTurquoise: " + colors[Colors.TURQUOISE]);
//
//                        }
                    }
                });
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
