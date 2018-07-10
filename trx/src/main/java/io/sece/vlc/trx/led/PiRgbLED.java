package io.sece.vlc.trx.led;

import io.sece.pigpio.PiGPIOPin;
import io.sece.vlc.Color;
import io.sece.vlc.trx.ColorLEDInterface;
import io.sece.vlc.trx.LEDException;


/**
 * A driver for a tri-color (RGB) LED connected to three different GPIO pins
 * on a Raspberry PI and PWM-controlled by the PiGPIO library.
 */
public class PiRgbLED implements ColorLEDInterface {
    private PiPwmLED red;
    private PiPwmLED green;
    private PiPwmLED blue;

    public PiRgbLED(PiGPIOPin red, PiGPIOPin green, PiGPIOPin blue)
    {
        this.red = new PiPwmLED(red);
        this.green = new PiPwmLED(green);
        this.blue = new PiPwmLED(blue);
    }

    @Override
    public void set(Color color) throws LEDException
    {
        /*calibrate hue value 180
        if(color.green == 255 && color.blue == 255)
        {
            red.set(color.red);
            green.set(color.green);
            blue.set(100);
        }
        else
        {*/
            red.set(color.red);
            green.set(color.green);
            blue.set(color.blue);
        //}
    }
}
