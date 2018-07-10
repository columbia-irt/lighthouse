package io.sece.vlc.rcvr;

import com.google.common.eventbus.Subscribe;

import java.util.ArrayList;

import io.sece.vlc.Color;
import io.sece.vlc.Coordinate;
import io.sece.vlc.Modem;
import io.sece.vlc.rcvr.processing.Frame;
import io.sece.vlc.rcvr.processing.FramingBlock;
import io.sece.vlc.rcvr.processing.Processing;

/**
 * Created by alex on 6/22/18.
 *
 * This class contains the basic setup of the Receiver including Modulation, FPS, Transmissionstarting
 */

public class Receiver<T extends Coordinate> {
    private Modem<Color> modem;
    private boolean transmissionStarted = false;
    private SynchronizationModule synchronizationModule;
    private int delay = 50;
    private FramingBlock framingBlock;



    public Receiver(Modem modem) {
        this.modem = modem;
        framingBlock = new FramingBlock(modem.startSequence(4), modem.bits, 800);
        Bus.subscribe(this);
    }


    public int getDelay() {
        return delay;
    }


    @Subscribe
    private void rx(Processing.Result ev) {
        long h = ev.frame.get(Frame.HUE);
        long b = ev.frame.get(Frame.BRIGHTNESS);

        String currSymbol  =  modem.demodulate(new Color((int)h, (int)b));
//        System.out.println(currSymbol + " " + ev.frame.get(Frame.IMAGE_TIMESTAMP));
        String[] currFrameData = framingBlock.apply(currSymbol);
        if(currFrameData != null){
            for(String temp: currFrameData){
                System.out.println(temp);
            }
            System.out.println("Received FrameArray " + currFrameData.toString() + currFrameData.length);
        }
    }


}
