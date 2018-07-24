package io.sece.vlc.trx;

import java.util.concurrent.TimeUnit;
import io.sece.vlc.Color;


public class CalibrationTransmitter implements Runnable {
    public int duration;
    public int[] hueValue;
    private int brightness = 100;
    public ColorLEDInterface led;


    @Override
    public void run() {
        try {
            for (int hue : this.hueValue) {
                led.set(new Color(hue, brightness));
                Sleeper.sleep(duration, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            /* do nothing and terminate */
        } finally {
            led.set(Color.BLACK);
        }
    }


    @Override
    public String toString() {
        return "FPS: " + duration + " - hueValue length: " + hueValue.length + " - brightness: " + brightness;
    }
}