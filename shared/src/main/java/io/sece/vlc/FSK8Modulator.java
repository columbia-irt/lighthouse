package io.sece.vlc;



public class FSK8Modulator extends FreqModulator {
    private Symbol symbol;

    public FSK8Modulator() {
        symbol = new Symbol(8);
        bits = symbol.bits;
    }

    @Override
    public Color modulate(String data, int offset) {
        switch(symbol.fromBits(data, offset)) {
            case 0: return Color.RED;
            case 1: return Color.GREEN;
            case 2: return Color.BLUE;
            case 3: return Color.YELLOW;
            case 4: return Color.PURPLE;
            case 5: return Color.TURQUOISE;
            case 6: return Color.BLACK;
            case 7: return Color.WHITE;
        }
        throw new AssertionError();
    }

    @Override
    public String demodulate(Color value) {
        if (value == Color.BLACK) {
            return symbol.toBits(0);
        } else if( value == Color.RED) {
            return symbol.toBits(1);
        } else if( value == Color.GREEN) {
            return symbol.toBits(2);
        } else if( value == Color.BLUE) {
            return symbol.toBits(3);
        } else if( value == Color.YELLOW) {
            return symbol.toBits(4);
        } else if( value == Color.PURPLE) {
            return symbol.toBits(5);
        } else if( value == Color.TURQUOISE) {
            return symbol.toBits(6);
        } else if( value == Color.WHITE) {
            return symbol.toBits(7);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public Color getClosestElement(int value) {
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
        }
    }
}
