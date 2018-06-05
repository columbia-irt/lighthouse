package io.sece.vlc.trx;

import io.sece.pigpio.PiGPIOPin;
import io.sece.vlc.CalibrationModulator;
import io.sece.vlc.Color;

public class calibrationClass implements Runnable
{
    private int duration;
    private int timeout;
    private int[] hueValue;

    public int getDuration() {
        return duration;
    }

    public int[] getHueValue() {
        return hueValue;
    }

    @Override
    public String toString() {
        return "FPS: " + duration + " - hueValue length: " + hueValue.length + " - timeout:" + timeout;
    }

    @Override
    public void run()
    {
        try {
            PiGPIOPin r = new PiGPIOPin(22);
            PiGPIOPin g = new PiGPIOPin(27);
            PiGPIOPin b = new PiGPIOPin(17);
            PiRgbLED   led = new PiRgbLED(r, g, b);
            CalibrationModulator mod;
            Transmitter<?> t;

            for(int i = 0; i < this.getHueValue().length; i++)
            {
                mod = new CalibrationModulator(this.getHueValue()[i], 100, 100);
                t = new Transmitter<>(led, mod, (this.getDuration() * 1000));
                String data = "1";
                // Transmit the data stored in the buffer.
                t.tx(data);
            }

            // Create an transmitter implementation which connects a particular
            // LEDInterface object to a particular Modulator. Note this should
            // enforce strict type checking and it should not be possible to
            // connect LEDs with incompatible modulators. That should generate a compile-time error.

            led.set(Color.BLACK);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

}