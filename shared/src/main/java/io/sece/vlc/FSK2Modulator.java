package io.sece.vlc;


public class FSK2Modulator extends FreqModulator {
    private Color u;
    private Color d;
    private Symbol symbol;

    public FSK2Modulator() {
        this(Color.RED, Color.GREEN);
    }

    public FSK2Modulator(Color u, Color d) {
        this.u = u;
        this.d = d;
        states = 2;
        symbol = new Symbol(states);
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
    public Color detect(Color input) {
        return nearestNeighbor(input, u, d);
    }

    @Override
    public StringBuilder demodulate(StringBuilder buf, int offset, Color input) {
        int sym;

        input = detect(input);
        if (input.equals(u)) sym = 0;
        else sym = 1;

        return symbol.toBits(buf, offset, sym);
    }
}
