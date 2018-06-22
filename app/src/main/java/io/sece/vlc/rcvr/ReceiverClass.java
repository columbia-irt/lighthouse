package io.sece.vlc.rcvr;

import io.sece.vlc.Modulator;

/**
 * Created by alex on 6/22/18.
 */

public class ReceiverClass<T> {
    private Modulator<T> modulator;
    public ReceiverClass(Modulator modulator){
        this.modulator = modulator;
    }

    public String rx(T value) throws InterruptedException{
        return modulator.demodulate(value);
    }
    public T getClosestElement(int value){
        return modulator.getClosestElement(value);
    }

    public void setModulator(Modulator<T> modulator) {
        this.modulator = modulator;
    }
}
