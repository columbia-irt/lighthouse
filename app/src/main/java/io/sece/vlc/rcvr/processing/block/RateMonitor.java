package io.sece.vlc.rcvr.processing.block;


import java.util.concurrent.TimeUnit;

import io.sece.vlc.rcvr.Bus;
import io.sece.vlc.rcvr.processing.Frame;
import io.sece.vlc.rcvr.processing.ProcessingBlock;
import io.sece.vlc.rcvr.utils.MovingAverage;
import io.sece.vlc.rcvr.utils.Uniq;


public class RateMonitor implements ProcessingBlock {
    // The window over which to calculate the moving average FPS
    private static final long AVG_WINDOW = 100;
    private static final TimeUnit AVG_WINDOW_UNIT = TimeUnit.MILLISECONDS;

    private String id;

    private MovingAverage fps = new MovingAverage(AVG_WINDOW, AVG_WINDOW_UNIT);
    private Uniq<Double> uniq = new Uniq<>();

    private long previousTimestamp = -1;


    public static class Event extends Bus.Event {
        public String id;
        public double fps;
        public long window = AVG_WINDOW;
        public TimeUnit unit = AVG_WINDOW_UNIT;

        public Event(String id, double fps) {
            this.id = id;
            this.fps = fps;
        }
    }


    public RateMonitor(String id) {
        this.id = id;
    }


    public synchronized Frame apply(Frame frame) {
        long timestamp = frame.getLongAttr(Frame.IMAGE_TIMESTAMP);

        if (previousTimestamp != -1) {
            // Calculate the FPS moving average value
            fps.update(1.0e9d / (double)(timestamp - previousTimestamp));

            // Round the average value to one decimal point to make sure we don't send updates
            // too often
            double v = Math.round(10.0d * fps.value) / 10.0d;
            if (uniq.hasChanged(v))
                Bus.send(new Event(id, v));
        }
        previousTimestamp = timestamp;
        return frame;
    }
}
