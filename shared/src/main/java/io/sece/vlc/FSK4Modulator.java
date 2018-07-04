package io.sece.vlc;



public class FSK4Modulator extends FreqModulator {
    private Color n;
    private Color e;
    private Color s;
    private Color w;
    private Symbol symbol;


    public FSK4Modulator(int offset)
    {
        this(Color.BLACK, new Color((((0 * 120) + offset)%360)), new Color((((1 * 120) + offset)%360)), new Color((((2 * 120) + offset)%360)));
    }

    public FSK4Modulator()
    {
        this(0);
    }

    public FSK4Modulator(Color n, Color e, Color s, Color w) {
        this.n = n;
        this.e = e;
        this.s = s;
        this.w = w;
        System.out.println("n (rgb) : " + n.red + "," + n.green + "," + n.blue);
        System.out.println("e (rgb) : " + e.red + "," + e.green + "," + e.blue);
        System.out.println("s (rgb) : " + s.red + "," + s.green + "," + s.blue);
        System.out.println("w (rgb) : " + w.red + "," + w.green + "," + w.blue);
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
    public String demodulate(Color value) {
        value = nearestNeighbor(value, n, e, s, w);

        if (value.equals(n)) return symbol.toBits(0);
        if (value.equals(e)) return symbol.toBits(1);
        if (value.equals(s)) return symbol.toBits(2);
        if (value.equals(w)) return symbol.toBits(3);
        throw new IllegalArgumentException();
    }
}
