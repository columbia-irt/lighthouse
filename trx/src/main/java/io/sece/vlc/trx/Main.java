package io.sece.vlc.trx;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;


public class Main {
    public static ColorLEDInterface led;

    public static void main(String[] args) throws IOException {

        System.out.println("Starting LED transmitter");
        ExecutorService threadPool = Executors.newCachedThreadPool();

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

        System.out.println("LED transmitter is running");
    }
}
