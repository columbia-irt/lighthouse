package io.sece.vlc;


public class FSK2Modulator extends FreqModulator {
    private Color u;
    private Color d;
    private Symbol symbol;

    public FSK2Modulator() {
        this(Color.RED, Color.GREEN);
    }

    public FSK2Modulator(Color u, Color d) {

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
    public String demodulate(int hue) {
        Color value = getClosestElement(hue);
        if(value == u) {
            return symbol.toBits(0);
        } else if( value == d) {
            return symbol.toBits(1);
        }
        throw new IllegalArgumentException();
    }

    private Color getClosestElement(int value) {
        int redDistance = Math.min(Math.abs(value - (Color.RED_HUE + 360)), Math.abs(value - Color.RED_HUE));
        int greenDistance = Math.min(Math.abs(value - (Color.GREEN_HUE + 360)), Math.abs(value - Color.GREEN_HUE));

        if(redDistance < greenDistance)
        {
            return Color.RED;
        }
        else
        {
            return Color.GREEN;
        }
    }
}
