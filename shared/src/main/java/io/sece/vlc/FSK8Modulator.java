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
            case 0: return Color.BLACK;
            case 1: return Color.RED;
            case 2: return Color.GREEN;
            case 3: return Color.BLUE;
            case 4: return Color.PURPLE;
            case 5: return Color.YELLOW;
            case 6: return Color.TURQUOISE;
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
        } else if( value == Color.PURPLE) {
            return symbol.toBits(4);
        } else if( value == Color.YELLOW) {
            return symbol.toBits(5);
        } else if( value == Color.TURQUOISE) {
            return symbol.toBits(6);
        } else if( value == Color.WHITE) {
            return symbol.toBits(7);
        }
        throw new IllegalArgumentException();
    }
}
