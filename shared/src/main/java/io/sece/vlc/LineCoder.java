package io.sece.vlc;


import io.sece.vlc.modem.FSK4Modem;
import io.sece.vlc.modem.FSK8Modem;

public class LineCoder {
    private String frame_marker = "";

    private static final int START = 0;
    private static final int RX_STATE_S1 = 1;
    private static final int RX_STATE_S2 = 2;
    private static final int RX_STATE_D = 3;
    private static final int RX_STATE_DS1 = 4;
    private static final int RX_STATE_DS2 = 5;
    private static final int TX_STATE_D1 = 1;

    private static final class FrameTooLong extends IndexOutOfBoundsException { }

    private int maxFrameSize;
    private int state = START;
    private StringBuilder buffer = new StringBuilder();
    private Modem<Color> modem;

    /*
        Colors for Startingsequence
     */
    private Color color1;
    private Color color2;
    private Color color3;

    public LineCoder(Modem modem, int maxFrameSize) {
        this.modem = modem;
        this.maxFrameSize = maxFrameSize * 8;
        if(modem instanceof FSK4Modem)
            initFrameMarker(((FSK4Modem) modem).e, ((FSK4Modem) modem).w, ((FSK4Modem) modem).s);
        if(modem instanceof FSK8Modem)
            initFrameMarker(((FSK8Modem) modem).color[FSK8Modem.E], ((FSK8Modem) modem).color[FSK8Modem.W], ((FSK8Modem) modem).color[FSK8Modem.SE]);
    }

    public void initFrameMarker(Color color1, Color color2, Color color3){
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;

        frame_marker = modem.demodulate(color1) + modem.demodulate(color2) + modem.demodulate(color3);
        System.out.println("FrameMarker: " + frame_marker);
    }

    public String getCurrentData() {
        return buffer.toString();
    }


    public void reset() {
        reset(RX_STATE_D);
    }


    private void reset(int state) {
        buffer.setLength(0);
        this.state = state;
    }


    public BitString rx(Color color) {
        try {
            switch (state) {
                case START:
                    if (color.equals(color1)) state = RX_STATE_S1;
                    break;

                case RX_STATE_S1:
                    if(color.equals(color2)){
                        state = RX_STATE_S2;
                    }else if(color.equals(color1)){
                        state = RX_STATE_S1;
                    }else{
                        state = START;
                    }
                    break;

                case RX_STATE_S2:
                    if(color.equals(color3)){
                        state = RX_STATE_D;
                    }else if(color.equals(color1)){
                        state = RX_STATE_S1;
                    }else{
                        state = START;
                    }
                    break;

                case RX_STATE_D:
                    if (color.equals(color1))
                        state = RX_STATE_DS1;
                    else
                        store(modem.demodulate(color));
                    break;

                case RX_STATE_DS1:
                    if (color.equals(color2)) {
                        state = RX_STATE_DS2;
                    } else if (color.equals(color1)) {
                        store(modem.demodulate(color));
                    } else {
                        store(modem.demodulate(color1) + modem.demodulate(color));
                        state = RX_STATE_D;
                    }
                    break;

                case RX_STATE_DS2:
                    if (color.equals(color3)) {
                        BitString rv = new BitString(buffer.toString());
                        reset();
                        return rv;
                    } else if (color.equals(color2)) {
                        store(modem.demodulate(color1) + modem.demodulate(color2));
                        state = RX_STATE_D;
                    } else {
                        reset(START);
                    }
                    break;

                default:
                    throw new RuntimeException("Bug: Invalid state reached");
            }
        } catch (FrameTooLong e) {
            reset(START);
        }

        return null;
    }


    private void store(String input) {
        if (input.length() + buffer.length() > maxFrameSize)
            throw new FrameTooLong();

        buffer.append(input);
    }


    public String tx(BitString data) {
        state = START;

        buffer.setLength(0);
        buffer.append(frame_marker);

        String input = data.toString();

        for(int i = 0; i < input.length(); i += modem.bits)
            tx(buffer, modem.modulate(input.substring(i, i + modem.bits)));

        return buffer.toString();
    }


    private void tx(StringBuilder b, Color s) {
        switch(state) {
            case START:
                if (s.equals(color1)) state = TX_STATE_D1;
                b.append(modem.demodulate(s));
                break;

            case TX_STATE_D1:
                if (s.equals(color2)) {
                    b.append(modem.demodulate(s) + modem.demodulate(s));
                    state = START;
                } else if(s.equals(color1)) {
                    b.append(modem.demodulate(s));
                    state = TX_STATE_D1;
                } else {
                    b.append(modem.demodulate(s));
                    state = START;
                }
                break;
            default:
                throw new RuntimeException("Bug: Invalid state reached");
        }
    }
}
