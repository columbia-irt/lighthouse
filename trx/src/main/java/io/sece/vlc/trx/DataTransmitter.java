package io.sece.vlc.trx;

import java.util.Random;

import io.sece.vlc.FSK2Modem;
import io.sece.vlc.FSK4Modem;
import io.sece.vlc.FSK8Modem;
import io.sece.vlc.Modem;
import io.sece.vlc.OOKModem;

public class DataTransmitter implements Runnable {
    private int FPS;
    private int timeout;
    private String modulator;
    private ColorLEDInterface led;

    public int getFPS() {
        return FPS;
    }

    public String getModulator() {
        return modulator;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setLed(ColorLEDInterface led) {
        this.led = led;
    }

    @Override
    public String toString() {
        return "FPS: " + FPS + " - timeout:" + timeout + " - modulator: " + modulator;
    }



    @Override
    public void run()
    {
        try {
            Transmitter<?> t;
            Modem mod;

            System.out.println(this.getModulator());
            switch (this.getModulator())
            {
                case "fsk2":
                    mod = new FSK2Modem();
                    break;
                case "fsk4":
                    mod = new FSK4Modem();
                    break;
                case "fsk8":
                    mod = new FSK8Modem();
                    break;
                default:
                    System.out.println("default");
                    mod = new OOKModem();
                    break;
            }
            // Create an transmitter implementation which connects a particular
            // LEDInterface object to a particular Modulator. Note this should
            // enforce strict type checking and it should not be possible to
            // connect LEDs with incompatible modulators. That should generate a compile-time error.
            t = new Transmitter<>(led, mod, (1000/this.getFPS()));

            String data = mod.startSequence(2) + "11110000111100001111";

            Random rand = new Random();


            // Transmit the data stored in the buffer.
            while(true) {
                try
                {
                    System.out.println(data);
                    t.tx(data);
                }
                catch (LEDException|InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
                try
                {
                    Thread.sleep((rand.nextInt(2500) + 500));
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
            //t.startTx();
            //led.set(Color.BLACK);

        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException();
        }
    }
}
