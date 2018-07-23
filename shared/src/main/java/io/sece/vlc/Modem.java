package io.sece.vlc;


import java.util.ArrayList;
import java.util.List;

/**
 * An abstract base class for all modulators and demodulators. The base class
 * declares API to modulate a bit set into a LED signal and to demodulate a
 * LED signal into a bit set. The type of LED signal is configurable and is
 * meant to be set by implementations inheriting from this class.
 *
 * Each modulate/demodulate function is implemented in two variants: with and
 * without an offset. The offset parameters allows reading/storing the bits at
 * a non-zero index in an existing BitSet buffer.start
 *
 * Each modulator also exports an attribute bits which determines how many
 * bits at a time the modulator consumers or generates.
 */
public abstract class Modem<V extends Coordinate> {
    public final int bits;
    public final int states;

    protected Modem(int states) {
        this.states = states;
        bits = Symbol.states2bits(states);
    }

    public List<V> modulate(List<Integer> symbols) {
        List<V> rv = new ArrayList<>();
        for(int s : symbols) rv.add(modulate(s));
        return rv;
    }

    public abstract V modulate(int symbol);
    public abstract int demodulate(V input);
    public abstract V detect(V input);
}
