package io.sece.vlc.modem;


import io.sece.vlc.Color;
import io.sece.vlc.FreqModem;
import io.sece.vlc.Symbol;

public class FSK4Modem extends FreqModem {
    private Color n, e, s, w;
    private Symbol symbol;


    public FSK4Modem(int offset) {
        this(Color.BLACK, new Color((((0 * 120) + offset)%360)), new Color((((1 * 120) + offset)%360)), new Color((((2 * 120) + offset)%360)));
    }

    public FSK4Modem()
    {
        this(0);
    }

    public FSK4Modem(Color n, Color e, Color s, Color w) {
        this.n = n;
        this.e = e;
        this.s = s;
        this.w = w;
        //System.out.println("n (rgb) : " + n);
        //System.out.println("e (rgb) : " + e);
        //System.out.println("s (rgb) : " + s);
        //System.out.println("w (rgb) : " + w);
        states = 4;
        symbol = new Symbol(states);
        bits = symbol.bits;
    }

    @Override
    public Color modulate(String data, int offset) {
        switch(symbol.fromBits(data, offset)) {
        case 0: return n;
        case 1: return e;
        case 2: return s;
        case 3: return w;
        }
        throw new AssertionError();
    }

    @Override
    public Color detect(Color input) {
        return input.nearestNeighbor(n, e, s, w);
    }

    @Override
    public StringBuilder demodulate(StringBuilder buf, int offset, Color input) {
        int sym;

        input = detect(input);
        if      (input.equals(n)) sym = 0;
        else if (input.equals(e)) sym = 1;
        else if (input.equals(s)) sym = 2;
        else if (input.equals(w)) sym = 3;
        else
            throw new IllegalArgumentException();

        return symbol.toBits(buf, offset, sym);
    }
}
