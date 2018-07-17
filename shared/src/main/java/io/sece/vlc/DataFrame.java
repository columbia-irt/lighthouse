package io.sece.vlc;


public class DataFrame {
    public static final int MAX_PAYLOAD_SIZE = 3;
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
    private static final class FrameTooShort extends IndexOutOfBoundsException { }

    private int state = START;

    public int seqNumber;
    public byte checksum;
    public byte[] payload = new byte[0];

    private StringBuilder bitString = new StringBuilder();

    private Modem<Color> modem;

    public DataFrame(Modem modem){
        this.modem = modem;
    }

    public String getCurrentData() {
        return bitString.toString();
    }


    public boolean errorsDetected() {
        if (payload.length <= 0) return true;
        CRC8 crc = new CRC8();
        crc.add(seqNumber).add(payload);
        return checksum != crc.compute();
    }


    public void reset() {
        reset(RX_STATE_D);
    }


    private void reset(int state) {
        bitString.setLength(0);
        payload = null;
        this.state = state;
    }


    public void parse(byte[] data) {
        if (data.length < 2)
            throw new FrameTooShort();
        checksum = data[0];
        seqNumber = data[1] & 0xff;
        payload = new byte[data.length - 2];
        System.arraycopy(data, 2, payload, 0, data.length - 2);
    }


    public byte[] encode() {
        CRC8 crc = new CRC8();
        crc.add(seqNumber).add(payload);

        byte[] rv = new byte[payload.length + 2];
        rv[0] = checksum = crc.compute();
        rv[1] = (byte)seqNumber;
        System.arraycopy(payload, 0, rv, 2, payload.length);
        return rv;
    }


    public boolean rx(Color color) {
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
                        try {
                            parse(BitString.toBytes(bitString.toString()));
                            return true;
                        } catch (FrameTooShort e) {
                            return false;
                        }
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

        return false;
    }


    private void store(String input) {
        if (input.length() + bitString.length() > MAX_FRAME_SIZE)
            throw new FrameTooLong();

        bitString.append(input);
    }


    public String tx(int width) {
        state = START;
        StringBuilder b = new StringBuilder(FRAME_MARKER);
        String input = BitString.fromBytes(encode());

        for(int i = 0; i < input.length(); i += width)
            tx(b, modem.modulate(input.substring(i, i + width)));
        return b.toString();
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
