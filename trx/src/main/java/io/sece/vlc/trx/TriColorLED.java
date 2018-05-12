package io.sece.vlc.trx;

import io.sece.pigpio.PiGPIO;
import io.sece.pigpio.PiGPIOException;
import io.sece.pigpio.PiGPIOPin;
import java.awt.Color;

class TriColorLED implements LEDInterface {
    PiGPIOPin redPin;
    PiGPIOPin greenPin;
    PiGPIOPin bluePin;
    public TriColorLED(PiGPIOPin redPin, PiGPIOPin greenPin, PiGPIOPin bluePin)
    {
        //GPIO pins which are connected to the specific pins of the LED
        this.redPin = redPin;
        this.greenPin = greenPin;
        this.bluePin = bluePin;
    }
    public void setIntensity(boolean onoff)
    {
            throw new UnsupportedOperationException();
    }

    public void setIntensity(int value)
    {
            throw new UnsupportedOperationException();
    }

    public void setColor(Color color) throws PiGPIOException
    {
        redPin.setValue(color.getRed());
        greenPin.setValue(color.getGreen());
        bluePin.setValue(color.getBlue());
    }
}
