package io.sece.vlc;

import static io.sece.vlc.Color.hsvToRGB;
import static io.sece.vlc.Color.RGBtoHSB;

public class CalibrationModulator extends FreqModulator
{
    private Color u;
    private Color d;
    private Symbol symbol;

    public CalibrationModulator(float hue, float saturation, float brightness) {

        this.u = hsvToRGB(hue, saturation, brightness);
        this.d = Color.BLACK;
        symbol = new Symbol(2);
        bits = symbol.bits;
        System.out.println("Red: " + u.getRed() + " Green: " + u.getGreen() + " Blue: " + u.getBlue());
        float[] test = RGBtoHSB(u.getRed(),u.getGreen(),u.getBlue(),null);
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
    public String demodulate(String data, int offset, Color value) {
        // Not yet implemented
        throw new UnsupportedOperationException();
    }
}
