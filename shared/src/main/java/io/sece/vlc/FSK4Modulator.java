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
    public String demodulate(int hue) {
        Color value = getClosestElement(hue);
        if (value == n) {
            return symbol.toBits(0);
        } else if( value == e) {
            return symbol.toBits(1);
        } else if( value == s) {
            return symbol.toBits(2);
        } else if( value == w) {
            return symbol.toBits(3);
        }
        throw new IllegalArgumentException();
    }


    private Color getClosestElement(int value) {
        int eDistance = Math.min(Math.abs(value - (e.hue + 360)), Math.abs(value - e.hue));
        int sDistance = Math.min(Math.abs(value - (s.hue + 360)), Math.abs(value - s.hue));
        int wDistance = Math.min(Math.abs(value - (w.hue + 360)), Math.abs(value - w.hue));
        //int yellowDistance = Math.min(Math.abs(value - (Color.YELLOW_HUE + 360)), Math.abs(value - Color.YELLOW_HUE));

        /*if(eDistance < sDistance && eDistance < wDistance && eDistance < yellowDistance)
        {
            return Color.RED;
        }
        else if(sDistance < wDistance && sDistance < yellowDistance)
        {
            return Color.GREEN;
        }
        else if(wDistance < yellowDistance)
        {
            return Color.BLUE;
        }
        else
        {
            return Color.YELLOW;
        }*/
        if(value == -1)
        {
            return Color.BLACK;
        }
        if(eDistance < sDistance && eDistance < wDistance)
        {
            return e;
        }
        else if(sDistance < wDistance)
        {
            return s;
        }
        else
        {
            return w;
        }
    }
}
