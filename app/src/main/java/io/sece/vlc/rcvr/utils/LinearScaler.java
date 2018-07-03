package io.sece.vlc.rcvr.utils;


/**
 * Scales values from the source linear interval defined by fromMin and fromMax to the target
 * linear interval specified by toMin and toMax.
 */
public class LinearScaler {
    private double fromMin;
    private double toMin;
    private double factor;


    public LinearScaler(int toMin, int toMax, int fromMin, int fromMax) {
        factor = (double)(toMax - toMin) / (double)(fromMax - fromMin);
        this.fromMin = fromMin;
        this.toMin = toMin;
    }


    public int invoke(int value) {
        return (int)Math.round(toMin + factor * ((double)value - fromMin));
    }
}
