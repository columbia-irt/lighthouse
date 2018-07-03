package io.sece.vlc.rcvr.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Range;
import android.util.Rational;
import android.util.Size;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


public class CameraHelper {
    private static final String TAG = "CameraHelper";
    private CameraManager mgr;
    public String id;
    private CameraCharacteristics camera;
    private StreamConfigurationMap map;


    public CameraHelper(Context context, String id) throws CameraException, CameraAccessException {
        this.id = id;
        mgr = getCameraManager(context);
        camera = mgr.getCameraCharacteristics(id);

        map = camera.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (null == map)
            throw new CameraException("Couldn't obtain stream configuration map");
    }


    public static CameraManager getCameraManager(Context context) throws CameraException {
        CameraManager mgr = (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
        if (null == mgr)
            throw new CameraException("Couldn't access Camera Manager");
        return mgr;
    }


    public boolean backFacing() throws CameraException {
        Integer facing = camera.get(CameraCharacteristics.LENS_FACING);
        if (null == facing)
            throw new CameraException("Couldn't get LENS_FACING attribute value");
        return facing == CameraCharacteristics.LENS_FACING_BACK;
    }


    /**
     * Return clockwise angle through which the output image needs to be rotated to be upright on
     * the device screen in its native orientation.
     */
    public int orientation() throws CameraException {
        Integer orientation = camera.get(CameraCharacteristics.SENSOR_ORIENTATION);
        if (orientation == null)
            throw new CameraException("Camera sensor orientation could not be obtained");
        return orientation;
    }


    private static String rangeToString(List<Range<Integer>> ranges) {
        StringBuilder b = new StringBuilder();
        for (Range<Integer> r : ranges) {
            b.append(r.toString());
            b.append(" ");
        }
        return b.toString();
    }


    private List<Range<Integer>> getFPSRanges() {
        Range<Integer>[] ranges = camera.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
        if (null == ranges)
            return Collections.emptyList();
        return Arrays.asList(ranges);
    }


    public String describeFPSRanges() {
        return rangeToString(getFPSRanges());
    }


    public String describeFormats() {
        StringBuilder b = new StringBuilder();
        for (int f : map.getOutputFormats()) {
            switch(f) {
                case ImageFormat.PRIVATE:
                    b.append(String.format(Locale.US, "PRIVATE(%d)", f));
                    break;

                case ImageFormat.JPEG:
                    b.append(String.format(Locale.US, "JPEG(%d)", f));
                    break;

                case ImageFormat.YUV_420_888:
                    b.append(String.format(Locale.US, "YUV_420_888(%d)", f));
                    break;

                case ImageFormat.RAW_SENSOR:
                    b.append(String.format(Locale.US, "RAW_SENSOR(%d)", f));
                    break;

                case ImageFormat.RAW_PRIVATE:
                    b.append(String.format(Locale.US, "RAW_PRIVATE(%d)", f));
                    break;

                case ImageFormat.RAW10:
                    b.append(String.format(Locale.US, "RAW10(%d)", f));
                    break;

                case ImageFormat.RAW12:
                    b.append(String.format(Locale.US, "RAW12(%d)", f));
                    break;

                default:
                    b.append(String.format(Locale.US, "%d", f));
                    break;
            }
            b.append(" ");
        }
        return b.toString();
    }


    public String describeHighSpeedSizesFor(int fps) {
        StringBuilder b = new StringBuilder();
        for (Size s : map.getHighSpeedVideoSizesFor(new Range<>(fps, fps))) {
            b.append(String.format(Locale.US, "%dx%d ", s.getWidth(), s.getHeight()));
        }
        return b.toString();
    }


    public int maxFPS() {
        return Collections.max(getFPSRanges().stream().map(Range::getUpper).collect(Collectors.toList()));
    }

    public StreamConfigurationMap getStreamMap() {
        return map;
    }

    public Rational getAeStep() {
        return camera.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP);
    }

    public Range<Integer> getAeRange() {
        return camera.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
    }


    @SuppressLint("MissingPermission")
    public CompletableFuture<CameraDevice> open() {
        Log.d(TAG, "Opening camera device " + id);
        final CompletableFuture<CameraDevice> rv = new CompletableFuture<>();

        try {
            mgr.openCamera(id, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice d) {
                    Log.d(TAG, "Camera device " + id + " opened");
                    rv.complete(d);
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice d) {
                    d.close();
                    Log.w(TAG, "Camera device " + id + " disconnected while opening");
                    rv.completeExceptionally(new CameraException("Camera disconnected while opening"));
                }

                @Override
                public void onError(@NonNull CameraDevice d, int err) {
                    d.close();
                    Log.e(TAG, "Error while opening camera device " + id + ": " + err);
                    rv.completeExceptionally(new CameraException("Error while opening camera device: " + err));
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error while opening camera device " + id, e);
            rv.completeExceptionally(e);
        }
        return rv;
    }


    public Size getSensorSize() throws CameraException {
        Rect rect = camera.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        if (null == rect)
            throw new CameraException("Couldn't get sensor size");
        return new Size(rect.width(), rect.height());
    }


    public float maxZoom() throws CameraException {
        Float size = camera.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
        if (null == size)
            throw new CameraException("Couldn't get max zoom parameter");
        return size;
    }


    public Rect zoomToCropRect(float zoom) {
        Size sensor;
        try {
            sensor = getSensorSize();
        } catch (CameraException e) {
            throw new RuntimeException(e);
        }

        int w = sensor.getWidth();
        int h = sensor.getHeight();

        Rect crop = new Rect(0, 0, w, h);

        int dx = w - Math.round((float)w / zoom);
        int dy = h - Math.round((float)h / zoom);

        crop.inset(dx / 2, dy / 2);
        return crop;
    }
}
