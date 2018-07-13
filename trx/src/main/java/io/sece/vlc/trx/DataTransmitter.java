package io.sece.vlc.trx;

import java.util.concurrent.TimeUnit;

import io.sece.vlc.CRC8;
import io.sece.vlc.DataBitString;
import io.sece.vlc.FramingBlock;
import io.sece.vlc.RaptorQ;
import io.sece.vlc.modem.FSK2Modem;
import io.sece.vlc.modem.FSK4Modem;
import io.sece.vlc.modem.FSK8Modem;
import io.sece.vlc.Modem;
import io.sece.vlc.modem.OOKModem;

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
            t = new Transmitter<>(led, mod, 1000000000/this.getFPS(), TimeUnit.NANOSECONDS);


            String data;

            RaptorQ raptor = new RaptorQ(DataBitString.stringToByte(DataBitString.DATA_BIT_STRING), 3);
            FramingBlock framingBlock = new FramingBlock();


            for(int i = 0; i < 256; i++)
            {
                byte[] tmp = raptor.getPacket(i);

                byte[] tmp2 = new byte[tmp.length + 1];
                tmp2[0] = (byte)CRC8.compute(tmp);
                System.arraycopy(tmp, 0, tmp2, 1, tmp.length);

                String test = DataBitString.bytesToString(tmp2);

                //String test = DataBitString.bytesToString(tmp) + String.format("%8s", Integer.toBinaryString((int)(CRC8.compute(tmp)&0xff))).replace(' ', '0');

                data = framingBlock.applyTX(test, mod.bits);

                data = FramingBlock.STARTING_SEQUENCE + data;

                try
                {
                    System.out.println(i + "    " + data);
                    t.tx(data);
                }
                catch (LEDException|InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }


            // Transmit the data stored in the buffer.
            /*while(true) {
                try
                {
                    //System.out.println(data);
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
            }*/
            //t.startTx();
            //led.set(Color.BLACK);

        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException();
        }
    }
}
