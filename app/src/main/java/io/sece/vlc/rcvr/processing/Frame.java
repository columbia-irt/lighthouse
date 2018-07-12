package io.sece.vlc.rcvr.processing;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.Image;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.HashMap;
import java.util.Map;

import io.sece.vlc.Color;

/**
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
public class Frame implements CameraBridgeViewBase.CvCameraViewFrame {
    public static final String IMAGE_TIMESTAMP = "IMAGE_TIMESTAMP";
    public static final String RX_TIMESTAMP = "RX_TIMESTAMP";
    public static final String PROCESSING_START = "PROCESSING_START";
    public static final String PROCESSING_END = "PROCESSING_END";
    public static final String HUE = "HUE";
    public static final String CURRENT_SEQUENCE = "CURRENT_SEQUENCE";

    public int width;
    public int height;
    public long sequence; // Monotonically increasing frame sequence number

    public HashMap<String, Object> attrs = new HashMap<>();

    private Mat y;      // Refers to the Image buffer for data
    private Mat uv;     // Refers to the Image buffer for data

    private Mat _gray; // Refers to y Mat for data
    private Mat _rgba; // Always allocates its own buffer to storeRX data
    private boolean refreshRGBA = true;


    static private void checkImageFormat(Image img) {
        if (img.getFormat() != ImageFormat.YUV_420_888)
            throw new AssertionError("Unsupported image format: " + img.getFormat());

        Image.Plane[] p = img.getPlanes();

        if (p.length != 3)
            throw new AssertionError("Unsupported number of image planes: " + p.length);

        if (p[0].getPixelStride() != 1)
            throw new AssertionError("Unsupported plane 0 pixel stride: " + p[0].getPixelStride());

        if (p[1].getPixelStride() != 2)
            throw new AssertionError("Unsupported plane 1 pixel stride: " + p[1].getPixelStride());

        if (p[2].getPixelStride() != 2)
            throw new AssertionError("Unsupported plane 2 pixel stride: " + p[2].getPixelStride());

        if (p[1].getRowStride() != p[0].getRowStride())
            throw new AssertionError("The YUV format is not semi-planar");
    }


    /**
     * Create a copy of the frame that does not depend on Image buffers. The copy can be passed
     * around after the corresponding Image object has been closed.
     */
    @SuppressWarnings("unchecked")
    public Frame copy() {
        Frame f = new Frame();

        f.width = width;
        f.height = height;
        f.sequence = sequence;

        f.attrs = (HashMap<String,Object>)((HashMap<String,Object>)attrs).clone();

        f.y = y.clone();
        f.uv = uv.clone();

        return f;
    }


    public Image set(Image image) {
        checkImageFormat(image);

        width = image.getWidth();
        height = image.getHeight();
        attrs.put(IMAGE_TIMESTAMP, image.getTimestamp());

        Image.Plane[] p = image.getPlanes();

        // Rows in an image can be aligned, hence, we need to use getRowStride() to determine the
        // length of a row in bytes, and then create a submat to get to the original width.
        // The Y plan is guaranteed not to be interleaved with the UV planes and pixel stride is
        // always 1.

        y = new Mat(height, p[0].getRowStride(), CvType.CV_8UC1, p[0].getBuffer())
                .submat(0, height, 0, width);

        // The following conversion code assumes a semi-planar YUV format where the U & V planes
        // are sub-sampled by a factor of two and *interleaved*. See the following page for more
        // information: https://wiki.videolan.org/YUV/

        uv = new Mat(height / 2, p[1].getRowStride() / 2, CvType.CV_8UC2, p[1].getBuffer())
                .submat(0, height / 2, 0, width / 2);

        // Make sure that the gray Mat is re-created next time it is accessed.
        _gray = null;

        // Make sure the RGBA Mat is re-created next time it is accessed.
        refreshRGBA = true;

        return image;
    }


    public <T extends Number> T setAttr(String key, T val) {
        attrs.put(key, val);
        return val;
    }


    public boolean setAttr(String key, boolean val) {
        attrs.put(key, val);
        return val;
    }


    public Color setAttr(String key, Color val) {
        attrs.put(key, val);
        return val;
    }


    public int getIntAttr(String key) {
        return (int)attrs.get(key);
    }


    public long getLongAttr(String key) {
        return (long)attrs.get(key);
    }


    public boolean getBooleanAttr(String key) {
        return (boolean)attrs.get(key);
    }


    public Color getColorAttr(String key) {
        return (Color)attrs.get(key);
    }


    /**
     * Constant-time cropping to the given region of interest.
     * @param roi Region of interest rectangle
     */
    public void crop(Rect roi) {
        // Since the uv matrix has half the width and height of the y matrix, we need to make sure
        // that the cropping rectangle has even width and height, otherwise an assertion in opencv
        // will crash the program.

        if ((roi.width() & 1) == 1)
            throw new IllegalArgumentException("Crop rectangle width must be even: " + roi.width());

        if ((roi.height() & 1) == 1)
            throw new IllegalArgumentException("Crop rectangle height must be even: " + roi.height());

        y = y.submat(roi.top, roi.bottom, roi.left, roi.right);
        uv = uv.submat(roi.top / 2, roi.bottom / 2, roi.left / 2, roi.right / 2);


        width = y.width();
        height = y.height();

        // Make sure that the gray Mat is re-created next time it is accessed.
        _gray = null;

        // Make sure the RGBA Mat is re-created next time it is accessed.
        refreshRGBA = true;
    }


    @Override
    public Mat gray() {
        if (null == _gray)
            _gray = y.submat(0, height, 0, width);
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
