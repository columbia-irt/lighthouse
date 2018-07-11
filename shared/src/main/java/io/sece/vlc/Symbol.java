package io.sece.vlc;



/**
 * A helper class that converts sequences of bits into modulation symbols and
 * vice versa. A modulation symbol is represented by an integer value from a
 * small range. For example, an 8-state symbol can be encoded by three bits
 * and individual symbol values will be encoded as integers in a 0 to 7 range.
 *
 * The Symbol class is a helper class for modulators/demodulators and is
 * normally not exported out of the package.
 */
class Symbol {
    public int bits;   // Number of bits it takes to represent a symbol

    /**
     * Create a Symbol instance with the given number of states. The number
     * of states must be a power of 2 and must be at least 2.
     */
    Symbol(int states) {
        if (states < 2) {
            throw new IllegalArgumentException("Number of states must be at least 2");
        }

        if ((states & (states - 1)) != 0) {
            throw new IllegalArgumentException("Number of states must be a power of 2");
        }

        bits = 31 - Integer.numberOfLeadingZeros(states);
    }


    /**
     * Convert the bits at the beginning of the buffer data into a symbol value.
     */
    public int fromBits(String data) {
        return fromBits(data, 0);
    }


    /**
     * Convert n bits starting at index offset in the buffer data into a
     * symbol value.
     */
    public int fromBits(String data, int offset) {
        int rv = 0;
        for(int i = offset; i < offset + bits; i++) {
            rv <<= 1;
            if (data.charAt(i) == '1') rv += 1;
        }
        return rv;
    }


    public String toBits(int symbol) {
        return toBits(new StringBuilder(), 0, symbol).toString();
    }


    /**
     * Convert the given modulation symbol value into bits and storeRX the bits
     * in buffer out starting at index offset. Return the output buffer.
     */
    public StringBuilder toBits(StringBuilder buf, int offset, int symbol) {
        for(int i = bits - 1; i >= 0; i--)
            buf.insert(offset++, ((symbol >> i) & 1L) == 1L ? '1' : '0');
        return buf;
    }
}
