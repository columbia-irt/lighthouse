package io.sece.vlc.trx;


/**
 * Discrete LED interface is the most restricted type of LED inteface. A
 * discrete LED can only be turned on or off, but nothing in-between.
 * Typically, a LED connected to a GPIO pin without PWM capabilities could
 * only provide a discrete interface.
 *
 * Although there could be, in theory, LEDs with multiple discrete states, we
 * only consider the two-state case here. The continuous version of the LED
 * interface might be a better fit for a descrete LED with multiple states.
 *
 * This interface extends the abstract LEDInterface in order to set the value
 * type to Boolean.
 */
interface DiscreteLEDInterface extends LEDInterface<Boolean> {
}
