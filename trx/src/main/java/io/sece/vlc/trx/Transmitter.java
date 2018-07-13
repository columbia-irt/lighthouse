package io.sece.vlc.trx;

import java.util.concurrent.TimeUnit;

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
    private long interval;


    public Transmitter(LEDInterface<T> led, Modem<T> modem, int interval, TimeUnit unit)
    {
        this.led = led;
        this.modem = modem;
        this.interval = unit.toNanos(interval);
    }


    public Transmitter(LEDInterface<T> led, Modem<T> modem, int interval) {
        this(led, modem, interval, TimeUnit.MILLISECONDS);
    }


    public void tx(String data) throws LEDException, InterruptedException
    {
        long deadline;

        if (data.length() % modem.bits != 0)
            throw new IllegalArgumentException("Invalid number of bits");

        int n = data.length() / modem.bits;
        long start = System.nanoTime();

        for (int i = 0; i < n; i++) {
            led.set(modem.modulate(data, i * modem.bits));
            deadline = start + (interval * (i + 1));
            Sleeper.sleepNanos(deadline - System.nanoTime());
        }
    }


    public void startTx() throws LEDException, InterruptedException
    {
        int amount = 8; //Amount of symbols should be calculated through interval
        this.tx(modem.startSequence(amount));
        this.tx("1000001");
    }
}
