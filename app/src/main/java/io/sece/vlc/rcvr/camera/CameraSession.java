package io.sece.vlc.rcvr.camera;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.ImageReader;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.sece.vlc.rcvr.CSVWrite;
import io.sece.vlc.rcvr.ViewfinderFragment;
import io.sece.vlc.rcvr.ViewfinderModel;

public class CameraSession {
    private static final String TAG = "CameraSession";

    private Context context;
    private List<Surface> surfaces;
    public CameraSessionParams params;
    public CaptureRequest.Builder builder;
    public CameraHelper camera;
    public CameraDevice cameraDevice;

    public CameraCaptureSession captureSession;
    private CompletableFuture<CameraCaptureSession> syncInProgress;

    public Size frameResolution;
    public int fps;
    private CSVWrite csvWrite;


    public CameraSession(Context context, CameraSessionParams params) {
        this.context = context;
        this.params = params;
        csvWrite = new CSVWrite("params_");
    }


    public void setSurfaces(List<Surface> surfaces) {
        this.surfaces = surfaces;
        if (null != builder)
            for (Surface s : surfaces) builder.addTarget(s);
    }


    public void setDevice(CameraDevice device) throws CameraAccessException, CameraException {
        cameraDevice = device;
        camera = new CameraHelper(context, device.getId());
        builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        builder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);
        setSurfaces(surfaces);
    }


    private void configureBuilder(CameraSessionParams params) {
        builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, params.aeCompensation());
        builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, new Range<>(fps, fps));
        builder.set(CaptureRequest.SCALER_CROP_REGION, camera.zoomToCropRect(params.zoom()));
    }


    public CompletableFuture<CameraCaptureSession> start() {
        return createCaptureSession(params);
    }


    public void stop() {
        if (null != captureSession) {
            captureSession.close();
            captureSession = null;
        }
    }


    public void closeDevice() {
        stop();

        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }


    private CompletableFuture<CameraCaptureSession> createCaptureSession(CameraSessionParams params) {
        Log.d(TAG, "Creating camera capture session");
        final CompletableFuture<CameraCaptureSession> rv = new CompletableFuture<>();

        configureBuilder(params);

        try {
            cameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        session.setRepeatingRequest(builder.build(), new CameraCaptureSession.CaptureCallback() {
                            @Override
                            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                                super.onCaptureCompleted(session, request, result);
                                String[] data = {String.valueOf(result.get(CaptureResult.SENSOR_TIMESTAMP)),String.valueOf(result.get(CaptureResult.SENSOR_EXPOSURE_TIME)),String.valueOf(result.get(CaptureResult.CONTROL_AE_EXPOSURE_COMPENSATION))
                                ,String.valueOf(result.get(CaptureResult.SENSOR_SENSITIVITY)),String.valueOf(result.get(CaptureResult.CONTROL_AWB_MODE))
                                ,String.valueOf(result.get(CaptureResult.LENS_FOCUS_DISTANCE)),String.valueOf(ViewfinderFragment.zooming)};
                                try {
                                    csvWrite.write(data);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, null);
                        Log.d(TAG, "Camera capture session created");
                        captureSession = session;
                        rv.complete(session);

                    } catch (CameraAccessException e) {
                        Log.e(TAG, "Error while creating camera capture session", e);
                        rv.completeExceptionally(e);
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.e(TAG, "Error while creating camera capture session");
                    rv.completeExceptionally(new CameraException("Error while creating camera capture session"));
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error while creating camera capture session", e);
            rv.completeExceptionally(e);
        }
        return rv;
    }


    public void sync() {
        if (null == syncInProgress) updateCaptureSession();
    }


    private void updateCaptureSession() {
        CameraSessionParams p = params.clone();

        if (null == cameraDevice) {
            syncInProgress = null;
            return;
        }

        syncInProgress = createCaptureSession(p);
        syncInProgress.whenComplete((s, ex) -> {
            if (null != ex) {
                Log.e(TAG, "Error when creating capture session", ex);
                // TODO: Try to recreate the session a limited number of times
            }
            if (!params.equals(p)) updateCaptureSession();
            else syncInProgress = null;
        });
    }
}
