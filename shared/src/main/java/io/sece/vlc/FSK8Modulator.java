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
            case 6: return Color.TURQUIOSE;
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
