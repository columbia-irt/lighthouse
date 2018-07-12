package io.sece.vlc.modem;

import io.sece.vlc.Color;
import io.sece.vlc.FreqModem;
import io.sece.vlc.Symbol;

import static io.sece.vlc.Color.RGBtoHSB;

public class CalibrationModem extends FreqModem
{
    private Color u;
    private Color d;
    private Symbol symbol;

    public CalibrationModem(int hue, int saturation, int brightness) {

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
    public Color detect(Color input) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder demodulate(StringBuilder buf, int offset, Color input) {
        // Not yet implemented
        throw new UnsupportedOperationException();
    }
}
