package io.sece.vlc.modem;


import io.sece.vlc.Amplitude;
import io.sece.vlc.Modem;


/**
 * An implementation of an On-Off Keying (OOK) modulator. This is the simplest
 * modulator that maps binary ones to light and binary zeroes to no light.
 * This modulator extends directly from the Modulator base class because it
 * accepts and generates boolean values so that it can be connected directly
 * to LED interfaces that only accept binary values.
 */
public class OOKModem extends Modem<Amplitude> {
    private Amplitude off = new Amplitude(0);
    private Amplitude on = new Amplitude(1);

    public OOKModem() {
        super(2);
    }

    @Override
    public Amplitude modulate(int symbol) {
        switch(symbol) {
        case 0: return off;
        case 1: return on;
        }
        throw new IllegalArgumentException("Bug: Invalid symbol " + symbol);
    }

    @Override
    public Amplitude detect(Amplitude input) {
        return input.nearestNeighbor(on, off);
    }

    @Override
    public int demodulate(Amplitude input) {
        // Not yet implemented
        throw new UnsupportedOperationException();
    }
}
