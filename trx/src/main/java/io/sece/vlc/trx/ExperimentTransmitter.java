package io.sece.vlc.trx;

import java.util.concurrent.TimeUnit;

import io.sece.vlc.Color;

public class ExperimentTransmitter implements Runnable{
    public ColorLEDInterface led;
    public int duration;

    @Override
    public void run() {
        try {
            for (int i = 0; i < 360; i++) {
                led.set(new Color(i, 100));
                Sleeper.sleep(duration, TimeUnit.SECONDS);

                led.set(Color.BLACK);
                Sleeper.sleep(100, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            /* do nothing and terminate */
        } finally {
            led.set(Color.BLACK);
        }
    }

    @Override
    public String toString() {
        return "Experiment is running for the next: " + ((duration * 360) + 36) + " seconds";
    }
}
