package io.sece.vlc.modem;

import io.sece.vlc.AmpModem;
import io.sece.vlc.Amplitude;


public class ASK2Modem extends AmpModem {
    private Amplitude l1, l2;

    public ASK2Modem() {
        this(0, 255);
    }

    public ASK2Modem(int l1, int l2) {
        super(2);
        this.l1 = new Amplitude(l1);
        this.l2 = new Amplitude(l2);
    }

    @Override
    public Amplitude detect(Amplitude input) {
        return input.nearestNeighbor(l1, l2);
    }

    @Override
    public Amplitude modulate(int symbol) {
        switch (symbol) {
            case 0: return l1;
            case 1: return l2;
        }
        throw new IllegalArgumentException("Bug: Invalid symbol " + symbol);
    }

    @Override
    public int demodulate(Amplitude input) {
        throw new UnsupportedOperationException();
    }
}
