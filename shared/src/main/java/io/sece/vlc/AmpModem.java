package io.sece.vlc;


/**
 * An abstract base class for all amplitude modulators, i.e., modulators that
 * modulate-demodulate the amplitude (intensity) of the LED light. Amplitude
 * modulators generate an intensity encoded in an integer. Amplitude
 * demodulators accept an Integer intensity and generate a symbol.
 *
 * Not all LEDs support amplitude modulation. For example, a LED that only
 * supports on-off states cannot amplitude-modulated.
 */
public abstract class AmpModem extends Modem<Amplitude> {
}
