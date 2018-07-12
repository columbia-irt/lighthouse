package io.sece.vlc.modem;


import io.sece.vlc.Color;
import io.sece.vlc.FreqModem;
import io.sece.vlc.Symbol;

public class FSK2Modem extends FreqModem {
    private Color u;
    private Color d;
    private Symbol symbol;

    public FSK2Modem() {
        this(Color.RED, Color.GREEN);
    }

    public FSK2Modem(Color u, Color d) {
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
        return input.nearestNeighbor(u, d);
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
