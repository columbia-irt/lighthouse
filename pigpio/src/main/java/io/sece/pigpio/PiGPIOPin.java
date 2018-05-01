package io.sece.pigpio;

import io.sece.pigpio.PiGPIO;

public class PiGPIOPin
{
    int gpio; //Number of the Hardware GPIO pin
    int mode; //the mode of the GPIO pin, input/output, for our purpose it should be defined as output

    public PiGPIOPin(int gpio, int mode)
    {
        this.gpio = gpio;
        this.mode = mode;
        try
        {
            PiGPIO.gpioSetMode(gpio,mode); // GIPO pin is set with given information
        } catch (Exception ex) {
            System.out.println("Error in setting up PiGPIOPin: " + ex);
        }
    }
    
    public int getGpio() 
    {
        return gpio;
    }

    public int getMode() 
    {
        return mode;
    }
}