package io.sece.vlc.rcvr;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.widget.TextView;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

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
        Mat originalFrame = pFrame.clone();
        Mat imgHSV = new Mat(pFrame.rows(), pFrame.cols(), CvType.CV_8UC4);
        Imgproc.cvtColor(pFrame, imgHSV, Imgproc.COLOR_BGR2HSV);
        r_curr = 0;
        g_curr = 0;
        b_curr = 0;

        int counter_curr = 0;
        for (int i = 0; i < originalFrame.cols(); i++) {
            for (int j = 0; j < originalFrame.rows(); j++) {
                int pixel_rgb_avg = ((int) (originalFrame.get(i, j)[0] + originalFrame.get(i, j)[1] + originalFrame.get(i, j)[2]) / 3);
//                if(!((originalFrame.get(i, j)[0] > (pixel_rgb_avg - gray_elimination_offset)) && (originalFrame.get(i, j)[0] < (pixel_rgb_avg + gray_elimination_offset)) && (originalFrame.get(i, j)[1] > (pixel_rgb_avg - gray_elimination_offset)) && (originalFrame.get(i, j)[1] < (pixel_rgb_avg + gray_elimination_offset)) && (originalFrame.get(i, j)[2] > (pixel_rgb_avg - gray_elimination_offset)) && (originalFrame.get(i, j)[2] < (pixel_rgb_avg + gray_elimination_offset)))){
                r_curr += originalFrame.get(i, j)[0];
                g_curr += originalFrame.get(i, j)[1];
                b_curr += originalFrame.get(i, j)[2];
                counter_curr += 1;
//                }
            }
        }
        if (counter_curr != 0) {
            r_curr = r_curr / counter_curr;
            g_curr = g_curr / counter_curr;
            b_curr = b_curr / counter_curr;
            hsv_curr = Color.RGBtoHSB(r_curr, g_curr, b_curr, null);

            try {
                String ret = receiverClass.rx(receiverClass.getClosestElement((int) hsv_curr[0]));
                System.out.println("Detected " + ret);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            activity.runOnUiThread(() -> {
                TextView tvColorDetected = ((TextView) activity.findViewById(R.id.tvColorDetected));
                TextView tvColorPreview = ((TextView) activity.findViewById(R.id.tvReceivedColor));
                if (tvColorDetected != null && tvColorPreview != null) {
                    tvColorDetected.setText("Current\nRGB: (" + r_curr + " ," + g_curr + " ," + b_curr + ")\nHSV: (" + (int) hsv_curr[0] + " ," + (int) hsv_curr[1] + " ," + (int) hsv_curr[2] + ")");
                    tvColorPreview.setBackgroundColor(android.graphics.Color.rgb(r_curr, g_curr, b_curr));
                    if (CameraFragment.currHueValue != -1) {
                        Color colorSent = Color.hsvToRGB(CameraFragment.currHueValue, 100, 100);
                        (activity.findViewById(R.id.tvTransmitterSentColor)).setBackgroundColor(android.graphics.Color.rgb(colorSent.getRed(), colorSent.getGreen(), colorSent.getBlue()));
                    }
                }
            });
        } else {
            activity.runOnUiThread(() -> {
                TextView tvColorDetected = ((TextView) activity.findViewById(R.id.tvColorDetected));
                TextView tvColorPreview = ((TextView) activity.findViewById(R.id.tvReceivedColor));
                if (tvColorDetected != null && tvColorPreview != null) {
                    tvColorDetected.setText("no valid color found");
                    tvColorPreview.setBackgroundColor(android.graphics.Color.rgb(0, 0, 0));
                }
            });
        }

    }
}