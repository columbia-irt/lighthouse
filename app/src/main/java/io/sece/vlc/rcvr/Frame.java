package io.sece.vlc.rcvr;

import android.graphics.ImageFormat;
import android.media.Image;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;


class Frame implements CameraBridgeViewBase.CvCameraViewFrame {
    private static final int STRIDE = 2;

    public Mat y;
    public Mat uv;

    private Mat _rgba;
    private Mat _gray;

    public int width;
    public int height;
    public long timestamp;

    static private void checkImageFormat(Image img) {
        assert (img.getFormat() == ImageFormat.YUV_420_888);

        Image.Plane[] p = img.getPlanes();
        assert (p.length == 3);
        assert (p[0].getPixelStride() == 1);
        assert (p[1].getPixelStride() == STRIDE);
        assert (p[2].getPixelStride() == STRIDE);
    }

    public Frame(Image image) {
        checkImageFormat(image);

        Image.Plane[] p = image.getPlanes();

        width = image.getWidth();
        height = image.getHeight();
        timestamp = image.getTimestamp();

        y = new Mat(height, width, CvType.CV_8UC1, p[0].getBuffer());
        uv = new Mat(height / STRIDE, width / STRIDE, CvType.CV_8UC2, p[1].getBuffer());
    }

    public void release() {
        if (null != _rgba) {
            _rgba.release();
            _rgba = null;
        }

        if (null != _gray) {
            _gray.release();
            _gray = null;
        }

        if (null != y) {
            y.release();
            y = null;
        }

        if (null != uv) {
            uv.release();
            uv = null;
        }
    }

    @Override
    public Mat gray() {
        if (null == _gray) {
            _gray = y.submat(0, width, 0, height);
        }
        return _gray;
    }

    @Override
    public Mat rgba() {
        if (null == _rgba) {
            _rgba = new Mat();
            Imgproc.cvtColorTwoPlane(y, uv, _rgba, Imgproc.COLOR_YUV2RGBA_NV21);
        }
        return _rgba;
    }

    @Override
    protected void finalize() {
        release();
    }
}
