package io.sece.vlc.trx;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import io.sece.vlc.RaptorCodeEncoder;


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

        RaptorCodeEncoder rcp = new RaptorCodeEncoder();
        rcp.test();
    }
}
