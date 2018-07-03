package io.sece.vlc.rcvr;

import android.arch.lifecycle.ViewModel;
import android.graphics.Point;
import android.util.Size;

import io.sece.vlc.rcvr.camera.CameraSessionParams;


public class ViewfinderModel extends ViewModel {
    public static final int DEFAULT_ROI_RADIUS = 60;
    public static final Point DEFAULT_ROI_CENTER = new Point(-1, -1);

    private static final Size DESIRED_RESOLUTION = new Size(1080, 1080);
    public Size targetResolution = DESIRED_RESOLUTION;

    public Point roiCenter = DEFAULT_ROI_CENTER;
    public int roiRadius = DEFAULT_ROI_RADIUS;

    public CameraSessionParams cameraParams = new CameraSessionParams();
}
