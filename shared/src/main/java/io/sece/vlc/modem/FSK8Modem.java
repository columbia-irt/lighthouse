package io.sece.vlc.modem;


import io.sece.vlc.Color;
import io.sece.vlc.FreqModem;
import io.sece.vlc.Symbol;


public class FSK8Modem extends FreqModem {
    private static final int N = 0;
    private static final int E = 1;
    private static final int S = 2;
    private static final int W = 3;
    private static final int NE = 4;
    private static final int SE = 5;
    private static final int SW = 6;
    private static final int NW = 7;

    private Symbol symbol;
    private Color[] color = new Color[8];


    public FSK8Modem() {
        states = 8;
        symbol = new Symbol(states);
        bits = symbol.bits;

        color[N] = Color.BLACK;

        for (int i = 1; i < states; i++)
            color[i] = new Color(Math.round(i * 360 / 7));
    }


    @Override
    public Color modulate(String data, int offset) {
        switch(symbol.fromBits(data, offset)) {
            case 0: return color[N];
            case 2: return color[E];
            case 4: return color[S];
            case 6: return color[W];

            case 1: return color[NE];
            case 3: return color[SE];
            case 5: return color[SW];
            case 7: return color[NW];
        }
        throw new AssertionError();
    }


    @Override
    public Color detect(Color input) {
        return input.nearestNeighbor(color[N], color[E], color[S], color[W], color[NE], color[SE], color[SW], color[NW]);
    }


    @Override
    public StringBuilder demodulate(StringBuilder buf, int offset, Color input) {
        int sym;

        input = detect(input);

        if      (input.equals(color[N])) sym = 0;
        else if (input.equals(color[E])) sym = 2;
        else if (input.equals(color[S])) sym = 4;
        else if (input.equals(color[W])) sym = 6;

        else if (input.equals(color[NE])) sym = 1;
        else if (input.equals(color[SE])) sym = 3;
        else if (input.equals(color[SW])) sym = 5;
        else if (input.equals(color[NW])) sym = 7;
        else
            throw new IllegalArgumentException();

        return symbol.toBits(buf, offset, sym);
    }
}
