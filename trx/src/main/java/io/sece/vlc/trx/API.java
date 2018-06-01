package io.sece.vlc.trx;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import io.sece.pigpio.PiGPIOPin;
import io.sece.vlc.CalibrationModulator;
import io.sece.vlc.Color;
import io.sece.vlc.FSK2Modulator;
import io.sece.vlc.FSK4Modulator;
import io.sece.vlc.FSK8Modulator;
import io.sece.vlc.Modulator;
import io.sece.vlc.OOKModulator;


public class API {
    private int port;
    private HttpServer server;
    private static String tID;
    private static boolean active;
    private static Thread threadCali;
    private static Thread threadTrans;
    private static Thread threadDog = new Thread();


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
                response = "Currently Running".getBytes();


                he.sendResponseHeaders(200, response.length);
                os.write(response);
            }
            else
            {
                active = true;
                tID = String.valueOf((int)(Math.random() * 901 + 100));
                response = tID.getBytes();


                he.sendResponseHeaders(200, response.length);


                try (Reader isr =  new InputStreamReader(he.getRequestBody(),"utf-8")) {
                    Gson gson = new GsonBuilder().create();
                    calibrationClass calC = gson.fromJson(isr, calibrationClass.class);
                    System.out.println(calC);

                    threadCali = new Thread(calC);
                    threadDog = new Thread(new watchDog(threadCali, calC.getDuration()*(calC.getHueValue().length + 1)));
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
                    response = "Failed reading json".getBytes();
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
                response = "Currently Running".getBytes();
                he.sendResponseHeaders(200, response.length);
                os.write(response);
            }
            else
            {
                active = true;
                tID = String.valueOf((int)(Math.random() * 901 + 100));
                response = tID.getBytes();
                he.sendResponseHeaders(200, response.length);

                try (Reader isr =  new InputStreamReader(he.getRequestBody(),"utf-8")) {
                    Gson gson = new GsonBuilder().create();
                    transmissionClass transC = gson.fromJson(isr, transmissionClass.class);
                    System.out.println(transC);

                    threadTrans = new Thread(transC);
                    threadDog = new Thread(new watchDog(threadCali, transC.getTimeout()));
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
                    response = "Failed reading json".getBytes();
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
                PiGPIOPin r = new PiGPIOPin(22);
                PiGPIOPin g = new PiGPIOPin(27);
                PiGPIOPin b = new PiGPIOPin(17);
                PiRgbLED led = new PiRgbLED(r, g, b);
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

