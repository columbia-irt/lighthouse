package io.sece.vlc;

import java.util.BitSet;


public class FSK2Modulator extends FreqModulator {
    private Color u;
    private Color d;
    private Symbol symbol;

    public FSK2Modulator() {
        this(Color.RED, Color.BLUE);
    }

    public FSK2Modulator(Color u, Color d) {
        this.u = u;
        this.d = d;
        symbol = new Symbol(2);
        bits = symbol.bits;
    }

    @Override
    public Color modulate(BitSet data, int offset) {
        switch(symbol.fromBits(data, offset)) {
        case 0: return u;
        case 1: return d;
        }
        throw new AssertionError();
    }

    @Override
    public BitSet demodulate(BitSet data, int offset, Color value) {
        // Not yet implemented
        throw new UnsupportedOperationException();
    }
}
