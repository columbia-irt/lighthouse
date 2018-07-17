package io.sece.vlc;


public class DataFrame {
    public static final int MAX_PAYLOAD_SIZE = 3;
    public static final int MAX_SIZE = 2 + MAX_PAYLOAD_SIZE;
    public static final int MIN_SIZE = 2;

    public static final class FrameTooShort extends Exception { }

    public int seqNumber;
    public byte checksum;
    public byte[] payload = new byte[0];
    public boolean error;


    public void parse(byte[] data) throws FrameTooShort {
        if (data.length < 2)
            throw new FrameTooShort();

        checksum = data[0];
        seqNumber = data[1] & 0xff;

        payload = new byte[data.length - 2];
        System.arraycopy(data, 2, payload, 0, data.length - 2);

        error = checksum != CRC8.compute(data, 1, data.length - 1);
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
}
