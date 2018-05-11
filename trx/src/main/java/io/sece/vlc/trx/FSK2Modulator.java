package io.sece.vlc.trx;

import io.sece.pigpio.PiGPIOException;
import java.awt.Color;
/**
 *
 * @author Hagen
 */
public class FSK2Modulator extends FreqModulator implements ModulatorInterface<BinSymbol> {
    private static final Color red  = RGBColor.red;
    private static final Color blue = RGBColor.blue;
    private LEDInterface piLED;
    private String input;
    private long delay;
    private BinSymbol symbol;
    

    public FSK2Modulator(String input, LEDInterface led, int delay) throws PiGPIOException, InterruptedException
    {
        this.piLED = led;
        this.input = input;
        this.delay = delay;
        
        this.run();
    }

    public void run() throws PiGPIOException, InterruptedException
    {
        for(int i = 0; i < input.length(); i++)
        {
            switch(input.charAt(i))
            {
                case ('0') :
                    piLED.setColor(blue);
                    break;
                case ('1') :
                    piLED.setColor(red);
                    break;
                default :
                    piLED.setColor(RGBColor.white);                   
                    break;                
            }
            Sleeper.sleepNanos(delay * 1000000);
        }
    }
    
    
    /*public Color modulate(BinSymbol symbol) {
        switch(symbol.value) {
        case ONE:
            return red;

        case ZERO:
            return blue;
        }
        throw new AssertionError();
    }*/
}
