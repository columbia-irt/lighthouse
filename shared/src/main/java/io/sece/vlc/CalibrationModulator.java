package io.sece.vlc;

import static io.sece.vlc.Color.RGBtoHSB;

public class CalibrationModulator extends FreqModulator
{
    private Color u;
    private Color d;
    private Symbol symbol;

    public CalibrationModulator(int hue, int saturation, int brightness) {

        this.u = new Color(new int[]{hue, saturation, brightness});
        this.d = Color.BLACK;
        states = 2;
        symbol = new Symbol(states);
        bits = symbol.bits;
        System.out.println("Red: " + u.red + " Green: " + u.green + " Blue: " + u.blue);
        int[] test = RGBtoHSB(null, u.red, u.green, u.blue);
        System.out.println("Hue: " + test[0] + " Saturation: " + test[1] + " Brightness: " + test[2]);
    }

    @Override
    public Color modulate(String data, int offset) {
        switch(symbol.fromBits(data, offset)) {
            case 0: return d;
            case 1: return u;
        }
        throw new AssertionError();
    }

    @Override
    public String demodulate(int value) {
        // Not yet implemented
        throw new UnsupportedOperationException();
    }


    private Color getClosestElement(int value) {
        throw new UnsupportedOperationException();
    }
}
