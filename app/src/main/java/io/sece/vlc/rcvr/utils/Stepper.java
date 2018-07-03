package io.sece.vlc.rcvr.utils;


public class Stepper {
    private int step;

    public Stepper(int step) {
        this.step = step;
    }

    public int invoke(int value) {
        return value / step * step;
    }
}
