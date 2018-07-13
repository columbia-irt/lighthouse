package io.sece.vlc.trx;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import io.sece.vlc.CRC8;
import io.sece.vlc.DataBitString;
import io.sece.vlc.FramingBlock;
import io.sece.vlc.RaptorQ;


public class Main {
    public static ColorLEDInterface led;

    public static void main(String[] args) throws IOException, LEDException {

        System.out.println("Starting LED transmitter");
        ExecutorService threadPool = Executors.newCachedThreadPool();

        //led = new PiRgbLED(new PiGPIOPin(22), new PiGPIOPin(27), new PiGPIOPin(17));

        String driverName = System.getProperty("led.driver");
        if (driverName == null)
            throw new LEDException("Please provide LED driver name via the 'led.driver' system property");

        String params = System.getProperty("led.params");
        if (params == null)
            led = ColorLEDInterface.byName(driverName);
        else
            led = ColorLEDInterface.byName(driverName, params);

        API api = new API(8000);
        api.start(threadPool);

        /*try {
            PiGPIOPin r = new PiGPIOPin(22);
            PiGPIOPin g = new PiGPIOPin(27);
            PiGPIOPin b = new PiGPIOPin(17);
            PiRgbLED led = new PiRgbLED(r, g, b);

            Transmitter<?> t;
            Modulator mod = new FSK8Modulator();

            t = new Transmitter<>(led, mod, (1000/1));

            t.startTx();
            led.set(Color.BLACK);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }*/
        System.out.println("LED transmitter is running");

        String data;
        String received = "";
        RaptorQ raptor = new RaptorQ(DataBitString.stringToByte(DataBitString.DATA_BIT_STRING), 4);

        FramingBlock framingBlock = new FramingBlock();
        int i = 0;

        while(!raptor.hasCompleted())
        {
            byte[] tmp = raptor.getPacket(i);

            String test = DataBitString.bytesToString(tmp) + String.format("%8s", Integer.toBinaryString((int) (CRC8.compute(tmp) & 0xff))).replace(' ', '0');

            data = framingBlock.applyTX(test, 2);

            if(i == 0)
            {
                data = FramingBlock.STARTING_SEQUENCE + data + FramingBlock.STARTING_SEQUENCE;
            }
            else
            {
                data = data + FramingBlock.STARTING_SEQUENCE;
            }


            System.out.println(i + "    " + data);

            for(int j = 0; j < data.length(); j+= 2) {
                System.out.println(data.substring(j, j + 2));
                String tmpi = framingBlock.applyRX(data.substring(j, j + 2));
                if(tmpi != null) {
                    received = tmpi;
                }
            }
            System.out.println("receivedData: " + received);

            String currData = received.substring(0, received.length() - 8);
            System.out.println("currData: " + currData);
            String currCRC = received.substring(received.length() - 8, received.length());
            System.out.println("currCRC: " + currCRC);
            String calcCRC = String.format("%8s", Integer.toBinaryString((int)CRC8.compute(DataBitString.stringToByte(currData)))).replace(' ', '0');
            System.out.println("calcCRC: " + calcCRC);

            if(currCRC.equals(calcCRC))
            {
                System.out.println("CRC stimmt");
                raptor.putPacket(DataBitString.stringToByte(currData));
            }
            else
            {
                System.out.println("CRC falsch");
            }
            i += 2;
        }

        if(raptor.hammingDistance() == 0)
        {
            System.out.println("Complete!!!");
        }
        else
        {
            System.out.println("Finished but Wrong!");
        }

    }
}
