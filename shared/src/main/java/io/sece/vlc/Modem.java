package io.sece.vlc;


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
    public int bits;
    public int states;

    public V modulate(String data) {
        return modulate(data, 0);
    }

    public abstract V modulate(String data, int offset);
    public abstract StringBuilder demodulate(StringBuilder buf, int offset, V input);
    public abstract V detect(V input);


    public String demodulate(V input) {
        return demodulate(new StringBuilder(), 0, input).toString();
    }


    public String startSequence(int amount) {
        String startingSequence = "";
        for(int i = 0; i < amount; i++)
        {
            startingSequence += String.format("%" + bits + "s", Integer.toBinaryString(i % states)).replace(' ', '0');
        }
        return startingSequence;
    }
}
