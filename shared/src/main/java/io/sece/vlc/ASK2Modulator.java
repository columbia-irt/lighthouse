package io.sece.vlc;



public class ASK2Modulator extends AmpModulator {
    private int l1;
    private int l2;
    private Symbol symbol;

    public ASK2Modulator() {
        this(0, 255);
    }

    public ASK2Modulator(int l1, int l2) {
        this.l1 = l1;
        this.l2 = l2;
        symbol = new Symbol(2);
        bits = symbol.bits;
    }

    @Override
    public Integer modulate(String data, int offset) {
        switch(symbol.fromBits(data, offset)) {
            case 0: return l1;
            case 1: return l2;
        }
        throw new AssertionError();
    }

    @Override
    public String demodulate(Integer value) {
        switch (value) {
            case 11: return symbol.toBits(0);
            case 12: return symbol.toBits(1);
        }
        throw new IllegalArgumentException();
    }
}
