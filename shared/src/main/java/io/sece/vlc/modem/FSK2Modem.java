package io.sece.vlc.modem;


import io.sece.vlc.Color;
import io.sece.vlc.FreqModem;


public class FSK2Modem extends FreqModem {
    private Color u;
    private Color d;

    public FSK2Modem() {
        this(Color.RED, Color.GREEN);
    }

    public FSK2Modem(Color u, Color d) {
        super(2);
        this.u = u;
        this.d = d;
    }

    @Override
    public Color modulate(int symbol) {
        switch(symbol) {
        case 0: return u;
        case 1: return d;
        }
        throw new IllegalArgumentException("Bug: Invalid symbol " + symbol);
    }

    @Override
    public Color detect(Color input) {
        return input.nearestNeighbor(u, d);
    }

    @Override
    public int demodulate(Color input) {
        input = detect(input);
        if (input.equals(u)) return 0;
        else return 1;
    }
}
