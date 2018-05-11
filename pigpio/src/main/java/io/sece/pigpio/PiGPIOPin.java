package io.sece.pigpio;

import io.sece.pigpio.PiGPIO;
import io.sece.pigpio.PiGPIOException;

public class PiGPIOPin
{
    private int gpio; //Number of the Hardware GPIO pin

    public PiGPIOPin(int gpio) throws PiGPIOException
    {
        this.gpio = gpio;
                
        PiGPIO.gpioSetMode(gpio,PiGPIO.PI_OUTPUT); // GIPO pin is set to Output since this is what we need
    }
    
    public int getGpio() 
    {
        return gpio;
    }
    
    public void setState(boolean on) throws PiGPIOException
    {
        if(on)
        {
            PiGPIO.gpioWrite(this.gpio, 1);
        }
        else
        {
            PiGPIO.gpioWrite(this.gpio, 0);
        }
    }
    public void setValue(int value) throws PiGPIOException
    {
        if(value >= 0 && value <=255)
        {
            switch(value)
            {
                case 0:
                    this.setState(false);
                    break;
                case 255:
                    this.setState(true);
                    break;
                default:
                    PiGPIO.gpioPWM(this.gpio, value);
                    break;
            }
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }
}