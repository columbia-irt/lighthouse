package io.sece.vlc.trx;

import io.sece.vlc.Color;


/**
 * The ColorLEDInterface is meant to be implemented by LEDs whose color can be
 * changed. In theory, this interface could also be split into a discrete and
 * continuous variant. For example, a discrete color LED may make it possible
 * to turn on or off individual primary colors. An continuous color LED may
 * additionally make it possible to control primary color channels via PWM.
 *
 * In this implementation, we ignore the discrete case and assume that if a
 * LED implements this interface, its primary colors are continuous.
 *
 * Naturally, this interface might also extend ContinuousLEDInterface and
 * DiscreteLEDInterface. The discrete variant could be implemented by turning
 * all channels on and off, the continous variant could be implemented by
 * setting the same intensity on all channels. This is left to the actual
 * class implementing the interface, since neutral colors are rarely obtained
 * by setting the same intensity value on all channels.
 */
public interface ColorLEDInterface extends LEDInterface<Color> {
}
