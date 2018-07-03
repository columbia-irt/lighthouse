package io.sece.vlc.rcvr.processing.block;

import java.util.concurrent.TimeUnit;

import io.sece.vlc.rcvr.processing.Frame;
import io.sece.vlc.rcvr.processing.ProcessingBlock;



public class FrameSampler implements ProcessingBlock {

    private long interval;  // Sampling interval in nano seconds
    private long lastAcceptedFrame = 0;


    public FrameSampler(long interval, TimeUnit unit) {
        this.interval = unit.toNanos(interval);
    }


    public Frame apply(Frame frame) {
        long stamp = frame.get(Frame.IMAGE_TIMESTAMP);

        if (stamp < lastAcceptedFrame + interval) return null;
        lastAcceptedFrame = stamp;
        return frame;
    }
}
