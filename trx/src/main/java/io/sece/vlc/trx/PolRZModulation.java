package io.sece.vlc.trx;

import io.sece.pigpio.PiGPIO;

class PolRZModulation implements LEDModulation
{    
    LEDInterface led;
    int delay;
    public PolRZModulation(LEDInterface led, int delay)
    {
        this.led = led;
        this.delay = delay;
    }
    
    public void setSymbols(String symbols)
    {
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
            
            led.setColor(0,0,0);
            
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ex) {
                System.out.println("Error case in Sleep: " + ex);
            }
        }
        
        led.setColor(0,255,0);
        
        try {
                Thread.sleep(75);
            } catch (InterruptedException ex) {
                System.out.println("Error case in Sleep: " + ex);
            }
        
        led.setColor(0,0,0);
    }
}