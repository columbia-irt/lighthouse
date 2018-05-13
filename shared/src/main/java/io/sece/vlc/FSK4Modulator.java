package io.sece.vlc;

import java.util.BitSet;


public class FSK4Modulator extends FreqModulator {
    private Color n;
    private Color e;
    private Color s;
    private Color w;
    private Symbol symbol;

    public FSK4Modulator() {
        this(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);
    }

    public FSK4Modulator(Color n, Color e, Color s, Color w) {
        this.n = n;
        this.e = e;
        this.s = s;
        this.w = w;
        symbol = new Symbol(4);
        bits = symbol.bits;
    }

    @Override
    public Color modulate(BitSet data, int offset) {
        switch(symbol.fromBits(data, offset)) {
        case 0: return n;
        case 1: return e;
        case 2: return s;
        case 3: return w;
        }
        throw new AssertionError();
    }

    @Override
    public BitSet demodulate(BitSet data, int offset, Color value) {
        // Not yet implemented
        throw new UnsupportedOperationException();
    }
}
