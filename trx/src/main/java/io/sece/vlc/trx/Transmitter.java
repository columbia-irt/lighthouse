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
    private static final long NO_TIMESTAMP = Long.MIN_VALUE;
    private LEDInterface<T> led;
    private Modem<T> modem;
    private long interval;
    private long timestamp = NO_TIMESTAMP;


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
        if (data.length() % modem.bits != 0)
            throw new IllegalArgumentException("Invalid number of bits");

        int n = data.length() / modem.bits;

        if (timestamp == NO_TIMESTAMP)
            timestamp = System.nanoTime();

        for (int i = 0; i < n; i++) {
            led.set(modem.modulate(data, i * modem.bits));
            timestamp += interval;
            Sleeper.sleepNanos(timestamp - System.nanoTime());
        }
    }
}
