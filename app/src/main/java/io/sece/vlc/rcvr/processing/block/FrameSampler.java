package io.sece.vlc.rcvr.processing.block;

import io.sece.vlc.rcvr.processing.Frame;
import io.sece.vlc.rcvr.processing.ProcessingBlock;



public class FrameSampler implements ProcessingBlock {
    private long interval;  // Sampling interval in nano seconds
    private long next = 0;

    public FrameSampler(int baudRate) {
        setBaudRate(baudRate);
    }


    public void setBaudRate(long baudRate) {
        this.interval = 1000000000 / baudRate;
    }


    public Frame apply(Frame frame) {
//         using the first frame's timestamp to get sure that it's working on all devices since
//         different phones could have various timestamps depending on camera architecture
        if(next == 0) next = frame.getLongAttr(Frame.IMAGE_TIMESTAMP);

        long ts = frame.getLongAttr(Frame.IMAGE_TIMESTAMP);
        if (ts < next) return null;
        next += interval;
        return frame;
    }
}
