package io.sece.vlc;


/*
 * Based on the polynomial = X^8 + X^5 + X^4 + 1.
 * http://reveng.sourceforge.net/crc-catalogue/
 * https://users.ece.cmu.edu/~koopman/roses/dsn04/koopman04_crc_poly_embedded.pdf
 */
public class CRC8 {
    private static final byte[] table;

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


    private CRC8() { }


    public static int compute(byte[] data)
    {
        return compute(data, 0, data.length);
    }


    public static int compute(byte[] data, int offset, int length)
    {
        int crc8 = 0;

        for (int i = 0; i < length; i++)
            crc8 = table[(crc8 ^ data[i + offset]) & 0x0FF];

        return crc8 & 0x0FF;
    }
}
