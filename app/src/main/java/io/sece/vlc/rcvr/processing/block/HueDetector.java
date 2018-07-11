package io.sece.vlc.rcvr.processing.block;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import io.sece.vlc.Color;
import io.sece.vlc.rcvr.processing.Frame;
import io.sece.vlc.rcvr.processing.ProcessingBlock;

public class HueDetector implements ProcessingBlock {
    private static final float DEFAULT_S_THRESHOLD = 0.5f;
    private static final float DEFAULT_V_THRESHOLD = 0.5f;

    private float s_threshold;
    private float v_threshold;

    private final ThreadLocal<Mat> rgbBuf = new ThreadLocal<Mat>() {
        @Override protected Mat initialValue() {
            return new Mat();
        }
    };

    private final ThreadLocal<Mat> hsvBuf = new ThreadLocal<Mat>() {
        @Override protected Mat initialValue() {
            return new Mat();
        }
    };

    private final ThreadLocal<float[]> floatBuf = new ThreadLocal<>();


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
        Mat rgb = rgbBuf.get();
        Mat hsv = hsvBuf.get();

        frame.rgba().convertTo(rgb, CvType.CV_32FC3, 1.0 / 255d);
        Imgproc.cvtColor(rgb, hsv, Imgproc.COLOR_RGB2HSV_FULL);

        int ch = hsv.channels();
        int len = hsv.rows() * hsv.cols() * ch;

        float[] buf = floatBuf.get();
        if (null == buf || buf.length != len) {
            buf = new float[len];
            floatBuf.set(buf);
        }

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

        long avgHue = 0;
        long brightness = 0;

        if (n > 0) {
            avgHue = Math.round(Math.toDegrees(Math.atan2(y / n, x / n)));
            if (avgHue < 0) avgHue += Color.MAX_HUE;
            brightness = Color.MAX_BRIGHTNESS;
        }

        frame.setAttr(Frame.HUE, new Color((int)avgHue, (int)brightness));
        return frame;
    }
}
