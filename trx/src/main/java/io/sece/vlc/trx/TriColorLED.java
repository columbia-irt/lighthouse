package io.sece.vlc.trx;

import io.sece.pigpio.PiGPIO;
import io.sece.pigpio.PiGPIOException;
import io.sece.pigpio.PiGPIOPin;

class TriColorLED implements LEDInterface
{
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

    public void setColor(int red, int green, int blue) throws PiGPIOException
    {
        redPin.setPWM(red);
        greenPin.setPWM(green);
        bluePin.setPWM(blue);
    }
}
