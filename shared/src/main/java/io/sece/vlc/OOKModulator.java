package io.sece.vlc;

import java.util.BitSet;


/**
 * An implementation of an On-Off Keying (OOK) modulator. This is the simplest
 * modulator that maps binary ones to light and binary zeroes to no light.
 * This modulator extends directly from the Modulator base class because it
 * accepts and generates boolean values so that it can be connected directly
 * to LED interfaces that only accept binary values.
 */
public class OOKModulator extends Modulator<Boolean> {
    private Symbol symbol;

    public OOKModulator() {
    }

    public OOKModulator(int mark, int space) {
        symbol = new Symbol(2);
        bits = symbol.bits;
    }

    @Override
    public Boolean modulate(BitSet data, int offset) {
        switch(symbol.fromBits(data, offset)) {
        case 0: return false;
        case 1: return true;
        }
        throw new AssertionError();
    }

    @Override
    public BitSet demodulate(BitSet data, int offset, Boolean value) {
        // Not yet implemented
        throw new UnsupportedOperationException();
    }
}
