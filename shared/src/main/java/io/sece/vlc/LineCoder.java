package io.sece.vlc;


public class LineCoder {
    private static final String FRAME_MARKER = "011110";

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


    public LineCoder(Modem modem, int maxFrameSize) {
        this.modem = modem;
        this.maxFrameSize = maxFrameSize * 8;
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
                    if (color.equals(Color.RED)) state = RX_STATE_S1;
                    break;

                case RX_STATE_S1:
                    state = color.equals(Color.BLUE) ? RX_STATE_S2 : START;
                    break;

                case RX_STATE_S2:
                    state = color.equals(Color.GREEN) ? RX_STATE_D : START;
                    break;

                case RX_STATE_D:
                    if (color.equals(Color.RED))
                        state = RX_STATE_DS1;
                    else
                        store(modem.demodulate(color));
                    break;

                case RX_STATE_DS1:
                    if (color.equals(Color.BLUE)) {
                        state = RX_STATE_DS2;
                    } else if (color.equals(Color.RED)) {
                        store(modem.demodulate(color));
                    } else {
                        store(modem.demodulate(Color.RED) + modem.demodulate(color));
                        state = RX_STATE_D;
                    }
                    break;

                case RX_STATE_DS2:
                    if (color.equals(Color.GREEN)) {
                        BitString rv = new BitString(buffer.toString());
                        reset();
                        return rv;
                    } else if (color.equals(Color.BLUE)) {
                        store(modem.demodulate(Color.RED) + modem.demodulate(Color.BLUE));
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
        buffer.append(FRAME_MARKER);

        String input = data.toString();

        for(int i = 0; i < input.length(); i += modem.bits)
            tx(buffer, modem.modulate(input.substring(i, i + modem.bits)));

        return buffer.toString();
    }


    private void tx(StringBuilder b, Color s) {
        switch(state) {
            case START:
                if (s.equals(Color.RED)) state = TX_STATE_D1;
                b.append(modem.demodulate(s));
                break;

            case TX_STATE_D1:
                if (s.equals(Color.BLUE)) {
                    b.append(modem.demodulate(s) + modem.demodulate(s));
                    state = START;
                } else if(s.equals(Color.RED)) {
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
