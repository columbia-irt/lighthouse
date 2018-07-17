package io.sece.vlc.trx;

import java.util.concurrent.TimeUnit;

import io.sece.vlc.BitString;
import io.sece.vlc.DataFrame;
import io.sece.vlc.LineCoder;
import io.sece.vlc.RaptorQEncoder;
import io.sece.vlc.modem.FSK2Modem;
import io.sece.vlc.modem.FSK4Modem;
import io.sece.vlc.modem.FSK8Modem;
import io.sece.vlc.Modem;

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
    public void run() {
        Transmitter<?> t;
        Modem mod;

        System.out.println(this.getModulator());
        switch (this.getModulator()) {
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
                throw new IllegalArgumentException("Unsupported modulator");
        }

        t = new Transmitter<>(led, mod, 1000000000 / this.getFPS(), TimeUnit.NANOSECONDS);

        RaptorQEncoder dataEncoder = new RaptorQEncoder(BitString.DEFAULT_DATA.data, DataFrame.MAX_PAYLOAD_SIZE);
        LineCoder lineCoder = new LineCoder(mod, DataFrame.MAX_SIZE);
        DataFrame dataFrame = new DataFrame();

        int i = 0;
        try {
            while (true) {
                if (Thread.interrupted()) break;

                dataFrame.seqNumber = i;
                dataFrame.payload = dataEncoder.getPacket(i);

                String bits = lineCoder.tx(dataFrame.encode());
                System.out.println(i + "\t" + bits);

                t.tx(bits);
                i = (i + 1) % 256;
            }
        } catch (LEDException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
        }
    }
}
