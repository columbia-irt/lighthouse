package io.sece.vlc.trx;

import io.sece.pigpio.PiGPIO;
import io.sece.pigpio.PiGPIOPin;
import java.awt.Color;
import java.lang.IllegalArgumentException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
// PiGPIO.gpioSetMode(22, PiGPIO.PI_OUTPUT);
// PiGPIO.gpioSetMode(27, PiGPIO.PI_OUTPUT);
// PiGPIO.gpioSetMode(17, PiGPIO.PI_OUTPUT);
// PiGPIO.gpioPWM(22, 255);
// PiGPIO.gpioPWM(27, 100);
// PiGPIO.gpioPWM(17, 100);

public class Main {
    public static LEDInterface piLED;
    public static ModulatorInterface<BinSymbol> piMod;
    public static PiGPIOPin redPin;
    public static PiGPIOPin greenPin;
    public static PiGPIOPin bluePin;

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

        redPin = new PiGPIOPin(22); // red
        greenPin = new PiGPIOPin(27); // green
        bluePin = new PiGPIOPin(17); // blue

        piLED = new TriColorLED(redPin, greenPin, bluePin);
        //piLED = new MonoColorLED(redPin.getGpio());

        final int delay = 100; //amount of time for sleep
        final int amount = 100; //amount for the for-loop
        final String input = "10"; //input string which gets multiplied in for-loop and stored in inputString

        String inputString = "";

        for(int i = 0; i < amount; i++)
        {
                inputString += input;
        }

        String finalString = "101011001100";
        System.out.println("LED transmitter is running");

        if(inputString.matches("[0-1]+"))
        {
            piMod = new FSK2Modulator(inputString, piLED, delay);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }
}
