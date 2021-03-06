package io.sece.vlc.rcvr;

import android.arch.lifecycle.ViewModel;
import android.graphics.Point;
import android.util.Size;

import io.sece.vlc.rcvr.camera.CameraSessionParams;


public class ViewfinderModel extends ViewModel {
    public static final int NO_BAUD_RATE = -1;
    public static final int DEFAULT_ROI_RADIUS = 100;
    public static final Point DEFAULT_ROI_CENTER = new Point(-1, -1);

    private static final Size DESIRED_RESOLUTION = new Size(1080, 1080);
    public Size targetResolution = DESIRED_RESOLUTION;

    public Point roiCenter = DEFAULT_ROI_CENTER;
    public int roiRadius = DEFAULT_ROI_RADIUS;

    public CameraSessionParams cameraParams = new CameraSessionParams();

    private int baudRate = -1;
    public String transmissionID;

    public Receiver receiver;

    public int getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
        Bus.send(new Bus.BaudRateChange(this.baudRate));
    }
}
