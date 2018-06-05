package io.sece.vlc;



public class ASK8Modulator extends AmpModulator {
    private int l1;
    private int l2;
    private int l3;
    private int l4;
    private int l5;
    private int l6;
    private int l7;
    private int l8;
    private Symbol symbol;

    public ASK8Modulator() {
        this(0, 36, 72, 108, 144, 180, 216, 252);
    }

    public ASK8Modulator(int l1, int l2, int l3, int l4, int l5, int l6, int l7, int l8) {
        this.l1 = l1;
        this.l2 = l2;
        this.l3 = l3;
        this.l4 = l4;
        this.l5 = l5;
        this.l6 = l6;
        this.l7 = l7;
        this.l8 = l8;
        symbol = new Symbol(8);
        bits = symbol.bits;
    }

    @Override
    public Integer modulate(String data, int offset) {
        switch(symbol.fromBits(data, offset)) {
        case 0: return l1;
        case 1: return l2;
        case 2: return l3;
        case 3: return l4;
        case 4: return l5;
        case 5: return l6;
        case 6: return l7;
        case 7: return l8;
        }
        throw new AssertionError();
    }

    @Override
    public String demodulate(Integer value) {
        switch (value)
        {
            case 11:
                return symbol.toBits(0);
            case 12:
                return symbol.toBits(1);
            case 13:
                return symbol.toBits(2);
            case 14:
                return symbol.toBits(3);
            case 15:
                return symbol.toBits(4);
            case 16:
                return symbol.toBits(5);
            case 17:
                return symbol.toBits(6);
            case 18:
                return symbol.toBits(7);
        }
        throw new IllegalArgumentException();
    }
}
