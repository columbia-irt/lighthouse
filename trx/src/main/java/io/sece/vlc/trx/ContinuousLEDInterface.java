package io.sece.vlc.trx;


/**
 * An interface for LEDs whose intensity can be controlled on a scale of more
 * than two values. Typically, a LED connected via a PWM-controllable GPIO pin
 * might provide this kind of interface. Also, a LED connected via a voltage
 * divider or a real analog PIN might provide this kind of interface.
 *
 * This interface extends the abstract LED interface in order to set the value
 * type to Integer.
 */
public interface ContinuousLEDInterface extends LEDInterface<Integer> {
}
