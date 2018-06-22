package io.sece.vlc.trx;

import io.sece.pigpio.PiGPIOPin;
import io.sece.vlc.CalibrationModulator;
import io.sece.vlc.Color;
import io.sece.vlc.FSK2Modulator;
import io.sece.vlc.FSK4Modulator;
import io.sece.vlc.FSK8Modulator;
import io.sece.vlc.Modulator;
import io.sece.vlc.OOKModulator;

public class transmissionClass implements Runnable {
    private int FPS;
    private int timeout;
    private String modulator;

    public int getFPS() {
        return FPS;
    }

    public String getModulator() {
        return modulator;
    }

    public int getTimeout() {
        return timeout;
    }

    @Override
    public String toString() {
        return "FPS: " + FPS + " - timeout:" + timeout + " - modulator: " + modulator;
    }

    @Override
    public void run()
    {
        try {
            PiGPIOPin r = new PiGPIOPin(22);
            PiGPIOPin g = new PiGPIOPin(27);
            PiGPIOPin b = new PiGPIOPin(17);
            PiRgbLED led = new PiRgbLED(r, g, b);

            Transmitter<?> t;
            Modulator mod;

            System.out.println(this.getModulator());
            switch (this.getModulator())
            {
                case "fsk2":
                    mod = new FSK2Modulator();
                    break;
                case "fsk4":
                    mod = new FSK4Modulator();
                    break;
                case "fsk8":
                    mod = new FSK8Modulator();
                    break;
                default:
                    System.out.println("default");
                    mod = new OOKModulator();
                    break;
            }
            // Create an transmitter implementation which connects a particular
            // LEDInterface object to a particular Modulator. Note this should
            // enforce strict type checking and it should not be possible to
            // connect LEDs with incompatible modulators. That should generate a compile-time error.
            t = new Transmitter<>(led, mod, (1000/this.getFPS()));

            String data = "01011010";


            // Transmit the data stored in the buffer.
            t.tx(data);
            led.set(Color.BLACK);

        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}
