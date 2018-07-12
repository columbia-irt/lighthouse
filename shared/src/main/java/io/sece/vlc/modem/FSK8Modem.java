package io.sece.vlc.modem;


import io.sece.vlc.Color;
import io.sece.vlc.FreqModem;
import io.sece.vlc.Symbol;

public class FSK8Modem extends FreqModem {
    private Symbol symbol;
    private Color n, e, s, w, ne, se, sw, nw;

    public FSK8Modem(Color n, Color ne, Color e, Color se, Color s, Color sw, Color w, Color nw) {
        this.n = n;
        this.ne = ne;
        this.e = e;
        this.se = se;
        this.s = s;
        this.sw = sw;
        this.w = w;
        this.nw = nw;

        //System.out.println("n (rgb) : " + n);
        //System.out.println("ne (rgb) : " + ne);
        //System.out.println("e (rgb) : " + e);
        //System.out.println("se (rgb) : " + se);
        //System.out.println("s (rgb) : " + s);
        //System.out.println("sw (rgb) : " + sw);
        //System.out.println("w (rgb) : " + w);
        //System.out.println("ne (rgb) : " + ne);
        states = 8;
        symbol = new Symbol(states);
        bits = symbol.bits;
    }


    public FSK8Modem(int offset)
    {
        this(new Color((((0 * 45) + offset)%360)), new Color((((1 * 45) + offset)%360)), new Color((((2 * 45) + offset)%360)), new Color((((3 * 45) + offset)%360)), new Color((((4 * 45) + offset)%360)),new Color((((5 * 45) + offset)%360)) ,new Color((((6 * 45) + offset)%360)) ,new Color((((7 * 45) + offset)%360)));
    }

    public FSK8Modem()
    {
        this(0);
    }


    @Override
    public Color modulate(String data, int offset) {
        switch(symbol.fromBits(data, offset)) {
            case 0: return n;
            case 2: return e;
            case 4: return s;
            case 6: return w;

            case 1: return ne;
            case 3: return se;
            case 5: return sw;
            case 7: return nw;
        }
        throw new AssertionError();
    }


    @Override
    public Color detect(Color input) {
        return input.nearestNeighbor(n, e, s, w, ne, se, sw, nw);
    }


    @Override
    public StringBuilder demodulate(StringBuilder buf, int offset, Color input) {
        int sym;

        input = detect(input);

        if      (input.equals(n)) sym = 0;
        else if (input.equals(e)) sym = 2;
        else if (input.equals(s)) sym = 4;
        else if (input.equals(w)) sym = 6;

        else if (input.equals(ne)) sym = 1;
        else if (input.equals(se)) sym = 3;
        else if (input.equals(sw)) sym = 5;
        else if (input.equals(nw)) sym = 7;
        else
            throw new IllegalArgumentException();

        return symbol.toBits(buf, offset, sym);
    }
}
