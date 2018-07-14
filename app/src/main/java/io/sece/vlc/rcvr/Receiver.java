package io.sece.vlc.rcvr;

import com.google.common.eventbus.Subscribe;

import io.sece.vlc.CRC8;
import io.sece.vlc.Color;
import io.sece.vlc.Coordinate;
import io.sece.vlc.BitString;
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


    public static class Event extends Bus.Event {
        public String bits;

        public Event(String bits) {
            this.bits = bits;
        }
    }


    public Receiver(Modem modem) {
        this.modem = modem;
        framingBlock = new FramingBlock();
        Bus.subscribe(this);
        raptor = new RaptorQ(BitString.DEFAULT_DATA, 4);
    }



    @Subscribe
    private void rx(Processing.Result ev) {
        Color c = ev.frame.getColorAttr(Frame.HUE);

        String currSymbol  =  modem.demodulate(c);
        String data = (framingBlock.applyRX(currSymbol));
        Bus.send(new Receiver.Event(framingBlock.rx_bits));
        if(data != null){
            System.out.println("Received Frame " + data);
            byte[] receivedData = BitString.toBytes(data);

            if (receivedData.length < 16) {
                System.out.println("Frame too short");
                return;
            }

            int receivedCRC = receivedData[0] & 0xff;
            int calcCRC = CRC8.compute(receivedData, 1, receivedData.length - 1);

            if (calcCRC == receivedCRC) {
                System.out.println("CRC8 correct");
                Bus.send(new Receiver.Event("crc correct"));
                raptor.putPacket(receivedData, 1);
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


