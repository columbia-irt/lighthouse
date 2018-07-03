package io.sece.vlc.rcvr.utils;

import java.util.concurrent.TimeUnit;


public class MovingAverage {
    private final double window;
    private long lastUpdate;

    public double value = Double.NaN;

    public static class Change {
        public double value;
        public double window;

        public Change(MovingAverage value) {
            this.value = value.value;
            this.window = value.window;
        }
    }

    public MovingAverage(long window, TimeUnit unit) {
        this.window = unit.toNanos(window);
    }


    public void update(double sample) {
        long now = System.nanoTime();

        if (lastUpdate == 0) {
            value = sample;
        } else {
            double elapsed = now - lastUpdate;
            double decay = Math.exp(- elapsed / window);
            value = (1.0d - decay) * sample + decay * value;
        }
        lastUpdate = now;
    }
}
