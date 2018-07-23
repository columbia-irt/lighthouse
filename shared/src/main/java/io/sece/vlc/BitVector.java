package io.sece.vlc;

import java.util.Iterator;
import java.util.NoSuchElementException;


public final class BitVector implements Iterable<Boolean> {
    public static final BitVector DEFAULT_DATA = new BitVector("1000001101011110010110111001100010110100100011101000010110110100001111000011101001110010110110010010001110010011101000011101011000011110110111010011111100100001011100111010011010000000101101100010101111011101110010111111111000110001101000010101001001011010");

    public byte[] data;
    public int length;


    private static int _get(byte[] data, int i, int width) {
        if (width <= 0 || width > 32)
            throw new IllegalArgumentException("Width must be between 1 and 32");

        int I = i + width - 1;
        int b1 = i / 8;
        int b2 = I / 8;

        byte mask = (byte) Math.pow(2, I % 8);
        mask += mask - 1;

        int d = (data[b2] & mask) & 0xff;
        for (int j = b2 - 1; j >= b1; j--) {
            d <<= 8;
            d += data[j] & 0xff;
        }

        return d >> (i % 8);
    }


    private static void _set(byte[] data, int i, int value, int width) {
        byte b;
        int mask;
        boolean v;

        for (int j = i; j < i + width; j++) {
            b = data[j / 8];
            mask = 1 << (j % 8);
            v = (value & 1) == 1;
            data[j / 8] = (byte)(v ? b | mask : b & ~mask);

            value >>= 1;
        }
    }


    private static byte[] parseString(String src) {
        int len = src.length();
        int n = len / 8 + ((len % 8 != 0) ? 1 : 0);
        byte[] dst = new byte[n];

        for (int i = 0; i < len; i++)
            switch(src.charAt(i)) {
                case '0': break;
                case '1': _set(dst, len - i - 1, 1, 1); break;
                default:
                    throw new IllegalArgumentException("Invalid bit string");
            }

        return dst;
    }


    public BitVector() {
        data = new byte[0];
        length = 0;
    }


    public BitVector(BitVector src) {
        this(src.data, src.length);
    }


    public BitVector(byte[] data) {
        this(data, data.length * 8);
    }


    public BitVector(String str) {
        this(parseString(str), str.length());
    }


    public BitVector(byte[] data, int length) {
        if (length > data.length * 8)
            throw new IllegalArgumentException("Byte array too short");

        this.data = data.clone();
        this.length = length;
    }


    public Iterator<Boolean> iterator() {
        return new Iterator<Boolean>() {
            private int index = 0;

            @Override
            public Boolean next() {
                try {
                    return get(index);
                } catch (IndexOutOfBoundsException e) {
                    throw new NoSuchElementException();
                } finally {
                    index++;
                }
            }

            @Override
            public boolean hasNext() {
                return index < length;
            }
        };
    }


    public int capacity() {
        return data.length * 8;
    }


    public void clear() {
        length = 0;
    }


    public boolean get(int i) {
        return get(i, 1) == 1;
    }


    public int get(int i, int width) {
        int I = i + width - 1;
        if (I >= length)
            throw new IndexOutOfBoundsException();

        return _get(data, i, width);
    }


    public BitVector resize(int totalBits) {
        if (totalBits > capacity()) {
            byte[] buf = new byte[(int) Math.ceil((double) totalBits / 8d)];
            System.arraycopy(data, 0, buf, 0, data.length);
            data = buf;
        }
        length = totalBits;
        return this;
    }


    public BitVector set(int i, boolean value) {
        return set(i, value ? 1 : 0, 1);
    }


    public BitVector set(int i, int value, int width) {
        resize(i + width);
        _set(data, i, value, width);
        return this;
    }


    public String toString() {
        StringBuilder b = new StringBuilder();

        for (int i = length - 1; i >= 0; i--)
            b.append(_get(data, i, 1) == 1 ? '1' : '0');

        return b.toString();
    }
}
