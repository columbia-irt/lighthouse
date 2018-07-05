package io.sece.vlc.trx.led;

import io.sece.pigpio.PiGPIOException;
import io.sece.pigpio.PiGPIOPin;
import io.sece.vlc.trx.DiscreteLEDInterface;
import io.sece.vlc.trx.LEDException;


/**
 * A driver for the most basic LED connected to a GPIO pin on a Raspberry PI
 * without PWM capabilities. A LED controlled by this driver can be only
 * turned on or off.
 *
 * This class is mostly for development and debugging purposes.
 */
public class PiBasicLED implements DiscreteLEDInterface {
    private PiGPIOPin pin;

    public PiBasicLED(PiGPIOPin pin)
    {
        this.pin = pin;
    }

    @Override
    public void set(Boolean value) throws LEDException
    {
        try {
            pin.setState(value);
        } catch (PiGPIOException e) {
            throw new LEDException(e);
        }
    }
}
