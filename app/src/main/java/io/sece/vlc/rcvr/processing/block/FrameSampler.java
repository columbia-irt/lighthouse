package io.sece.vlc.rcvr.processing.block;

import com.google.common.eventbus.Subscribe;

import java.util.concurrent.TimeUnit;

import io.sece.vlc.Color;
import io.sece.vlc.Modem;
import io.sece.vlc.rcvr.Bus;
import io.sece.vlc.rcvr.ViewfinderModel;
import io.sece.vlc.rcvr.processing.Frame;
import io.sece.vlc.rcvr.processing.ProcessingBlock;



public class FrameSampler implements ProcessingBlock {

    private long interval;  // Sampling interval in nano seconds
    private long lastAcceptedFrame = 0;
    private Modem<Color> modem;
    private Color prevColor;
    private int sameColorCounter = 0;

    public FrameSampler(int baudRate, Modem modem) {
        this.interval = 1000000000 / baudRate;
        this.modem = modem;
        Bus.subscribe(this);
    }


    @Subscribe
    public void onBaudRateChange(Bus.BaudRateChange event) {
        this.interval = 1000000000 / event.baudRate;
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

        } else {
            prevColor = currColor;
            return null;
        }

    }
}
