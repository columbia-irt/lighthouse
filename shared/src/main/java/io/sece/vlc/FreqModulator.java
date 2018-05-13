package io.sece.vlc;


/**
 * An abstract base class for all frequency modulators, i.e., modulators that
 * change the color (frequency) of the light. Not all LEDs, e.g.,
 * monochromatic, support frequency modulation.
 */
public abstract class FreqModulator extends Modulator<Color> {
}
