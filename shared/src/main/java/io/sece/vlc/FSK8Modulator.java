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
            case 3: return Color.PURPLE;
            case 4: return Color.YELLOW;
            case 5: return Color.TURQUIOSE;
            case 6: return Color.BLACK;
            case 7: return Color.WHITE;
        }
        throw new AssertionError();
    }

    @Override
    public String demodulate(String data, int offset, Color value) {
        // Not yet implemented
        throw new UnsupportedOperationException();
    }
}
