package io.sece.vlc.rcvr;

import io.sece.vlc.Modulator;
import io.sece.vlc.rcvr.modules.SynchronizationModule;

/**
 * Created by alex on 6/22/18.
 *
 * This class contains the basic setup of the Receiver including Modulation, FPS, Transmissionstarting
 */

public class ReceiverClass<T> {
    private Modulator<T> modulator;
    private boolean transmissionStarted = false;
    private SynchronizationModule synchronizationModule;
    private int delay = 1000;

    public ReceiverClass(Modulator modulator){
        this.modulator = modulator;
        System.out.println("Startingseq: " + modulator.startSequence(8));
        synchronizationModule = new SynchronizationModule(modulator.startSequence(8), delay);
    }

    public String rx(T value) throws InterruptedException{
        if(transmissionStarted){
            return modulator.demodulate(value);
        }else{
//            transmissionStarted =
                    rxStartingSequence(value);
            return "";
        }
    }

    public T getClosestElement(int value){
        return modulator.getClosestElement(value);
    }

    public void setModulator(Modulator<T> modulator) {
        this.modulator = modulator;
        System.out.println("Startingseq: " + modulator.startSequence(8));
        synchronizationModule = new SynchronizationModule(modulator.startSequence(8), delay);
    }

    public boolean rxStartingSequence(T value){
        return synchronizationModule.symbolReceived(modulator.demodulate(value));
    }
    public boolean isTransmissionStarted(){
        return transmissionStarted;
    }

    public int getDelay(){
        return delay;
    }
}
