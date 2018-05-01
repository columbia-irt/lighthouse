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
	
	public static TriColorLED piLED = new TriColorLED();
        public static LEDModulation piMod;
	
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
	  PiGPIO.gpioSetMode(22, PiGPIO.PI_OUTPUT); // red
	  PiGPIO.gpioSetMode(27, PiGPIO.PI_OUTPUT); // green
	  PiGPIO.gpioSetMode(17, PiGPIO.PI_OUTPUT); // blue
          
          final int delay = 50; //amount of time for sleep
          final int amount = 50; //amount for the for-loop 
          final String input = "10"; //input string which gets multiplied in for-loop
        
          String inputString = "";
        
          for(int i = 0; i < amount; i++)
          {
              inputString += input;
          }

      System.out.println("LED transmitter is running");
      
      piMod = new OOKModulation(piLED, delay);
      //piMod = new PolRZModulation(piLED, delay);
      piMod.setSymbols(inputString);
  }  
}
