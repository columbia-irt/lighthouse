package io.sece.vlc.rcvr.processing.block;

import android.graphics.Rect;
import android.graphics.RectF;

import com.google.common.eventbus.Subscribe;

import io.sece.vlc.rcvr.Bus;
import io.sece.vlc.rcvr.ViewfinderFragment;
import io.sece.vlc.rcvr.processing.Frame;
import io.sece.vlc.rcvr.processing.ProcessingBlock;

public class RoIExtractor implements ProcessingBlock {
    private volatile RectF roi;


    public RoIExtractor(RectF init) {
        Bus.subscribe(this);
        this.roi = new RectF(init);
    }


    public Frame apply(Frame frame) {
        RectF roi = getRoI();

        Rect r = new Rect(Math.round(roi.left * frame.width), Math.round(roi.top * frame.height),
                Math.round(roi.right * frame.width), Math.round(roi.bottom * frame.height));

        // Make sure that the width and height of the crop rectangle are both even. This is needed
        // because the UV planes are sub-sampled by a factor of 2.

        if ((r.width() & 1) == 1) r.right--;
        if ((r.height() & 1) == 1) r.bottom--;

        // We need to make a copy of the frame data for later processing on a separate thread.
        // We're not interested in data outside of the region of interest, so we first crop
        // the frame to the region of interest. Cropping is a constant time operation.
        frame.crop(r);
        return frame;
    }


    private synchronized RectF getRoI() {
        return roi;
    }

    public synchronized void setBoundingBox(RectF roi) {
        this.roi = new RectF(roi);
    }
}
