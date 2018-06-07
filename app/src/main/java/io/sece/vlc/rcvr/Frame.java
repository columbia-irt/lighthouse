package io.sece.vlc.rcvr;

import android.graphics.ImageFormat;
import android.media.Image;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * An OpenCV compatible wrapper for Android Image.
 *
 * This class converts an Android Image object into something OpenCV can work with. The OpenCV
 * library expects two methods: rgba() and gray(). The image given to us by the Android Camera2
 * framework will be in YUV format.
 *
 * WARNING: The Frame object directly references the buffer of the image object for its Y (and
 * gray) Mats! For that reason, the lifetime of this object is directly tied to the lifetime of the
 * image. Once close() has been called on the image, this object can no longer be used. Accessing
 * the Y/gray Mats will result in undefined behavior.
 *
 * Call the method copy to create a copy of the frame that will not depend on the Image buffer
 * data. The resulting copy is safe to keep and pass around after the corresponding Image has been
 * closed.
 *
 * It is possible to keep a long-lived Frame instance and repeatedly call setImage on the instance
 * with new image data. In that case, the Frame object will keep reusing whatever buffers it
 * already created for the Mats.
 */
class Frame implements CameraBridgeViewBase.CvCameraViewFrame {
    private static final int STRIDE = 2;

    public int width;
    public int height;
    public long timestamp;

    public Mat y;      // Refers to the Image buffer for data
    public Mat uv;     // Refers to the Image buffer for data

    private Mat _gray; // Refers to y Mat for data

    private Mat _rgba; // Always allocates its own buffer to store data
    private boolean refreshRGBA = true;


    static private void checkImageFormat(Image img) throws Exception {
        if (img.getFormat() != ImageFormat.YUV_420_888)
            throw new Exception("Unsupported image format");

        Image.Plane[] p = img.getPlanes();

        if (p.length != 3)
            throw new Exception("Unsupported number of image planes: " + p.length);

        if (p[0].getPixelStride() != 1)
            throw new Exception("Unsupported plane 0 stride: " + p[0].getPixelStride());

        if (p[1].getPixelStride() != STRIDE)
            throw new Exception("Unsupported plane 1 stride: " + p[1].getPixelStride());

        if (p[2].getPixelStride() != STRIDE)
            throw new Exception("Unsupported plane 2 stride: " + p[2].getPixelStride());
    }


    /**
     * Create a copy of the frame that does not depend on Image buffers. The copy can be passed
     * around after the corresponding Image object has been closed.
     */
    public Frame copy() {
        Frame f = new Frame();

        f.width = width;
        f.height = height;
        f.timestamp = timestamp;

        f.y = y.clone();
        f.uv = uv.clone();
        return f;
    }


    public void setImage(Image image) throws Exception {
        checkImageFormat(image);

        width = image.getWidth();
        height = image.getHeight();
        timestamp = image.getTimestamp();

        Image.Plane[] p = image.getPlanes();
        y = new Mat(height, width, CvType.CV_8UC1, p[0].getBuffer());
        uv = new Mat(height / STRIDE, width / STRIDE, CvType.CV_8UC2, p[1].getBuffer());

        // Make sure that the gray Mat is re-created next time it is accessed.
        _gray = null;

        // Make sure the RGBA Mat is re-created next time it is accessed;
        refreshRGBA = true;
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
            refreshRGBA = true;
        }
        if (refreshRGBA) {
            Imgproc.cvtColorTwoPlane(y, uv, _rgba, Imgproc.COLOR_YUV420sp2BGRA);
            refreshRGBA = false;
        }
        return _rgba;
    }


    /**
     * Manually de-allocate all native memory consumed by Mats, before the object is garbage
     * collected. It is recommended to manually call this method once the object is no longer
     * needed, rather than waiting for the GC to destroy the object and release the native
     * buffers.
     */
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
}
