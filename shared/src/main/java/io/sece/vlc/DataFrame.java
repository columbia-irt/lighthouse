package io.sece.vlc;


public class DataFrame {
    public static final int MAX_PAYLOAD_SIZE = 4; //3 for FSK4 for FSK8 we need 4 so that MAX_SIZE is 6 and therefor % 3 = 0
    public static final int MAX_SIZE = 2 + MAX_PAYLOAD_SIZE;
    public static final int MIN_SIZE = 2;

    public static final class FrameTooShort extends Exception { }

    public int seqNumber;
    public byte checksum;
    public byte[] payload = new byte[0];
    public boolean error;


    public void unpack(BitVector input) throws FrameTooShort {
        int bytes = input.length / 8;

        if (bytes < MIN_SIZE)
            throw new FrameTooShort();

        checksum = input.data[0];
        seqNumber = input.data[1] & 0xff;

        payload = new byte[bytes - 2];
        System.arraycopy(input.data, 2, payload, 0, bytes - 2);

        error = checksum != CRC8.compute(input.data, 1, bytes - 1);
    }


    public BitVector pack() {
        CRC8 crc = new CRC8();
        crc.add(seqNumber).add(payload);

        byte[] rv = new byte[payload.length + 2];

        rv[0] = checksum = crc.compute();
        rv[1] = (byte)seqNumber;
        System.arraycopy(payload, 0, rv, 2, payload.length);

        return new BitVector(rv);
    }
}
