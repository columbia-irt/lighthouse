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
    public String demodulate(Color input) {
        input = detect(input);
        if (input.equals(u)) return symbol.toBits(0);
        else return symbol.toBits(1);
    }
}
