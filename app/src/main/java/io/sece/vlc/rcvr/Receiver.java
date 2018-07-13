package io.sece.vlc.rcvr;

import com.google.common.eventbus.Subscribe;

import io.sece.vlc.Color;
import io.sece.vlc.Coordinate;
import io.sece.vlc.FramingBlock;
import io.sece.vlc.Modem;
import io.sece.vlc.rcvr.processing.Frame;
import io.sece.vlc.rcvr.processing.Processing;

/**
 * Created by alex on 6/22/18.
 *
 * This class contains the basic setup of the Receiver including Modulation, FPS, Transmissionstarting
 */

public class Receiver<T extends Coordinate> {
    private Modem<Color> modem;
    private FramingBlock framingBlock;



    public Receiver(Modem modem) {
        this.modem = modem;
        framingBlock = new FramingBlock();
        Bus.subscribe(this);
    }



    @Subscribe
    private void rx(Processing.Result ev) {
        Color c = ev.frame.getColorAttr(Frame.HUE);

        String currSymbol  =  modem.demodulate(c);

        String data = (framingBlock.applyRX(currSymbol));
        if(data != null){
            System.out.println("Received Frame " + data);
        }
    }


}
