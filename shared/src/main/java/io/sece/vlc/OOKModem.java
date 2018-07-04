package io.sece.vlc;


/**
 * An implementation of an On-Off Keying (OOK) modulator. This is the simplest
 * modulator that maps binary ones to light and binary zeroes to no light.
 * This modulator extends directly from the Modulator base class because it
 * accepts and generates boolean values so that it can be connected directly
 * to LED interfaces that only accept binary values.
 */
public class OOKModem extends Modem<Amplitude> {
    private Symbol symbol;
    private Amplitude off = new Amplitude(0);
    private Amplitude on = new Amplitude(1);

    public OOKModem() {
        states = 2;
        symbol = new Symbol(states);
        bits = symbol.bits;
    }

    @Override
    public Amplitude modulate(String data, int offset) {
        switch(symbol.fromBits(data, offset)) {
        case 0: return off;
        case 1: return on;
        }
        throw new AssertionError();
    }

    @Override
    public Amplitude detect(Amplitude input) {
        return nearestNeighbor(input, on, off);
    }

    public StringBuilder demodulate(StringBuilder buf, int offset, Amplitude input) {
        // Not yet implemented
        throw new UnsupportedOperationException();
    }
}
