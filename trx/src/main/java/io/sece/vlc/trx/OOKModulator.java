package io.sece.vlc.trx;

import io.sece.pigpio.PiGPIO;
import io.sece.pigpio.PiGPIOException;

class OOKModulator implements LEDModulator
{
    LEDInterface led;
    int delay;
    long timeBefore = 0; //the sleep will depend on the real delay time
    long timeAfter = 0;
    long delta = 0;
    float time = 0;
    public OOKModulator(LEDInterface led, int delay)
    {
        this.led = led;
        this.delay = delay; // in ms
    }
    public void setSymbols(String symbols) throws PiGPIOException
    {
        //starting bit Green
        led.setColor(0,255,0);
		
		try {
                Sleeper.sleepNanos(delay * 1000000); // from ms to nano
            } catch (InterruptedException ex) {
                System.out.println("Error case in Sleep: " + ex);
            }
            timeBefore = System.currentTimeMillis();
        // modulate each symbol into a color here eather off = '0' or red = '1'
        for(int i = 0; i < symbols.length(); i++)
        {
            switch(symbols.charAt(i))
            {
                case '0':
                    led.setColor(0,0,0);
                    break;
                case '1':
                    led.setColor(255,0,0);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            try {
                Sleeper.sleepNanos((delay - delta) * 1000000);
            } catch (InterruptedException ex) {
                System.out.println("Error case in Sleep: " + ex);
            }
            timeAfter = System.currentTimeMillis();
            delta = (timeAfter - timeBefore) - (delay * (i+1));
            System.out.println("Delta: " + timeAfter);
            time += (timeAfter - timeBefore) / (i+1);
        }
            led.setColor(0,0,255); // stop bit blue
            System.out.println("Delta: " + time / symbols.length() + " amount: " + symbols.length());
        try {
                Sleeper.sleepNanos(delay * 1000000 *2);
            } catch (InterruptedException ex) {
                System.out.println("Error case in Sleep: " + ex);
            }
        //reset led to off after done with modulation
        led.setColor(0,0,0);
    }
}