package io.sece.vlc;

public class BitString {
    public static final byte[] DEFAULT_DATA = toBytes("1000001101011110010110111001100010110100100011101000010110110100001111000011101001110010110110010010001110010011101000011101011000011110110111010011111100100001011100111010011010000000101101100010101111011101110010111111111000110001101000010101001001011010");

    public final byte[] data;
    public final int length;


    public BitString(byte[] data) {
        this.data = data;
        this.length = data.length * 8;
    }


    public BitString(String data) {
        this(toBytes(data));
    }


    public String toString() {
        return fromBytes(data);
    }


    public static byte[] toBytes(String src) {
        int n = src.length() / 8;
        byte[] dst = new byte[n];

        for(int i = 0; i < n; i++)
            dst[i] = (byte)Integer.parseInt(src.substring(i * 8,(i * 8) + 8),2);

        return dst;
    }


    public static String fromBytes(byte[] input) {
        StringBuilder b = new StringBuilder();

        for (byte i : input)
            b.append(String.format("%8s", Integer.toBinaryString(i & 0xff)).replace(' ', '0'));

        return b.toString();
    }
}
