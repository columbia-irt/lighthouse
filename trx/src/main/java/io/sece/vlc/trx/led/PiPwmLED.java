package io.sece.vlc.trx.led;

import io.sece.pigpio.PiGPIOException;
import io.sece.pigpio.PiGPIOPin;
import io.sece.vlc.trx.ContinuousLEDInterface;
import io.sece.vlc.trx.LEDException;


/**
 * A driver for a monochromatic LED connected to a GPIO on a Raspberry PI and
 * controlled by the PiGPIO library. Since PiGPIO can automatically do PWM on
 * any GPIO pin, this driver implements the continuous variant of the
 * interface.
 */
public class PiPwmLED implements ContinuousLEDInterface {
    private PiGPIOPin pin;


    public PiPwmLED(PiGPIOPin pin) {
        // Set the PWM frequency to the maximum value by default. The library may set a lower
        // value depending on the sample rate used.
        this(pin, 40000);
    }


    public PiPwmLED(PiGPIOPin pin, int pwmFrequency) {
        this.pin = pin;
        try {
            pin.setPWMFrequency(pwmFrequency);
        } catch (PiGPIOException e) {
            throw new LEDException(e);
        }
    }


    @Override
    public void set(Integer value) {
        try {
            pin.setPWMValue(value);
        } catch (PiGPIOException e) {
            throw new LEDException(e);
        }
    }
}
