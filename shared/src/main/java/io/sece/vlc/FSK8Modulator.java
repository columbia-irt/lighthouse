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
    public String demodulate(int hue) {
        Color value = getClosestElement(hue);
        if( value == n) {
            return symbol.toBits(0);
        } else if( value == ne) {
            return symbol.toBits(1);
        } else if( value == e) {
            return symbol.toBits(2);
        } else if( value == se) {
            return symbol.toBits(3);
        } else if( value == s) {
            return symbol.toBits(4);
        } else if( value == sw) {
            return symbol.toBits(5);
        } else if (value == w) {
            return symbol.toBits(6);
        } else if( value == nw) {
            return symbol.toBits(7);
        }
        throw new IllegalArgumentException();
    }

    private Color getClosestElement(int value) {

        int nDistance = Math.min(Math.abs(value - (n.hue + 360)), Math.abs(value - n.hue));
        int neDistance = Math.min(Math.abs(value - (ne.hue + 360)), Math.abs(value - ne.hue));
        int eDistance = Math.min(Math.abs(value - (e.hue + 360)), Math.abs(value - e.hue));
        int seDistance = Math.min(Math.abs(value - (se.hue + 360)), Math.abs(value - se.hue));
        int sDistance = Math.min(Math.abs(value - (s.hue + 360)), Math.abs(value - s.hue));
        int swDistance = Math.min(Math.abs(value - (sw.hue + 360)), Math.abs(value - sw.hue));
        int wDistance = Math.min(Math.abs(value - (w.hue + 360)), Math.abs(value - w.hue));
        int nwDistance = Math.min(Math.abs(value - (nw.hue + 360)), Math.abs(value - nw.hue));

        if(nDistance < neDistance && nDistance < eDistance && nDistance < seDistance && nDistance < sDistance && nDistance < swDistance && nDistance < wDistance && nDistance < nwDistance)
        {
            return n;
        }
        else if(neDistance < eDistance && neDistance < seDistance && neDistance < sDistance && neDistance < swDistance && neDistance < wDistance && neDistance < nwDistance)
        {
            return ne;
        }
        else if(eDistance < seDistance && eDistance < sDistance && eDistance < swDistance && eDistance < wDistance && eDistance < nwDistance)
        {
            return e;
        }
        else if(seDistance < sDistance && seDistance < swDistance && seDistance < wDistance && seDistance < nwDistance)
        {
            return se;
        }
        else if(sDistance < swDistance && sDistance < wDistance && sDistance < nwDistance)
        {
            return s;
        }
        else if(swDistance < wDistance && swDistance < nwDistance)
        {
            return sw;
        }
        else if(wDistance < nwDistance)
        {
            return w;
        }
        else
        {
            return nw;
        }

        /*
        if(value == -1)
        {
            return Color.BLACK;
        }
        if(value == -2)
        {
            return Color.WHITE;
        }

        int redDistance = Math.min(Math.abs(value - (Color.RED_HUE + 360)), Math.abs(value - Color.RED_HUE));
        int greenDistance = Math.min(Math.abs(value - (Color.GREEN_HUE + 360)), Math.abs(value - Color.GREEN_HUE));
        int blueDistance = Math.min(Math.abs(value - (Color.BLUE_HUE + 360)), Math.abs(value - Color.BLUE_HUE));
        int yellowDistance = Math.min(Math.abs(value - (Color.YELLOW_HUE + 360)), Math.abs(value - Color.YELLOW_HUE));
        int purpleDistance = Math.min(Math.abs(value - (Color.PURPLE_HUE + 360)), Math.abs(value - Color.PURPLE_HUE));
        int turquoiseDistance = Math.min(Math.abs(value - (Color.TURQUOISE_HUE + 360)), Math.abs(value - Color.TURQUOISE_HUE));

        if(redDistance < greenDistance && redDistance < blueDistance && redDistance < yellowDistance && redDistance < purpleDistance && redDistance < turquoiseDistance)
        {
            return Color.RED;
        }
        else if(greenDistance < blueDistance && greenDistance < yellowDistance && greenDistance < purpleDistance && greenDistance < turquoiseDistance)
        {
            return Color.GREEN;
        }
        else if(blueDistance < yellowDistance && blueDistance < purpleDistance && blueDistance < turquoiseDistance)
        {
            return Color.BLUE;
        }
        else if(yellowDistance < purpleDistance && yellowDistance < turquoiseDistance)
        {
            return Color.YELLOW;
        }
        else if(purpleDistance < turquoiseDistance)
        {
            return Color.PURPLE;
        }
        else
        {
            return Color.TURQUOISE;
        }*/
    }
}
