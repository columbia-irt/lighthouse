package io.sece.vlc;


/*
 * Based on the polynomial = X^8 + X^5 + X^4 + 1.
 * http://reveng.sourceforge.net/crc-catalogue/
 * https://users.ece.cmu.edu/~koopman/roses/dsn04/koopman04_crc_poly_embedded.pdf
 */
public class CRC8 {
    private static final byte[] table;
    private byte value;

    static {
        table = new byte[256];
        int a;
        int v;

        for (int i = 0; i < 256; i++) {
            a = i;
            v = 0;

            for (int j = 0; j < 8; j++) {
                if (((a ^ v) & 0x01) == 0x01)
                    v = ((v ^ 0x18) >> 1) | 0x80;
                else
                    v = v >> 1;

                a >>= 1;
            }
            table[i] = (byte)v;
        }
    }


    public CRC8() {
        this(0);
    }


    public CRC8(int seed) {
        value = (byte)seed;
    }


    public CRC8 add(int data) {
        value = table[(value ^ (byte)data) & 0xff];
        return this;
    }


    public CRC8 add(byte[] data) {
        return add(data, 0, data.length);
    }


    public CRC8 add(byte[] data, int offset, int length) {
        for (int i = 0; i < length; i++)
            value = table[(value ^ data[i + offset]) & 0x0ff];
        return this;
    }


    public byte compute() {
        return value;
    }


    public static byte compute(byte[] data) {
        return compute(data, 0, data.length);
    }


    public static byte compute(byte[] data, int offset, int length) {
        return new CRC8().add(data, offset, length).compute();
    }
}
