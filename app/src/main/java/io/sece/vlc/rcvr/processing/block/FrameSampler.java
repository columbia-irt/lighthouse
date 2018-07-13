package io.sece.vlc.rcvr.processing.block;

import java.util.concurrent.TimeUnit;

import io.sece.vlc.Color;
import io.sece.vlc.Modem;
import io.sece.vlc.rcvr.ViewfinderModel;
import io.sece.vlc.rcvr.processing.Frame;
import io.sece.vlc.rcvr.processing.ProcessingBlock;



public class FrameSampler implements ProcessingBlock {

    private long interval;  // Sampling interval in nano seconds
    private long lastAcceptedFrame = 0;
    private Modem<Color> modem;
    private Color prevColor;
    private int sameColorCounter = 0;

    public FrameSampler(long interval, TimeUnit unit, Modem modem) {
        this.interval = unit.toNanos(interval);
        this.modem = modem;
    }


    public Frame apply(Frame frame) {
        Color currColor = frame.getColorAttr(Frame.HUE);
        currColor = modem.detect(currColor);

        if(prevColor != null){
            long stamp = frame.getLongAttr(Frame.IMAGE_TIMESTAMP);
            if(prevColor == currColor){
                sameColorCounter++;
            }else{
//                System.out.println("color processed: " + sameColorCounter + " " + modem.demodulate(currColor));
                sameColorCounter = 0;
            }


            if (stamp < lastAcceptedFrame + interval){
                prevColor = currColor;
                return null;
            }
            lastAcceptedFrame = stamp;
            prevColor = currColor;
//            System.out.println("color processed: " + sameColorCounter + " " + modem.demodulate(currColor));
            sameColorCounter = 0;
            return frame;

        }else {

            prevColor = currColor;
            return null;
        }

    }


}
