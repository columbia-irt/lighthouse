package io.sece.vlc.modem;

import io.sece.vlc.Color;
import io.sece.vlc.FreqModem;

import static io.sece.vlc.Color.RGBtoHSB;

public class CalibrationModem extends FreqModem
{
    private Color u;
    private Color d;

    public CalibrationModem(int hue, int saturation, int brightness) {
        super(2);
        this.u = new Color(new int[]{hue, saturation, brightness});
        this.d = Color.BLACK;
        System.out.println("Red: " + u.red + " Green: " + u.green + " Blue: " + u.blue);
        int[] test = RGBtoHSB(null, u.red, u.green, u.blue);
        System.out.println("Hue: " + test[0] + " Saturation: " + test[1] + " Brightness: " + test[2]);
    }

    @Override
    public Color modulate(int symbol) {
        switch(symbol) {
            case 0: return d;
            case 1: return u;
        }
        throw new IllegalArgumentException("Bug: Invalid symbol " + symbol);
    }

    @Override
    public Color detect(Color input) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int demodulate(Color input) {
        // Not yet implemented
        throw new UnsupportedOperationException();
    }
}
