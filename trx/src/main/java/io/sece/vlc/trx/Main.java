package io.sece.vlc.trx;

import java.lang.IllegalArgumentException;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import io.sece.pigpio.PiGPIO;
import io.sece.pigpio.PiGPIOPin;
import io.sece.vlc.CalibrationModulator;
import io.sece.vlc.Color;
import io.sece.vlc.OOKModulator;
import io.sece.vlc.FSK2Modulator;
import io.sece.vlc.FSK4Modulator;
import io.sece.vlc.FSK8Modulator;


public class Main {


    public static void main(String[] args) throws Exception {

        boolean calibrate = true;

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

        PiGPIOPin r = new PiGPIOPin(22);
        PiGPIOPin g = new PiGPIOPin(27);
        PiGPIOPin b = new PiGPIOPin(17);

        // Create an instance of a mono color GPIO-only (no PWM) LED. This is
        // just for development and testing purposes.
        PiBasicLED led1 = new PiBasicLED(r);
        // Create an instance of a mono color PWM-controllable LED. This is
        // just for development and testing purposes
        PiPwmLED   led2 = new PiPwmLED(g);
        PiRgbLED   led3 = new PiRgbLED(r, g, b);

        //String which
        String data = "11111";


        System.out.println("LED transmitter is running");

        FSK2Modulator mod1 = new FSK2Modulator();
        FSK4Modulator mod2 = new FSK4Modulator();
        FSK8Modulator mod3 = new FSK8Modulator();
        OOKModulator  mod4 = new OOKModulator();


        Scanner in = new Scanner(System.in);

        if(calibrate) {
            while (true) {
                System.out.println("Hue Wert eingeben");
                String hueFromLine = in.nextLine();
                int hue = Integer.parseInt(hueFromLine);
                System.out.println("Hue Wert: " + hue);

                CalibrationModulator mod5 = new CalibrationModulator(hue, 100, 100);

                // Create an transmitter implementation which connects a particular
                // LEDInterface object to a particular Modulator. Note this should
                // enforce strict type checking and it should not be possible to
                // connect LEDs with incompatible modulators. That should generate a compile-time error.
                Transmitter<?> t = new Transmitter<>(led3, mod5, 2000);

                // Transmit the data stored in the buffer.
                t.tx(data);

                //testing purpose, make sure that the LED is off after any transmission
                led3.set(Color.BLACK);

                System.out.println("Done!");
            }
        }
        else
        {
            // Create an transmitter implementation which connects a particular
            // LEDInterface object to a particular Modulator. Note this should
            // enforce strict type checking and it should not be possible to
            // connect LEDs with incompatible modulators. That should generate a compile-time error.
            Transmitter<?> t = new Transmitter<>(led3, mod3, 2000);

            // Transmit the data stored in the buffer.
            t.tx(data);

            //testing purpose, make sure that the LED is off after any transmission
            led3.set(Color.BLACK);

            System.out.println("Done!");
        }
    }
}
