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
        states = 2;
        symbol = new Symbol(states);
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
        throw new IllegalArgumentException();
    }


    private Integer getClosestElement(int value) {
        throw new UnsupportedOperationException();
    }
}
