package io.sece.vlc.trx;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import io.sece.pigpio.PiGPIO;
import io.sece.vlc.RaptorCodeEncoder;


public class Main {


    public static void main(String[] args) throws Exception {

        System.out.println("Starting LED transmitter");
        ExecutorService threadPool = Executors.newCachedThreadPool();

        System.out.println("Initializing pigpio library");
        PiGPIO.gpioInitialise();
        Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
                System.out.println("Shutting down pigpio library");
                PiGPIO.gpioTerminate();
        }});

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
