package io.sece.vlc.rcvr.processing.block;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import io.sece.vlc.rcvr.processing.Frame;
import io.sece.vlc.rcvr.processing.ProcessingBlock;

public class HueDetector implements ProcessingBlock {
    private static final float DEFAULT_S_THRESHOLD = 0.5f;
    private static final float DEFAULT_V_THRESHOLD = 0.5f;
    public static final long NO_HUE_DETECTED = Long.MAX_VALUE;

    private float s_threshold;
    private float v_threshold;


    public HueDetector() {
        this(DEFAULT_S_THRESHOLD, DEFAULT_V_THRESHOLD);
    }


    public HueDetector(float s_threshold, float v_threshold) {
        this.s_threshold = s_threshold;
        this.v_threshold = v_threshold;
    }

    // Since hue is an angle, we need to calculate average hue as follows:
    // https://rosettacode.org/wiki/Averages/Mean_angle

    // TODO: Pre-allocate some of the buffers and keep them in thread-local storage as optimization

    public Frame apply(Frame frame) {
        Mat rgb = new Mat();
        Mat hsv = new Mat();
        frame.rgba().convertTo(rgb, CvType.CV_32FC3, 1.0 / 255d);
        Imgproc.cvtColor(rgb, hsv, Imgproc.COLOR_RGB2HSV_FULL);

        int ch = hsv.channels();
        float[] buf = new float[hsv.rows() * hsv.cols() * ch];
        hsv.get(0, 0, buf);

        double x = 0.0;
        double y = 0.0;

        int n = 0;

        double angle;
        for(int i = 0; i < buf.length; i += ch) {
            if (buf[i + 1] > s_threshold && buf[i + 2] > v_threshold) {
                angle = Math.toRadians(buf[i]);
                x += Math.cos(angle);
                y += Math.sin(angle);
                n++;
            }
        }

        long avg;
        if (n > 0) {
            avg = Math.round(Math.toDegrees(Math.atan2(y / n, x / n)));
            if (avg < 0) avg += 360;
        } else {
            avg = NO_HUE_DETECTED;
        }

        frame.set(Frame.DETECTED_HUE, avg);
        return frame;
    }
}
