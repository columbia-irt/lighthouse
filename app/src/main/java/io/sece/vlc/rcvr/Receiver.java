package io.sece.vlc.rcvr;

import android.util.Log;

import com.google.common.eventbus.Subscribe;

import java.util.List;

import io.sece.vlc.Color;
import io.sece.vlc.BitVector;
import io.sece.vlc.DataFrame;
import io.sece.vlc.LineCoder;
import io.sece.vlc.Modem;
import io.sece.vlc.RaptorQDecoder;
import io.sece.vlc.Symbol;
import io.sece.vlc.rcvr.processing.Frame;
import io.sece.vlc.rcvr.processing.Processing;


public class Receiver {
    private static final String TAG = "Receiver";
    private Modem<Color> modem;
    private DataFrame dataFrame = new DataFrame();
    private LineCoder lineCoder;
    private Symbol symbol;
    private RaptorQDecoder dataDecoder;


    public Receiver(Modem modem) {
        this.modem = modem;
        symbol = new Symbol(modem.states);
        lineCoder = new LineCoder(new int[] {1,3, 2}, DataFrame.MAX_SIZE * 8 / symbol.bits);
        reset();
    }


    public void reset() {
        lineCoder.reset();
        dataDecoder = new RaptorQDecoder(BitVector.DEFAULT_DATA.length / 8, DataFrame.MAX_PAYLOAD_SIZE);
        Bus.send(new Bus.ProgressUpdate(dataDecoder.percentCompleted()));
    }


    public void stop() {
        Bus.unsubscribe(this);
    }


    public void start() {
        Bus.subscribe(this);
        Bus.send(new Bus.ProgressUpdate(dataDecoder.percentCompleted()));
    }


    @Subscribe
    private void rx(Processing.Result ev) {
        int s = modem.demodulate(ev.frame.getColorAttr(Frame.HUE));

        List<Integer> symbols;
        try {
            symbols = lineCoder.decode(s);
        } catch (LineCoder.FrameTooLong e) {
            Log.d(TAG, "Frame too long");
            return;
        }

        if (symbols == null || symbols.size() == 0) return;

        BitVector frame = symbol.toBits(symbols);

        try {
            dataFrame.unpack(frame);
        } catch (DataFrame.FrameTooShort e) {
            Log.d(TAG, "Frame too short");
            return;
        }

        if (dataFrame.error) {
            Log.d(TAG, "Frame corrupted");
            return;
        }

        if (dataFrame.payload.length < dataDecoder.minPacketSize()
                || dataFrame.payload.length > dataDecoder.maxPacketSize()) {
            Log.d(TAG, "Invalid payload size " + dataFrame.payload.length);
            return;
        }

        try {
            dataDecoder.putPacket(dataFrame.seqNumber, dataFrame.payload);
        } catch(Exception e) {
            Log.d(TAG, "Error while decoding payload", e);
            return;
        }
        Bus.send(new Bus.ProgressUpdate(dataDecoder.percentCompleted()));

        if (dataDecoder.hasCompleted()) {
            stop();
            Bus.send(new Bus.TransferCompleted(dataDecoder.getData()));
        }
    }
}
