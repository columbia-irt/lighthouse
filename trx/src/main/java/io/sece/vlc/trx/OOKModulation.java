package io.sece.vlc.trx;

import io.sece.pigpio.PiGPIO;

class OOKModulation implements LEDModulation
{
    LEDInterface led;
    int delay;
    public OOKModulation(LEDInterface led, int delay)
    {
        this.led = led;
        this.delay = delay;
    }
    public void setSymbols(String symbols)
    {
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
                Thread.sleep(delay);
            } catch (InterruptedException ex) {
                System.out.println("Error case in Sleep: " + ex);
            }
        }
        
        led.setColor(0,255,0);
        
        try {
                Thread.sleep(delay * 2);
            } catch (InterruptedException ex) {
                System.out.println("Error case in Sleep: " + ex);
            }
        
        led.setColor(0,0,0);
    }
}