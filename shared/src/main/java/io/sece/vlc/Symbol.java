package io.sece.vlc;

import java.util.BitSet;


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
    public int max;    // The maximum symbol number returned by fromBits
    public int states; // Number of symbol states

    /**
     * Create a Symbol instance with the given number of states. The number
     * of states must be a power of 2 and must be at least 2.
     */
    public Symbol(int states) {
        if (states < 2) {
            throw new IllegalArgumentException("Number of states must be at least 2");
        }

        if ((states & (states - 1)) != 0) {
            throw new IllegalArgumentException("Number of states must be a power of 2");
        }

        bits = 31 - Integer.numberOfLeadingZeros(states);
        this.states = states;
        max = states - 1;
    }


    /**
     * Convert the bits at the beginning of the buffer data into a symbol value.
     */
    public int fromBits(BitSet data) {
        return fromBits(data, 0);
    }


    /**
     * Convert n bits starting at index offset in the buffer data into a
     * symbol value.
     */
    public int fromBits(BitSet data, int offset) {
        int rv = 0;
        for(int i = offset + bits - 1; i >= offset; i--) {
            rv <<= 1;
            if (data.get(i)) rv += 1;
        }
        return rv;
    }


    /**
     * Convert the given modulation symbol value into bits and store the bits
     * at the beginning of buffer out. Return buffer out.
     */
    public BitSet toBits(BitSet data, int symbol) {
        return toBits(data, 0, symbol);
    }


    /**
     * Convert the given modulation symbol value into bits and store the bits
     * in buffer out starting at index offset. Return the output buffer.
     */
    public BitSet toBits(BitSet data, int offset, int symbol) {
        for(int i = 0; i < bits; i++)
            data.set(offset + i, ((symbol >> i) & 1L) == 1L);
        return data;
    }
}
