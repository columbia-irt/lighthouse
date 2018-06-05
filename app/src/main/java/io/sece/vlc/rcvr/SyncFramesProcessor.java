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
    static float[] hsv_curr;

    public SyncFramesProcessor(LinkedBlockingQueue syncBlockingQueue, Activity activity) {
        this.syncBlockingQueue = syncBlockingQueue;
        this.activity = activity;
        hsv_curr = new float[3];
    }

    @Override
    public void run() {
        while(true) {
            try {
                processFrame((Mat)syncBlockingQueue.take());
            }catch (InterruptedException e){
                System.out.println(e);
            }
        }
    }

    public void processFrame(Mat pFrame) {
        Mat originalFrame = pFrame.clone();
        if(pFrame != null){
            Mat imgHSV = new Mat(pFrame.rows(),pFrame.cols(), CvType.CV_8UC4);
            Imgproc.cvtColor(pFrame, imgHSV, Imgproc.COLOR_BGR2HSV);
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
                Output on UI for
             */
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    ((TextView)activity.findViewById(R.id.tvColorDetected)).setText("Current\nRGB: (" + r_curr + " ," + g_curr + " ," + b_curr + ")\nHSV: (" + (int)hsv_curr[0] + " ," + (int)hsv_curr[1] + " ," + (int)hsv_curr[2]  + ")");
                    (activity.findViewById(R.id.tvReceivedColor)).setBackgroundColor(android.graphics.Color.rgb(r_curr,g_curr,b_curr));
                    if(MainActivity.currHueValue != -1){
                        Color colorSent = Color.hsvToRGB(MainActivity.currHueValue, 100, 100);
                        (activity.findViewById(R.id.tvTransmitterSentColor)).setBackgroundColor(android.graphics.Color.rgb(colorSent.getRed(),colorSent.getGreen(),colorSent.getBlue()));
                    }
                }
            });
        }
    }



}
