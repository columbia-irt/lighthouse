package io.sece.vlc.trx;

import io.sece.vlc.Modulator;

/**
 * This class represents a LED transmitter which connects a LED with a
 * modulator and provides a method to transmit a packet of bits.
 *
 */
class Transmitter<T> {
    private LEDInterface<T> led;
    private Modulator<T> modulator;
    private int interval;

    public Transmitter(LEDInterface<T> led, Modulator<T> modulator, int interval)
    {
        this.led = led;
        this.modulator = modulator;
        this.interval = interval;
    }

    public void tx(String data) throws LEDException, InterruptedException
    {
        if(data.length() % modulator.bits == 0)
        {
            long startTime = System.currentTimeMillis();
            long delta = 0;
            int count = 1;
            for(int i = 0; i < data.length(); i += modulator.bits)
            {
                led.set(modulator.modulate(data, i));
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
}
