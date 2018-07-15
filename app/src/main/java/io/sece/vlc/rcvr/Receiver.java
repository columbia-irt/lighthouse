package io.sece.vlc.rcvr;

import android.util.Log;

import com.google.common.eventbus.Subscribe;

import io.sece.vlc.Color;
import io.sece.vlc.Coordinate;
import io.sece.vlc.BitString;
import io.sece.vlc.DataFrame;
import io.sece.vlc.Modem;
import io.sece.vlc.RaptorQDecoder;
import io.sece.vlc.rcvr.processing.Frame;
import io.sece.vlc.rcvr.processing.Processing;


public class Receiver<T extends Coordinate> {
    private static final String TAG = "Receiver";
    private Modem<Color> modem;
    private DataFrame dataFrame = new DataFrame();
    private RaptorQDecoder decoder = new RaptorQDecoder(BitString.DEFAULT_DATA.length, DataFrame.MAX_PAYLOAD_SIZE);
    private int frameErrors = 0;
    private int frameTotal = 0;


    public Receiver(Modem modem) {
        this.modem = modem;
        Bus.subscribe(this);
    }


    private static int hammingDistance(byte[] a, byte[] b) {
        if (a.length != b.length)
            throw new IllegalArgumentException("a.length != b.length");

        int rv = 0;
        for (int i = 0; i < a.length; i++)
            rv += Integer.bitCount((a[i] & 0xff) ^ (b[i] & 0xff));
        return rv;
    }


    @Subscribe
    private void rx(Processing.Result ev) {
        Color c = ev.frame.getColorAttr(Frame.HUE);

        boolean complete = dataFrame.rx(modem.demodulate(c));
        Bus.send(new Bus.FrameUpdate(dataFrame.getCurrentData()));
        if (!complete) return;

        frameTotal++;
        Log.d(TAG, "Frame: " + dataFrame.getCurrentData());

        if (dataFrame.errorsDetected()) {
            Log.w(TAG, "Frame errors detected");
            frameErrors++;
            Bus.send(new Bus.FrameStats(frameTotal, frameErrors));
            return;
        }
        Bus.send(new Bus.FrameStats(frameTotal, frameErrors));

        byte[] payload = dataFrame.getPayload();
        dataFrame.reset();

        if (payload.length < decoder.minPacketSize() || payload.length > decoder.maxPacketSize()) {
            Log.w(TAG, "Invalid payload size " + payload.length);
            frameErrors++;
            return;
        }

        try {
            decoder.putPacket(payload);
            Log.d(TAG, "Progress: " + decoder.percentCompleted());
        } catch(Exception e) {
            Log.w(TAG, "Error while decoding frame payload", e);
            frameErrors++;
            return;
        }
        Bus.send(new Bus.ProgressUpdate(decoder.percentCompleted()));

        if (!decoder.hasCompleted()) return;

        String msg;
        if (hammingDistance(BitString.DEFAULT_DATA, decoder.getData()) == 0)
            msg = "Transfer completed successfully";
        else
            msg = "Transfer failed";

        Bus.send(new Bus.TransferCompleted(msg));
    }
}
