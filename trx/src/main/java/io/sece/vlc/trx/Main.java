package io.sece.vlc.trx;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import io.sece.pigpio.PiGPIO;


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

        System.out.println("LED transmitter is running");
    }
}
