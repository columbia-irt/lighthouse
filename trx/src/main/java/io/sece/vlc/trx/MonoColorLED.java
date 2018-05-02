package io.sece.vlc.trx;

import io.sece.pigpio.PiGPIO;
import io.sece.pigpio.PiGPIOException;
import io.sece.pigpio.PiGPIOPin;

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
        pin.setPWM(value);
    }

    public void setColor(int red, int green, int blue) 
    {
        
    }
}