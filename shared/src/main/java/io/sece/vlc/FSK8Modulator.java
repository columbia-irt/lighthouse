package io.sece.vlc;



public class FSK8Modulator extends FreqModulator {
    private Symbol symbol;
    private Color n;
    private Color ne;
    private Color e;
    private Color se;
    private Color s;
    private Color sw;
    private Color w;
    private Color nw;

    public FSK8Modulator(Color n, Color ne, Color e, Color se, Color s, Color sw, Color w, Color nw) {
        this.n = n;
        this.ne = ne;
        this.e = e;
        this.se = se;
        this.s = s;
        this.sw = sw;
        this.w = w;
        this.nw = nw;

        System.out.println("n (rgb) : " + n.red + "," + n.green + "," + n.blue);
        System.out.println("ne (rgb) : " + ne.red + "," + ne.green + "," + ne.blue);
        System.out.println("e (rgb) : " + e.red + "," + e.green + "," + e.blue);
        System.out.println("se (rgb) : " + se.red + "," + se.green + "," + se.blue);
        System.out.println("s (rgb) : " + s.red + "," + s.green + "," + s.blue);
        System.out.println("sw (rgb) : " + sw.red + "," + sw.green + "," + sw.blue);
        System.out.println("w (rgb) : " + w.red + "," + w.green + "," + w.blue);
        System.out.println("ne (rgb) : " + ne.red + "," + ne.green + "," + ne.blue);
        states = 8;
        symbol = new Symbol(states);
        bits = symbol.bits;
    }


    public FSK8Modulator(int offset)
    {
        this(new Color((((0 * 45) + offset)%360)), new Color((((1 * 45) + offset)%360)), new Color((((2 * 45) + offset)%360)), new Color((((3 * 45) + offset)%360)), new Color((((4 * 45) + offset)%360)),new Color((((5 * 45) + offset)%360)) ,new Color((((6 * 45) + offset)%360)) ,new Color((((7 * 45) + offset)%360)));
    }

    public FSK8Modulator()
    {
        this(0);
    }


    @Override
    public Color modulate(String data, int offset) {
        switch(symbol.fromBits(data, offset)) {
            case 0: return n;
            case 1: return ne;
            case 2: return e;
            case 3: return se;
            case 4: return s;
            case 5: return sw;
            case 6: return w;
            case 7: return nw;
        }
        throw new AssertionError();
    }


    @Override
    public String demodulate(Color value) {
        value = nearestNeighbor(value, n, e, s, w, ne, se, sw, nw);

        if (value.equals(n)) return symbol.toBits(0);
        if (value.equals(e)) return symbol.toBits(2);
        if (value.equals(s)) return symbol.toBits(4);
        if (value.equals(w)) return symbol.toBits(6);

        if (value.equals(ne)) return symbol.toBits(1);
        if (value.equals(se)) return symbol.toBits(3);
        if (value.equals(sw)) return symbol.toBits(5);
        if (value.equals(nw)) return symbol.toBits(7);

        throw new IllegalArgumentException();
    }
}
