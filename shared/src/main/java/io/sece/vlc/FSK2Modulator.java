package io.sece.vlc;


public class FSK2Modulator extends FreqModulator {
    private Color u;
    private Color d;
    private Symbol symbol;

    public FSK2Modulator() {
        this(Color.BLACK, Color.RED);
    }

    public FSK2Modulator(Color u, Color d) {

        this.u = u;
        this.d = d;
        symbol = new Symbol(2);
        bits = symbol.bits;
    }

    @Override
    public Color modulate(String data, int offset) {
        switch(symbol.fromBits(data, offset)) {
        case 0: return u;
        case 1: return d;
        }
        throw new AssertionError();
    }

    @Override
    public String demodulate(Color value) {
        if(value == u) {
            return symbol.toBits(0);
        } else if( value == d) {
            return symbol.toBits(1);
        }
        throw new IllegalArgumentException();
    }
}
