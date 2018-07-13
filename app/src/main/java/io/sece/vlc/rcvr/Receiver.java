package io.sece.vlc.rcvr;

import com.google.common.eventbus.Subscribe;

import io.sece.vlc.CRC8;
import io.sece.vlc.Color;
import io.sece.vlc.Coordinate;
import io.sece.vlc.DataBitString;
import io.sece.vlc.FramingBlock;
import io.sece.vlc.Modem;
import io.sece.vlc.RaptorQ;
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
    private RaptorQ raptor;


    public Receiver(Modem modem) {
        this.modem = modem;
        framingBlock = new FramingBlock();
        Bus.subscribe(this);
        raptor = new RaptorQ(DataBitString.dataBitString(DataBitString.DATA_BIT_STRING), 4);
    }



    @Subscribe
    private void rx(Processing.Result ev) {
        Color c = ev.frame.getColorAttr(Frame.HUE);

        String currSymbol  =  modem.demodulate(c);

        String data = (framingBlock.applyRX(currSymbol));
        if(data != null){
            System.out.println("Received Frame " + data);

            byte[] receivedData = DataBitString.dataBitString(data.substring(0, data.length() - 8));
            String receivedCRC = (data.substring(data.length() - 8, data.length()));
            String calcCRC = String.format("%8s", Integer.toBinaryString((int)CRC8.compute(receivedData)).replace(' ', '0'));

            if(calcCRC.equals(receivedCRC)){
                System.out.println("CRC8 correct");
                raptor.putPacket(receivedData);
            }else{
                System.out.println("CRC is incorrect");
            }
            if(raptor.hasCompleted()){
                if(raptor.hammingDistance() == 0)
                {
                    System.out.println("Transmission completed successfully");
                }
                else
                {
                    System.out.println("Unsuccessful transmission completed");
                }
            }
        }

    }

}


