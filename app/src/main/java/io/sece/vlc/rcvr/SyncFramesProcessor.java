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
    private void processFrame(Mat pFrame) {
        Mat imgHSV = new Mat(pFrame.rows(), pFrame.cols(), CvType.CV_8UC4);
        Imgproc.cvtColor(pFrame.clone(), imgHSV, Imgproc.COLOR_RGB2HSV);
//        Output Ranges: [0,180], [0,255], [0,255]


        List<Mat> channels = new ArrayList<Mat>(3);
        Core.split(imgHSV, channels);
        Mat H = channels.get(0);
        Mat S = channels.get(1);
        Mat V = channels.get(2);
        int h = (int)Core.mean(H).val[0] * 2;
        int s = (int)Core.mean(S).val[0];
        int v = (int)Core.mean(V).val[0];

        activity.runOnUiThread(() -> {
            TextView tvColorDetected = ((TextView) activity.findViewById(R.id.tvColorDetected));
            try{
                tvColorDetected.setText("HSV: (" + h  + ", " + s + ", " + v +")");
            }catch(Exception e){}
        });
        try {
            String ret = receiverClass.rx(receiverClass.getClosestElement(h));
            System.out.println("Detected " + ret);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /*
        Copy entire HSV Frame

        int counter_curr = 0;
        Mat originalFrame = new Mat();
        imgHSV.copyTo(originalFrame);

        int h_curr = 0;
        int s_curr = 0;
        int v_curr = 0;
        for (int i = 0; i < originalFrame.cols(); i++) {
            for (int j = 0; j < originalFrame.rows(); j++) {
                h_curr += originalFrame.get(i, j)[0];
                s_curr += originalFrame.get(i, j)[1];
                v_curr += originalFrame.get(i, j)[2];
                counter_curr += 1;
            }
        }
        h_curr = h_curr / counter_curr;
        s_curr = s_curr / counter_curr;
        v_curr = v_curr / counter_curr;
        System.out.println("HSV: (" + h_curr  + ", " + s_curr + ", " + v_curr +")");
        */

//        Copy channels seperate
//
//        Mat copied_H = new Mat();
//        H.copyTo(copied_H);
//        Mat copied_S = new Mat();
//        S.copyTo(copied_S);
//        Mat copied_V = new Mat();
//        V.copyTo(copied_V);
//        int counter_curr = 0;
//        double avg_hue = 0;
//        double avg_sat = 0;
//        double avg_v = 0;
//        for (int i = 0; i < copied_S.cols(); i++) {
//            for (int j = 0; j < copied_S.rows(); j++) {
//                double curr_s = copied_S.get(i, j)[0];
//                double curr_v = copied_V.get(i, j)[0];
//                if(curr_s > 30 && curr_v > 30){
//                    avg_hue += copied_H.get(i,j)[0];
//                    avg_sat += curr_s;
//                    avg_v += curr_v;
//                    counter_curr += 1;
//                }
//            }
//        }
//        avg_hue = avg_hue/counter_curr;
//        avg_sat=avg_sat/counter_curr;
//        avg_v =avg_v/counter_curr;
//        System.out.println("HSV: (" + avg_hue  + ", " + avg_sat + ", " + avg_v +")");
//        System.out.println("Average by mean Hue: " + Core.mean(H).toString() + " " +  Core.mean(S).val[0]+ " " +  Core.mean(V).val[0]);



//          Previous way using RGB
//        int counter_curr = 0;
//        for (int i = 0; i < originalFrame.cols(); i++) {
//            for (int j = 0; j < originalFrame.rows(); j++) {
//                int pixel_rgb_avg = ((int) (originalFrame.get(i, j)[0] + originalFrame.get(i, j)[1] + originalFrame.get(i, j)[2]) / 3);
////                if(!((originalFrame.get(i, j)[0] > (pixel_rgb_avg - gray_elimination_offset)) && (originalFrame.get(i, j)[0] < (pixel_rgb_avg + gray_elimination_offset)) && (originalFrame.get(i, j)[1] > (pixel_rgb_avg - gray_elimination_offset)) && (originalFrame.get(i, j)[1] < (pixel_rgb_avg + gray_elimination_offset)) && (originalFrame.get(i, j)[2] > (pixel_rgb_avg - gray_elimination_offset)) && (originalFrame.get(i, j)[2] < (pixel_rgb_avg + gray_elimination_offset)))){
//                r_curr += originalFrame.get(i, j)[0];
//                g_curr += originalFrame.get(i, j)[1];
//                b_curr += originalFrame.get(i, j)[2];
//                counter_curr += 1;
////                }
//            }
//        }
//        if (counter_curr != 0) {
//            r_curr = r_curr / counter_curr;
//            g_curr = g_curr / counter_curr;
//            b_curr = b_curr / counter_curr;
//            hsv_curr = Color.RGBtoHSB(r_curr, g_curr, b_curr, null);
//
//
//
//            activity.runOnUiThread(() -> {
//                TextView tvColorDetected = ((TextView) activity.findViewById(R.id.tvColorDetected));
//                TextView tvColorPreview = ((TextView) activity.findViewById(R.id.tvReceivedColor));
//                if (tvColorDetected != null && tvColorPreview != null) {
//                    tvColorDetected.setText("Current\nRGB: (" + r_curr + " ," + g_curr + " ," + b_curr + ")\nHSV: (" + (int) hsv_curr[0] + " ," + (int) hsv_curr[1] + " ," + (int) hsv_curr[2] + ")");
//                    tvColorPreview.setBackgroundColor(android.graphics.Color.rgb(r_curr, g_curr, b_curr));
//                    if (CameraFragment.currHueValue != -1) {
//                        Color colorSent = Color.hsvToRGB(CameraFragment.currHueValue, 100, 100);
//                        (activity.findViewById(R.id.tvTransmitterSentColor)).setBackgroundColor(android.graphics.Color.rgb(colorSent.getRed(), colorSent.getGreen(), colorSent.getBlue()));
//                    }
//                }
//            });
//        } else {
//            activity.runOnUiThread(() -> {
//                TextView tvColorDetected = ((TextView) activity.findViewById(R.id.tvColorDetected));
//                TextView tvColorPreview = ((TextView) activity.findViewById(R.id.tvReceivedColor));
//                if (tvColorDetected != null && tvColorPreview != null) {
//                    tvColorDetected.setText("no valid color found");
//                    tvColorPreview.setBackgroundColor(android.graphics.Color.rgb(0, 0, 0));
//                }
//            });
//        }

    }
}