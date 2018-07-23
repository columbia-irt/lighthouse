package io.sece.vlc.trx;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.sece.vlc.BitVector;
import io.sece.vlc.Color;
import io.sece.vlc.DataFrame;
import io.sece.vlc.LineCoder;
import io.sece.vlc.RaptorQEncoder;
import io.sece.vlc.Symbol;
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
        Transmitter trx;
        Modem<?> modem;

        System.out.println(this.getModulator());
        switch (this.getModulator()) {
            case "fsk2":
                modem = new FSK2Modem();
                break;

            case "fsk4":
                modem = new FSK4Modem();
                break;

            case "fsk8":
                modem = new FSK8Modem();
                break;

            default:
                throw new IllegalArgumentException("Unsupported modulator");
        }

        Symbol symbol = new Symbol(modem.states);
        RaptorQEncoder dataEncoder = new RaptorQEncoder(BitVector.DEFAULT_DATA.data, DataFrame.MAX_PAYLOAD_SIZE);
        LineCoder lineCoder = new LineCoder(new int[] {1, 3, 2});
        DataFrame dataFrame = new DataFrame();

        trx = new Transmitter<>(led, 1000000000 / this.getFPS(), TimeUnit.NANOSECONDS);
        Thread t = new Thread(trx);
        t.start();

        int i = 0;
        try {
            while (true) {
                dataFrame.seqNumber = i;
                dataFrame.payload = dataEncoder.getPacket(i);

                BitVector bits = dataFrame.pack();
                System.out.println(i + "\t" + bits.toString());
                trx.enqueue(modem.modulate(lineCoder.encode(symbol.fromBits(bits))));
                i = (i + 1) % 256;
            }
        } catch (InterruptedException e) {
            t.interrupt();
        } catch (LineCoder.FrameTooLong e) {
            throw new RuntimeException(e);
        }

        try {
            t.join(1000);
        } catch (InterruptedException e) {
            System.out.println("Transmitter thread refused to die");
        }
    }
}
