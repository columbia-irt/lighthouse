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
            for (int i = 0; i < this.hueValue.length; i++) {
                led.set(new Color(this.hueValue[i], brightness));
                Sleeper.sleep(duration, TimeUnit.SECONDS);
            }
        } catch (LEDException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
        } finally {
            try {
                led.set(Color.BLACK);
            } catch(LEDException e) { }
        }
    }


    @Override
    public String toString() {
        return "FPS: " + duration + " - hueValue length: " + hueValue.length + " - brightness: " + brightness;
    }
}