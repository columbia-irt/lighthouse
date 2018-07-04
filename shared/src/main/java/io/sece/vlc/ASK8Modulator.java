package io.sece.vlc;



public class ASK8Modulator extends AmpModulator {
    private Amplitude l1, l2, l3, l4, l5, l6, l7, l8;
    private Symbol symbol;

    public ASK8Modulator() {
        this(0, 36, 72, 108, 144, 180, 216, 252);
    }

    public ASK8Modulator(int l1, int l2, int l3, int l4, int l5, int l6, int l7, int l8) {
        this.l1 = new Amplitude(l1);
        this.l2 = new Amplitude(l2);
        this.l3 = new Amplitude(l3);
        this.l4 = new Amplitude(l4);
        this.l5 = new Amplitude(l5);
        this.l6 = new Amplitude(l6);
        this.l7 = new Amplitude(l7);
        this.l8 = new Amplitude(l8);
        states = 8;
        symbol = new Symbol(states);
        bits = symbol.bits;
    }

    @Override
    public Amplitude modulate(String data, int offset) {
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
    public Amplitude detect(Amplitude input) {
        return nearestNeighbor(input, l1, l2, l3, l4, l5, l6, l7, l8);
    }

    @Override
    public String demodulate(Amplitude input) {
        throw new UnsupportedOperationException();
    }
}
