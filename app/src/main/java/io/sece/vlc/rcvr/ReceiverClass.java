package io.sece.vlc.rcvr;

import com.google.common.eventbus.Subscribe;

import io.sece.vlc.EuclideanSpace;
import io.sece.vlc.Modem;
import io.sece.vlc.rcvr.processing.Frame;
import io.sece.vlc.rcvr.processing.Processing;

/**
 * Created by alex on 6/22/18.
 *
 * This class contains the basic setup of the Receiver including Modulation, FPS, Transmissionstarting
 */

public class ReceiverClass<T extends EuclideanSpace> {
    private Modem<T> modem;
    private boolean transmissionStarted = false;
    private SynchronizationModule synchronizationModule;
    private int delay = 50;


    public ReceiverClass(Modem modem) {
        this.modem = modem;
        System.out.println("Startingseq: " + modem.startSequence(8));
        synchronizationModule = new SynchronizationModule(modem.startSequence(8), delay);
    }

    public String rx(T value) throws InterruptedException {
        if (transmissionStarted) {
            return modem.demodulate(value);
        } else {
            transmissionStarted = rxStartingSequence(value);
            return "";
        }
    }

    public void setModem(Modem<T> modem) {
        this.modem = modem;
        System.out.println("Startingseq: " + modem.startSequence(8));
        synchronizationModule = new SynchronizationModule(modem.startSequence(8), delay);
    }

    public boolean rxStartingSequence(T value) {
        return synchronizationModule.symbolReceived(modem.demodulate(value));
    }

    public boolean isTransmissionStarted() {
        return transmissionStarted;
    }

    public int getDelay() {
        return delay;
    }


    @Subscribe
    public void rx(Processing.Result ev) {
        long h = ev.frame.get(Frame.HUE);
        long b = ev.frame.get(Frame.BRIGHTNESS);
    }
}
