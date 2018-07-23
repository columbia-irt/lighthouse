package io.sece.vlc.modem;


import io.sece.vlc.Color;
import io.sece.vlc.FreqModem;


public class FSK4Modem extends FreqModem {
    private Color n, e, s, w;

    public FSK4Modem()
    {
        this(0);
    }

    public FSK4Modem(int offset) {
        this(Color.BLACK, new Color((((0 * 120) + offset) % 360)), new Color((((1 * 120) + offset) % 360)), new Color((((2 * 120) + offset) % 360)));
    }

    public FSK4Modem(Color n, Color e, Color s, Color w) {
        super(4);
        this.n = n;
        this.e = e;
        this.s = s;
        this.w = w;
    }

    @Override
    public Color modulate(int symbol) {
        switch(symbol) {
        case 0: return n;
        case 1: return e;
        case 2: return s;
        case 3: return w;
        }
        throw new IllegalArgumentException("Bug: Invalid symbol " + symbol);
    }

    @Override
    public Color detect(Color input) {
        return input.nearestNeighbor(n, e, s, w);
    }

    @Override
    public int demodulate(Color input) {
        input = detect(input);
        if      (input.equals(n)) return 0;
        else if (input.equals(e)) return 1;
        else if (input.equals(s)) return 2;
        else if (input.equals(w)) return 3;
        else
            throw new IllegalArgumentException("Bug in FSK4 demodulator");
    }
}
