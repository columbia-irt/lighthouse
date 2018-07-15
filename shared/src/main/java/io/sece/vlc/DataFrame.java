package io.sece.vlc;


public class DataFrame {
    public static final int MAX_PAYLOAD_SIZE = 4;
    private static final int MAX_FRAME_SIZE = (1 + MAX_PAYLOAD_SIZE) * 2 * 8;
    public static final String FRAME_MARKER = "011110";

    private static final int START = 0;
    private static final int RX_STATE_S1 = 1;
    private static final int RX_STATE_S2 = 2;
    private static final int RX_STATE_D = 3;
    private static final int RX_STATE_DS1 = 4;
    private static final int RX_STATE_DS2 = 5;
    private static final int TX_STATE_D1 = 1;

    private static final class FrameTooLong extends IndexOutOfBoundsException { }

    private int state = START;
    private StringBuilder buffer = new StringBuilder();
    private byte[] data;


    public String getCurrentData() {
        return buffer.toString();
    }


    public boolean errorsDetected() {
        if (data.length <= 1) return true;
        int a = data[0] & 0xff;
        int b = CRC8.compute(data, 1, data.length - 1);
        return a != b;
    }


    public byte[] getPayload() {
        byte[] rv = new byte[data.length - 1];
        System.arraycopy(data, 1, rv, 0, rv.length);
        return rv;
    }


    public void setPayload(byte[] payload) {
        data = new byte[payload.length + 1];
        System.arraycopy(payload, 0, data, 1, payload.length);
        data[0] = (byte)CRC8.compute(data, 1, payload.length);

        buffer.setLength(0);
        buffer.append(BitString.fromBytes(data));
    }


    public void reset() {
        reset(RX_STATE_D);
    }


    private void reset(int state) {
        buffer.setLength(0);
        data = null;
        this.state = state;
    }


    public boolean rx(String bits) {
        try {
            switch (state) {
                case START:
                    if (bits.equals("01")) state = RX_STATE_S1;
                    break;

                case RX_STATE_S1:
                    state = bits.equals("11") ? RX_STATE_S2 : START;
                    break;

                case RX_STATE_S2:
                    state = bits.equals("10") ? RX_STATE_D : START;
                    break;

                case RX_STATE_D:
                    if (bits.equals("01"))
                        state = RX_STATE_DS1;
                    else
                        store(bits);
                    break;

                case RX_STATE_DS1:
                    if (bits.equals("11")) {
                        state = RX_STATE_DS2;
                    } else if (bits.equals("01")) {
                        store("01");
                    } else {
                        store("01" + bits);
                        state = RX_STATE_D;
                    }
                    break;

                case RX_STATE_DS2:
                    if (bits.equals("10")) {
                        data = BitString.toBytes(buffer.toString());
                        return true;
                    } else if (bits.equals("11")) {
                        store("0111");
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

        return false;
    }


    private void store(String input) {
        if (input.length() + buffer.length() > MAX_FRAME_SIZE)
            throw new FrameTooLong();

        buffer.append(input);
    }


    public String tx(int width) {
        state = START;
        StringBuilder b = new StringBuilder(FRAME_MARKER);
        String input = buffer.toString();

        for(int i = 0; i < input.length(); i += width)
            tx(b, input.substring(i, i + width));
        return b.toString();
    }


    private void tx(StringBuilder b, String s) {
        switch(state) {
            case START:
                if (s.equals("01")) state = TX_STATE_D1;
                b.append(s);
                break;

            case TX_STATE_D1:
                if (s.equals("11")) {
                    b.append("1111");
                    state = START;
                } else if(s.equals("01")) {
                    b.append("01");
                    state = TX_STATE_D1;
                } else {
                    b.append(s);
                    state = START;
                }
                break;

            default:
                throw new RuntimeException("Bug: Invalid state reached");
        }
    }
}
