package io.sece.vlc;


import java.util.ArrayList;
import java.util.List;


public class Symbol {
    public int bits;   // Number of bits it takes to represent a symbol


    public Symbol(int states) {
        bits = states2bits(states);
    }


    public static int states2bits(int states) {
        if (states < 2) {
            throw new IllegalArgumentException("Number of states must be at least 2");
        }

        if ((states & (states - 1)) != 0) {
            throw new IllegalArgumentException("Number of states must be a power of 2");
        }

        return 31 - Integer.numberOfLeadingZeros(states);
    }


    public List<Integer> fromBits(BitVector data) {
        if (data.length % bits != 0)
            throw new IllegalArgumentException("Invalid bit vector length");

        ArrayList<Integer> rv = new ArrayList<>();
        for (int i = 0; i < data.length; i += bits)
            rv.add(data.get(i, bits));

        return rv;
    }


    public BitVector toBits(List<Integer> symbols) {
        BitVector dst = new BitVector();
        return toBits(dst, symbols);
    }


    public BitVector toBits(BitVector dst, List<Integer> symbols) {
        for (int sym : symbols)
            dst.set(dst.length, sym, bits);
        return dst;
    }
}
