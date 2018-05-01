package io.sece.vlc.trx;

import io.sece.pigpio.PiGPIO;

class MonoColorLED implements LEDInterface
{
    int pin; // the GPIO pin which is connected to the LED
    public MonoColorLED(int pin)
    {
        this.pin = pin;
    }
    public void setIntensity(boolean onoff)
    {
            throw new UnsupportedOperationException();
    }

    public void setIntensity(int value)
    {
            throw new UnsupportedOperationException();
    }

    public void setColor(int red, int green, int blue)
    {
        try
        {
                PiGPIO.gpioPWM(pin, red); // MonoColor can only handle one value. ANY IDEA? Or schould setIntensity be used for that?
        }
        catch(Exception e)
        {
                System.out.println("error in setColor: " + e);
        }
    }
}