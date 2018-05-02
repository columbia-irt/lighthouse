package io.sece.vlc.trx;

import io.sece.pigpio.PiGPIO;
import io.sece.pigpio.PiGPIOException;

class PolRZModulator implements LEDModulator
{    
    TriColorLED led;
    int delay;
    public PolRZModulator(TriColorLED led, int delay)
    {
        this.led = led;
        this.delay = delay;
    }
    
    public void setSymbols(String symbols) throws PiGPIOException
    {
        // modulate each symbol into a color here eather red = '0' or blue = '1'
        for(int i = 0; i < symbols.length(); i++)
        {
            System.out.println(System.currentTimeMillis());
            switch(symbols.charAt(i))
            {
                case ('0') :
                    led.setColor(255,0,0);
                    break;
                case ('1') :
                    led.setColor(0,0,255);
                    break;
                default :
                    System.out.println("Error case in Switch");
                    led.setColor(255,255,255);
                    break;                
            }
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ex) {
                System.out.println("Error case in Sleep: " + ex);
            }
            //after each symbol the LED is turned off => returns too its neutral state
            led.setColor(0,0,0);
            
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ex) {
                System.out.println("Error case in Sleep: " + ex);
            }
        }
        // end bit green
        led.setColor(0,255,0);
        
        try {
                Thread.sleep(75);
            } catch (InterruptedException ex) {
                System.out.println("Error case in Sleep: " + ex);
            }
        //reset led to off after done with modulation
        led.setColor(0,0,0);
    }
}