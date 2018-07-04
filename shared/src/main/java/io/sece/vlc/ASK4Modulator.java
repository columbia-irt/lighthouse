package io.sece.vlc;



public class ASK4Modulator extends AmpModulator {
    private int l1;
    private int l2;
    private int l3;
    private int l4;
    private Symbol symbol;

    public ASK4Modulator() {
        this(0, 85, 170, 255);
    }

    public ASK4Modulator(int l1, int l2, int l3, int l4) {
        this.l1 = l1;
        this.l2 = l2;
        this.l3 = l3;
        this.l4 = l4;
        states = 4;
        symbol = new Symbol(states);
        bits = symbol.bits;
    }

    @Override
    public Integer modulate(String data, int offset) {
        switch(symbol.fromBits(data, offset)) {
        case 0: return l1;
        case 1: return l2;
        case 2: return l3;
        case 3: return l4;
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
