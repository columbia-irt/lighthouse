package io.sece.vlc.trx;

import java.awt.Color;
import io.sece.pigpio.PiGPIO;
import io.sece.pigpio.PiGPIOException;

class OOKModulator extends AmpModulator implements ModulatorInterface<BinSymbol>
{
    private static final Color on  = RGBColor.red;
    private static final Color off = RGBColor.off;
    private LEDInterface piLED;
    private String input;
    private long delay;
    private BinSymbol symbol;
    

    public OOKModulator(String input, LEDInterface led, int delay) throws PiGPIOException, InterruptedException
    {
        this.piLED = led;
        this.input = input;
        this.delay = delay;
        
        this.run();
    }
    
    public Color modulate(BinSymbol symbol) {
        switch(symbol.value) {
        case ONE:
            return on;

        case ZERO:
            return off;
        }
        throw new AssertionError();
    }
    
    private void run() throws PiGPIOException, InterruptedException
    {
        for(int i = 0; i < input.length(); i++)
        {
            switch(input.charAt(i))
            {
                case ('0') :
                    symbol = new BinSymbol(BinSymbol.Value.ZERO);
                    break;
                case ('1') :
                    symbol = new BinSymbol(BinSymbol.Value.ONE);
                    break;
                default :
                    System.out.println("Error case in Switch");                    
                    break;                
            }
            piLED.setColor(this.modulate(symbol));
            Sleeper.sleepNanos(delay * 1000000);
        }
    }
}
    /*
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
}*/