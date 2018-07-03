package io.sece.vlc.rcvr.camera;


public class CameraSessionParams implements Cloneable {
    private static final float MIN_ZOOM = 1.0f;
    private static final int DEFAULT_AE_COMPENSATION = 0;

    private float zoom = MIN_ZOOM;
    private int aeCompensation = DEFAULT_AE_COMPENSATION;


    public float zoom() {
        return zoom;
    }

    public synchronized float zoom(float val) {
        return this.zoom = val;
    }

    public int aeCompensation() {
        return aeCompensation;
    }

    public synchronized int aeCompensation(int val) {
        return this.aeCompensation = val;
    }

    public synchronized CameraSessionParams clone() {
        try {
            return (CameraSessionParams)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized boolean equals(CameraSessionParams o) {
        return zoom == o.zoom
                && aeCompensation == o.aeCompensation;
    }
}
