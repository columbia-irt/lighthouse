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

    public PiPwmLED(PiGPIOPin pin)
    {
        this.pin = pin;
    }

    @Override
    public void set(Integer value) throws LEDException
    {
        try {
            pin.setPWMValue(value);
        } catch (PiGPIOException e) {
            throw new LEDException(e);
        }
    }
}
