package io.sece.vlc.rcvr.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class CameraHelper {
    private static final String TAG = "CameraHelper";
    private CameraManager mgr;
    public String id;
    private CameraCharacteristics camera;
    private StreamConfigurationMap map;

    public boolean backFacing;
    public int orientation;
    public List<Range<Integer>> fps;
    public Rational aeStep;
    public Range<Integer> aeRange;
    public Size sensorSize;
    public boolean calibratedTimestamps;
    public Range<Long> exposureRange;
    public float[] ndFilters;
    public float[] apertures;
    public float[] focalLengths;
    public Range<Integer> isoRange;
    public float maxDigitalZoom;



    public CameraHelper(Context context, String id) throws CameraException, CameraAccessException {
        this.id = id;
        mgr = getCameraManager(context);
        camera = mgr.getCameraCharacteristics(id);

        map = camera.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (null == map)
            throw new CameraException("Couldn't obtain stream configuration map");

        backFacing = getBackFacing();
        orientation = getOrientation();
        fps = getFPSRanges();
        aeStep = getAeStep();
        aeRange = getAeRange();
        sensorSize = getSensorSize();
        calibratedTimestamps = getCalibratedTimestamps();
        exposureRange = getExposureTimeRange();
        ndFilters = getFilterDensities();
        apertures = getApertures();
        focalLengths = getFocalLengths();
        isoRange = getSensitivities();
        maxDigitalZoom = getMaxDigitalZoom();
    }


    public static CameraManager getCameraManager(Context context) throws CameraException {
        CameraManager mgr = (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
        if (null == mgr)
            throw new CameraException("Couldn't access Camera Manager");
        return mgr;
    }


    protected boolean getBackFacing() throws CameraException {
        Integer facing = camera.get(CameraCharacteristics.LENS_FACING);
        if (null == facing)
            throw new CameraException("Couldn't get the LENS_FACING camera attribute value");
        return facing == CameraCharacteristics.LENS_FACING_BACK;
    }


    /**
     * Return clockwise angle through which the output image needs to be rotated to be upright on
     * the device screen in its native orientation.
     */
    protected int getOrientation() throws CameraException {
        Integer orientation = camera.get(CameraCharacteristics.SENSOR_ORIENTATION);
        if (orientation == null)
            throw new CameraException("Camera sensor orientation could not be obtained");
        return orientation;
    }


    protected List<Range<Integer>> getFPSRanges() {
        Range<Integer>[] ranges = camera.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
        if (null == ranges)
            return Collections.emptyList();
        return Arrays.asList(ranges);
    }


    public int maxFPS() {
        return Collections.max(fps.stream().map(Range::getUpper).collect(Collectors.toList()));
    }


    public StreamConfigurationMap getStreamMap() {
        return map;
    }


    protected Rational getAeStep() throws CameraException {
        Rational rv = camera.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP);
        if (rv == null)
            throw new CameraException("Couldn't obtain AE compensation step");
        return rv;
    }


    protected Range<Integer> getAeRange() throws CameraException {
        Range<Integer> rv = camera.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
        if (rv == null)
            throw new CameraException("Couldn't obtain AE compensation range");
        return rv;
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


    protected Size getSensorSize() throws CameraException {
        Rect rect = camera.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        if (null == rect)
            throw new CameraException("Couldn't get sensor size");
        return new Size(rect.width(), rect.height());
    }


    /**
     * Returns true if the timestamps provided by the camera for each frame can be correlated to
     * other system timestamps, e.g., from other sensors.
     */
    protected boolean getCalibratedTimestamps() {
        Integer src = camera.get(CameraCharacteristics.SENSOR_INFO_TIMESTAMP_SOURCE);
        if (src == null)
            src = CameraMetadata.SENSOR_INFO_TIMESTAMP_SOURCE_UNKNOWN;

        return src == CameraMetadata.SENSOR_INFO_TIMESTAMP_SOURCE_REALTIME;
    }


    // Returns true if the device supports the required hardware level, or better.
    public boolean getHardwareLevel(int required) {
        int level = camera.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        if (level == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY)
            return required == level;

        // deviceLevel is not LEGACY, can use numerical sort
        return required <= level;
    }


    protected Range<Long> getExposureTimeRange() throws CameraException {
        Range<Long> rv = camera.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
        if (rv == null)
            throw new CameraException("Could't get exposure time range");
        return rv;
    }


    protected float[] getFilterDensities() {
        float[] rv = camera.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FILTER_DENSITIES);
        if (rv == null)
            rv = new float[]{0};
        return rv;
    }


    protected float[] getApertures() throws CameraException {
        float[] rv = camera.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES);
        if (rv == null)
            throw new CameraException("No apertures reported");
        return rv;
    }


    protected float[] getFocalLengths() throws CameraException {
        float[] rv = camera.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
        if (rv == null)
            throw new CameraException("No focal lengths reported");
        return rv;
    }


    protected Range<Integer> getSensitivities() throws CameraException {
        Range<Integer> rv = camera.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
        if (rv == null)
            throw new CameraException("No sensitivities reported");
        return rv;
    }


    protected float getMaxDigitalZoom() throws CameraException {
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


    private static void rangeListToString(StringBuilder b, List<Range<Integer>> ranges) {
        for (Range<Integer> r : ranges) {
            b.append(r.toString());
            b.append(",");
        }
    }


    private void describeFormats(StringBuilder b) {
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
            b.append(",");
        }
    }


    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(String.format(Locale.US, "Camera %s:", id));
        b.append(String.format(Locale.US, " back_facing=%b", backFacing));
        b.append(String.format(Locale.US, " orientation=%d", orientation));
        b.append(" fps_ranges=");
        rangeListToString(b, fps);
        b.append(String.format(Locale.US, " ae_range=%s", aeRange.toString()));
        b.append(String.format(Locale.US, " ae_step=%s", aeStep.toString()));
        b.append(String.format(Locale.US, " sensor_size=%s", sensorSize.toString()));
        b.append(String.format(Locale.US, " calibrated_timestamps=%b", calibratedTimestamps));

        long minExposure = (long)1e+9 / exposureRange.getLower();
        long maxExposure = TimeUnit.NANOSECONDS.toSeconds(exposureRange.getUpper());
        b.append(String.format(Locale.US, " exposure_range=[%s,%d\"]", new Rational(1, (int)minExposure).toString(), maxExposure));

        b.append(String.format(Locale.US, " nd_filters=%s", Arrays.toString(ndFilters)));
        b.append(String.format(Locale.US, " apertures=%s", Arrays.toString(apertures)));
        b.append(String.format(Locale.US, " focal_lengths=%s", Arrays.toString(focalLengths)));
        b.append(String.format(Locale.US, " iso_range=%s", isoRange.toString()));
        b.append(String.format(Locale.US, " max_digital_zoom=%s", maxDigitalZoom));
        b.append(" formats=");
        describeFormats(b);
        return b.toString();
    }
}
