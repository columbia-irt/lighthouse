package io.sece.vlc.trx.led;

import io.sece.pigpio.PiGPIOException;
import io.sece.pigpio.PiGPIOPin;
import io.sece.vlc.Color;
import io.sece.vlc.trx.ColorLEDInterface;


/**
 * A driver for a tri-color (RGB) LED connected to three different GPIO pins
 * on a Raspberry PI and PWM-controlled by the PiGPIO library.
 */
public class PiRgbAnodeLED implements ColorLEDInterface {
    private PiPwmLED red;
    private PiPwmLED green;
    private PiPwmLED blue;


    public PiRgbAnodeLED(PiGPIOPin red, PiGPIOPin green, PiGPIOPin blue) {
        this.red = new PiPwmLED(red);
        this.green = new PiPwmLED(green);
        this.blue = new PiPwmLED(blue);
    }


    public PiRgbAnodeLED(String arguments) throws PiGPIOException {
        String[] args = arguments.split(",");
        if (args.length != 3)
            throw new IllegalArgumentException("Invalid arguments: " + arguments);

        this.red = new PiPwmLED(new PiGPIOPin(Integer.parseInt(args[0])));
        this.green = new PiPwmLED(new PiGPIOPin(Integer.parseInt(args[1])));
        this.blue = new PiPwmLED(new PiGPIOPin(Integer.parseInt(args[2])));
    }


    @Override
    public void set(Color color) {
        red.set(255 - color.red);
        green.set(255 - color.green);
        blue.set(255 - color.blue);
    }
}
