package io.sece.vlc.rcvr;

import android.util.Log;

import com.google.common.eventbus.Subscribe;

import io.sece.vlc.Color;
import io.sece.vlc.BitString;
import io.sece.vlc.DataFrame;
import io.sece.vlc.Modem;
import io.sece.vlc.RaptorQDecoder;
import io.sece.vlc.rcvr.processing.Frame;
import io.sece.vlc.rcvr.processing.Processing;


public class Receiver {
    private static final String TAG = "Receiver";
    private Modem<Color> modem;
    private DataFrame dataFrame;
    private RaptorQDecoder decoder;
    private int frameErrors;
    private int frameTotal;


    public Receiver(Modem modem) {
        this.modem = modem;
        reset();
    }


    private void updateCounters(int frameTotal, int frameErrors) {
        this.frameTotal = frameTotal;
        this.frameErrors = frameErrors;
        Bus.send(new Bus.FrameStats(this.frameTotal, this.frameErrors));
    }


    public void reset() {
        dataFrame = new DataFrame(modem);
        Bus.send(new Bus.FrameUpdate(dataFrame.getCurrentData()));

        decoder = new RaptorQDecoder(BitString.DEFAULT_DATA.length, DataFrame.MAX_PAYLOAD_SIZE);
        Bus.send(new Bus.ProgressUpdate(decoder.percentCompleted()));

        updateCounters(0, 0);
    }


    public void stop() {
        Bus.unsubscribe(this);
    }


    public void start() {
        Bus.subscribe(this);
        Bus.send(new Bus.FrameUpdate(dataFrame.getCurrentData()));
        Bus.send(new Bus.ProgressUpdate(decoder.percentCompleted()));
    }


    @Subscribe
    private void rx(Processing.Result ev) {
        Color c = modem.detect(ev.frame.getColorAttr(Frame.HUE));
        boolean complete = dataFrame.rx(c);
        Bus.send(new Bus.FrameUpdate(dataFrame.getCurrentData()));
        if (!complete) return;

        updateCounters(frameTotal + 1, frameErrors);
        Log.d(TAG, "Frame: " + dataFrame.getCurrentData());

        if (dataFrame.errorsDetected()) {
            Log.w(TAG, "Corrupted frame received");
            updateCounters(frameTotal, frameErrors + 1);
            return;
        }

        byte[] payload = dataFrame.getPayload();
        dataFrame.reset();

        if (payload.length < decoder.minPacketSize() || payload.length > decoder.maxPacketSize()) {
            Log.w(TAG, "Invalid payload size " + payload.length);
            updateCounters(frameTotal, frameErrors + 1);
            return;
        }

        try {
            decoder.putPacket(payload);
            Log.d(TAG, "Progress: " + decoder.percentCompleted());
        } catch(Exception e) {
            Log.w(TAG, "Error while decoding frame payload", e);
            updateCounters(frameTotal, frameErrors + 1);
            return;
        }
        Bus.send(new Bus.ProgressUpdate(decoder.percentCompleted()));

        if (decoder.hasCompleted()) {
            stop();
            Bus.send(new Bus.TransferCompleted(decoder.getData()));
        }
    }
}
