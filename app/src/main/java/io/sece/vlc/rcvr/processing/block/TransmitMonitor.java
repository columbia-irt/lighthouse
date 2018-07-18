package io.sece.vlc.rcvr.processing.block;


import java.util.concurrent.TimeUnit;

import io.sece.vlc.Color;
import io.sece.vlc.Modem;
import io.sece.vlc.rcvr.Bus;
import io.sece.vlc.rcvr.processing.Frame;
import io.sece.vlc.rcvr.processing.ProcessingBlock;
import io.sece.vlc.rcvr.utils.MovingAverage;
import io.sece.vlc.rcvr.utils.Uniq;


public class TransmitMonitor implements ProcessingBlock {
    // The window over which to calculate the moving average FPS
    private static final long AVG_WINDOW = 600;
    private static final TimeUnit AVG_WINDOW_UNIT = TimeUnit.MILLISECONDS;

    private int baudRate;

    private MovingAverage fpsReceived = new MovingAverage(AVG_WINDOW, AVG_WINDOW_UNIT);
    private Uniq<Double> uniq = new Uniq<>();

    private Modem<Color> modem;

    private long previousTimestamp = -1;
    private Color prevColor;
    private int tolerance;
    private int count = 3;

    public static class Event extends Bus.Event {
        public boolean transmissionInProgress = false;
        public double fps;

        public Event(double fps, boolean transmissionInProgress) {
            this.fps = fps;
            this.transmissionInProgress = transmissionInProgress;
        }
    }


    public TransmitMonitor(int baudRate, Modem modem, double tolerance) {
        this.baudRate = baudRate;
        this.modem = modem;
        this.tolerance = (int)Math.round(baudRate * tolerance / 100.0d);
    }


    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }


    public synchronized Frame apply(Frame frame) {
        Color c = frame.getColorAttr(Frame.HUE);
        c = modem.detect(c);

        long timestamp = frame.getLongAttr(Frame.IMAGE_TIMESTAMP);

        if (prevColor != null && !prevColor.equals(c)) {
            // Calculate the FPS moving average value
            fpsReceived.update(baudRate);
            count = 3;
        } else {
            if (count == 0)
                fpsReceived.update(0);
            count--;
            if (count < 0) count = 0;
        }

        // Round the average value to one decimal point to make sure we don't send updates
        // too often
        double calcFPS = Math.round(10.0d * fpsReceived.value) / 10.0d;

        if (uniq.hasChanged(calcFPS))
            Bus.send(new Event(calcFPS, (calcFPS > (baudRate - tolerance) && calcFPS < (baudRate + tolerance))));

        prevColor = c;
        return frame;
    }
}
