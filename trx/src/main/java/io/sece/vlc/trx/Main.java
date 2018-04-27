package io.sece.vlc.trx;

import io.sece.pigpio.PiGPIO;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

// PiGPIO.gpioSetMode(22, PiGPIO.PI_OUTPUT);
// PiGPIO.gpioSetMode(27, PiGPIO.PI_OUTPUT);
// PiGPIO.gpioSetMode(17, PiGPIO.PI_OUTPUT);
// PiGPIO.gpioPWM(22, 255);
// PiGPIO.gpioPWM(27, 100);
// PiGPIO.gpioPWM(17, 100);

public class Main {
  public static void main(String[] args) throws Exception {
      System.out.println("Starting LED transmitter");
      ExecutorService threadPool = Executors.newCachedThreadPool();

      System.out.println("Initializing pigpio library");
      PiGPIO.gpioInitialize();
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
