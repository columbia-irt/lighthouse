package io.sece.vlc.rcvr;

import android.util.Log;

import com.google.common.eventbus.Subscribe;

import io.sece.vlc.Color;
import io.sece.vlc.BitString;
import io.sece.vlc.DataFrame;
import io.sece.vlc.LineCoder;
import io.sece.vlc.Modem;
import io.sece.vlc.RaptorQDecoder;
import io.sece.vlc.rcvr.processing.Frame;
import io.sece.vlc.rcvr.processing.Processing;


public class Receiver {
    private static final String TAG = "Receiver";
    private Modem<Color> modem;
    private DataFrame dataFrame = new DataFrame();
    private LineCoder lineCoder;
    private RaptorQDecoder dataDecoder;
    private int frameErrors;
    private int frameTotal;


    public Receiver(Modem modem) {
        this.modem = modem;
        lineCoder = new LineCoder(modem, DataFrame.MAX_SIZE);
        reset();
    }


    public void reset() {
        lineCoder.reset();
        Bus.send(new Bus.FrameUpdate(lineCoder.getCurrentData()));

        dataDecoder = new RaptorQDecoder(BitString.DEFAULT_DATA.length, DataFrame.MAX_PAYLOAD_SIZE);
        Bus.send(new Bus.ProgressUpdate(dataDecoder.percentCompleted()));

        updateCounters(0, 0);
    }


    private void updateCounters(int frameTotal, int frameErrors) {
        this.frameTotal = frameTotal;
        this.frameErrors = frameErrors;
        Bus.send(new Bus.FrameStats(this.frameTotal, this.frameErrors));
    }


    public void stop() {
        Bus.unsubscribe(this);
    }


    public void start() {
        Bus.subscribe(this);
        Bus.send(new Bus.FrameUpdate(lineCoder.getCurrentData()));
        Bus.send(new Bus.ProgressUpdate(dataDecoder.percentCompleted()));
    }


    @Subscribe
    private void rx(Processing.Result ev) {
        Color c = modem.detect(ev.frame.getColorAttr(Frame.HUE));

        BitString frame = lineCoder.rx(c);
        Bus.send(new Bus.FrameUpdate(lineCoder.getCurrentData()));
        if (frame == null) return;

        updateCounters(frameTotal + 1, frameErrors);
        Log.d(TAG, "Frame: " + frame);

        try {
            dataFrame.parse(frame);
        } catch (DataFrame.FrameTooShort e) {
            Log.w(TAG, "Too short frame received");
            updateCounters(frameTotal, frameErrors + 1);
            return;
        }

        if (dataFrame.error) {
            Log.w(TAG, "Corrupted frame received");
            updateCounters(frameTotal, frameErrors + 1);
            return;
        }

        if (dataFrame.payload.length < dataDecoder.minPacketSize()
                || dataFrame.payload.length > dataDecoder.maxPacketSize()) {
            Log.w(TAG, "Invalid payload size " + dataFrame.payload.length);
            updateCounters(frameTotal, frameErrors + 1);
            return;
        }

        try {
            dataDecoder.putPacket(dataFrame.seqNumber, dataFrame.payload);
            Log.d(TAG, "Progress: " + dataDecoder.percentCompleted());
        } catch(Exception e) {
            Log.w(TAG, "Error while decoding frame payload", e);
            updateCounters(frameTotal, frameErrors + 1);
            return;
        }
        Bus.send(new Bus.ProgressUpdate(dataDecoder.percentCompleted()));

        if (dataDecoder.hasCompleted()) {
            stop();
            Bus.send(new Bus.TransferCompleted(dataDecoder.getData()));
        }
    }
}
