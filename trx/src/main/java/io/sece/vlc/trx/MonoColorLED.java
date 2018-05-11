package io.sece.vlc.trx;

import io.sece.pigpio.PiGPIO;
import io.sece.pigpio.PiGPIOException;
import io.sece.pigpio.PiGPIOPin;
import java.awt.Color;

class MonoColorLED implements LEDInterface
{
    PiGPIOPin pin; // the GPIO pin which is connected to the LED
    public MonoColorLED(PiGPIOPin pin)
    {
        this.pin = pin;
    }
    public void setIntensity(boolean on) throws PiGPIOException
    {
        pin.setState(on);
    }

    public void setIntensity(int value) throws PiGPIOException
    {
        pin.setValue(value);
    }

    public void setColor(Color color) 
    {
            throw new UnsupportedOperationException();
    }
}