package io.sece.vlc.trx;

import io.sece.vlc.Coordinate;
import io.sece.vlc.Modem;

/**
 * This class represents a LED transmitter which connects a LED with a
 * modulator and provides a method to transmit a packet of bits.
 *
 */
class Transmitter<T extends Coordinate> {
    private LEDInterface<T> led;
    private Modem<T> modem;
    private int interval;



    public Transmitter(LEDInterface<T> led, Modem<T> modem, int interval)
    {
        this.led = led;
        this.modem = modem;
        this.interval = interval;
    }

    public void tx(String data) throws LEDException, InterruptedException
    {
        if(data.length() % modem.bits == 0)
        {
            long startTime = System.currentTimeMillis();
            long delta = 0;
            int count = 1;
            
            for(int i = 0; i < data.length(); i += modem.bits)
            {
                led.set(modem.modulate(data, i));
                Sleeper.sleepNanos((interval - delta) * 1000000);
                delta = System.currentTimeMillis() - (startTime + (interval * count));
                count ++;
            }
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    public void startTx() throws LEDException, InterruptedException
    {
        int amount = 8; //Amount of symbols should be calculated through interval

        this.tx(modem.startSequence(amount));
        this.tx("1000001");

    }
}
