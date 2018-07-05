package io.sece.vlc;



public class ASK4Modem extends AmpModem {
    private Amplitude l1, l2, l3, l4;
    private Symbol symbol;

    public ASK4Modem() {
        this(0, 85, 170, 255);
    }

    public ASK4Modem(int l1, int l2, int l3, int l4) {
        this.l1 = new Amplitude(l1);
        this.l2 = new Amplitude(l2);
        this.l3 = new Amplitude(l3);
        this.l4 = new Amplitude(l4);
        states = 4;
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
        }
        throw new AssertionError();
    }

    @Override
    public Amplitude detect(Amplitude input) {
        return input.nearestNeighbor(l1, l2, l3, l4);
    }

    @Override
    public StringBuilder demodulate(StringBuilder buf, int offset, Amplitude input) {
        throw new UnsupportedOperationException();
    }
}
