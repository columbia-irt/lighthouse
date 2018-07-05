package io.sece.vlc.trx;


import java.io.InputStreamReader;
import java.io.Reader;

import java.util.concurrent.ExecutorService;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import io.sece.pigpio.PiGPIOException;
import io.sece.pigpio.PiGPIOPin;

import io.sece.vlc.Color;


public class API {
    private int port;
    private HttpServer server;
    private static String tID;
    private static boolean active;
    private static Thread threadCali;
    private static Thread threadTrans;
    private static Thread threadDog = new Thread();
    private static LEDInterface led;


    public API(int port) throws IOException {
        tID = "";
        active = false;
        this.port = port;
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new RootHandler());
        server.createContext("/calibration", new calibrationHandler());
        server.createContext("/transmit", new transmissionHandler());
        server.createContext("/off", new offHandler());
        server.setExecutor(null);

        try
        {
            PiGPIOPin r = new PiGPIOPin(22);
            PiGPIOPin g = new PiGPIOPin(27);
            PiGPIOPin b = new PiGPIOPin(17);
            led = new PiRgbLED(r, g, b);
        }
        catch (PiGPIOException e)
        {
            System.out.println(e.getMessage());
        }

    }

    public void start(ExecutorService executor) {
        System.out.println("Starting HTTP API on port " + port);
        server.setExecutor(executor);
        server.start();
    }

    public void stop() {
        System.out.println("Stopping HTTP API");
        server.stop(0);
    }

    static class RootHandler implements HttpHandler {
        public void handle(HttpExchange he) throws IOException {
            byte[] response = "works".getBytes();
            he.sendResponseHeaders(200, response.length);
            OutputStream os = he.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    static class calibrationHandler implements HttpHandler {
        public void handle(HttpExchange he) throws IOException {

            byte [] response;

            OutputStream os = he.getResponseBody();

            if(threadDog != null && !threadDog.isAlive())
            {
                active = false;
                tID = "";
            }

            if(active)
            {
                response = ("{ response: \"Currently Running\" }").getBytes();


                he.sendResponseHeaders(200, response.length);
                os.write(response);
            }
            else
            {
                active = true;
                tID = String.valueOf((int)(Math.random() * 901 + 100));
                response = ("{ tID:" + tID + " }").getBytes();


                he.sendResponseHeaders(200, response.length);


                try (Reader isr =  new InputStreamReader(he.getRequestBody(),"utf-8")) {
                    Gson gson = new GsonBuilder().create();
                    calibrationClass calC = gson.fromJson(isr, calibrationClass.class);
                    calC.setLed((PiRgbLED)led);
                    System.out.println(calC);

                    threadCali = new Thread(calC);
                    threadDog = new Thread(new watchDog(threadCali, calC.getDuration()*(calC.getHueValue().length),(PiRgbLED)led));
                    threadDog.start();
                    threadCali.start();
                    os.write(response);
                    //testing purpose, make sure that the LED is off after any transmission
                }
                catch (Exception e)
                {
                    active = false;
                    tID = "";
                    System.out.println(e.getMessage());
                    response = ("{ response: \"Failed reading json\" }").getBytes();
                    os.write(response);
                }
            }
            os.close();
        }
    }


    static class transmissionHandler implements HttpHandler {
        public void handle(HttpExchange he) throws IOException {
            byte [] response;

            if(threadDog != null && !threadDog.isAlive())
            {
                active = false;
                tID = "";
            }

            OutputStream os = he.getResponseBody();
            if(active)
            {
                response = ("{ response: \"Currently Running\" }").getBytes();
                he.sendResponseHeaders(200, response.length);
                os.write(response);
            }
            else
            {
                active = true;
                tID = String.valueOf((int)(Math.random() * 901 + 100));
                response = ("{ tID:" + tID + " }").getBytes();
                he.sendResponseHeaders(200, response.length);

                try (Reader isr =  new InputStreamReader(he.getRequestBody(),"utf-8")) {
                    Gson gson = new GsonBuilder().create();
                    transmissionClass transC = gson.fromJson(isr, transmissionClass.class);
                    transC.setLed((PiRgbLED)led);
                    System.out.println(transC);

                    threadTrans = new Thread(transC);
                    threadDog = new Thread(new watchDog(threadTrans, transC.getTimeout(),(PiRgbLED)led));
                    threadDog.start();
                    threadTrans.start();

                    os.write(response);
                    //testing purpose, make sure that the LED is off after any transmission
                }
                catch (Exception e)
                {
                    active = false;
                    tID = "";
                    System.out.println(e.getMessage());
                    response = ("{ response: \"Failed reading json\" }").getBytes();
                    os.write(response);
                }
            }
            os.close();
        }
    }

    static class offHandler implements HttpHandler {
        public void handle(HttpExchange he) throws IOException {
            String jsonString = "";
            byte [] response;
            transmissionID transID;



            try (Reader isr =  new InputStreamReader(he.getRequestBody(),"utf-8")) {
                Gson gson = new GsonBuilder().create();
                transID = gson.fromJson(isr, transmissionID.class);
                if (!active) {
                    jsonString = "Not active";
                } else {
                    if (tID.equals(String.valueOf(transID.getID())))//tID muss be transmitted by client
                    {
                        jsonString = "you turned it off";
                        if (threadCali != null && threadCali.isAlive()) {
                            threadCali.stop();
                        }
                        if (threadTrans != null && threadTrans.isAlive()) {
                            threadTrans.stop();
                        }
                        if (threadDog != null && threadDog.isAlive()) {
                            threadDog.stop();
                        }
                        led.set(Color.BLACK);
                        tID = "";
                        active = false;
                    } else {
                        jsonString = "you are not allowed to turn the device off!";
                    }
                }
            }
            catch(Exception e)
                {
                    System.out.println(e.getMessage());
                }

            response = jsonString.getBytes();

            he.sendResponseHeaders(200, response.length);
            OutputStream os = he.getResponseBody();
            os.write(response);
            os.close();
        }
    }
}

