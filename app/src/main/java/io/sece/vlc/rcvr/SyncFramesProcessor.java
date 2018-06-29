package io.sece.vlc.rcvr;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.widget.TextView;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import io.sece.vlc.Color;


public class SyncFramesProcessor implements Runnable {
    private LinkedBlockingQueue syncBlockingQueue;
    private Activity activity;

    /*
        temporary variables used for color experiments
     */

    private int r_curr = 0;
    private int g_curr = 0;
    private int b_curr = 0;
    private static float[] hsv_curr;

    public static int gray_elimination_offset = 5;
    private ReceiverClass receiverClass;

    SyncFramesProcessor(LinkedBlockingQueue syncBlockingQueue, Activity activity, ReceiverClass receiverClass) {
        this.syncBlockingQueue = syncBlockingQueue;
        this.activity = activity;
        this.receiverClass = receiverClass;
        hsv_curr = new float[3];
    }

    @Override
    public void run() {
        while (true) {
            try {
                processFrame((Mat) syncBlockingQueue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void processFrame(Mat pFrame) throws InterruptedException{
        Mat imgHSV = new Mat(pFrame.rows(), pFrame.cols(), CvType.CV_8UC4);
        Imgproc.cvtColor(pFrame.clone(), imgHSV, Imgproc.COLOR_RGB2HSV);
//        Output Ranges: [0,180], [0,255], [0,255]
        byte[] signedBytes = new byte[imgHSV.channels() * imgHSV.cols() * imgHSV.rows()];
        imgHSV.get(0,0, signedBytes);

        double avg_h = 0;
        double avg_s = 0;
        double avg_v = 0;
        int counter = 0;

        /**
         * SignedBytes is an array of dimension 1 which means we have to iterate in steps by 3
         * to receive hsv pixels from a pixel
         */
        for(int i = 2; i < signedBytes.length; i+=3){
            if(signedBytes[i] < 0 && signedBytes[i-1] < 0 ) { // means v and s value are greater 128; signed byte
                counter ++;
                avg_h += signedBytes[i-2];
                avg_s += signedBytes[i-1];
                avg_v += signedBytes[i];
            }
        }

        if(counter > 0){
            int h = (int)(((avg_h + (256 * counter)) / counter) - 256) *2;
            int s = (int)((avg_s + (256 * counter)) / counter) - 256;
            int v = (int)((avg_v + (256 * counter)) / counter) - 256;
            int h_final = (h < 0) ? 255 + h : h;
            int s_final = (s < 0) ? 255 + s : s;
            int v_final = (v < 0) ? 255 + v : v;

            String ret = receiverClass.rx(receiverClass.getClosestElement(h));
            if(ret.length() > 0){
                System.out.println("Detected " + ret);
            }

            activity.runOnUiThread(() -> {
                TextView tvColorDetected = ((TextView) activity.findViewById(R.id.tvColorDetected));
                if (tvColorDetected != null) {
                    tvColorDetected.setText(h_final +", " + s_final + ", " + v_final);
                }
            });
        }
    }
}