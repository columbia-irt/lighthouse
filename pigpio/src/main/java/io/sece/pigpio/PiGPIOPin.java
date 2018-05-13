package io.sece.pigpio;

import io.sece.pigpio.PiGPIO;
import io.sece.pigpio.PiGPIOException;


public class PiGPIOPin
{
    private int gpio;


    public PiGPIOPin(int gpio) throws PiGPIOException
    {
        this.gpio = gpio;

        PiGPIO.gpioSetMode(gpio,PiGPIO.PI_OUTPUT);
    }


    public int getGpio()
    {
        return gpio;
    }


    public void setState(boolean state) throws PiGPIOException
    {
        if (state) PiGPIO.gpioWrite(this.gpio, 1);
        else PiGPIO.gpioWrite(this.gpio, 0);
    }


    public void setPWMValue(int value) throws PiGPIOException
    {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException("Invalid value: " + value);
        }
        PiGPIO.gpioPWM(gpio, value);
    }
}
