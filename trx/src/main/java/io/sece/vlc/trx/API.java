package io.sece.vlc.trx;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import io.sece.pigpio.PiGPIOPin;
import io.sece.vlc.CalibrationModulator;
import io.sece.vlc.Color;


public class API {
    private int port;
    private HttpServer server;

    public API(int port) throws IOException {
        this.port = port;
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new RootHandler());
        server.createContext("/calibration", new calibrationHandler());
        server.createContext("/calibrationJson", new hueValueHandler());
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
        public void handle(HttpExchange t) throws IOException {
            byte [] response = "works".getBytes();
            t.sendResponseHeaders(200, response.length);
            OutputStream os = t.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    static class calibrationHandler implements HttpHandler {
        public void handle(HttpExchange he) throws IOException {
            Map<String, Object> parameters = new HashMap<String, Object>();
            URI requestedUri = he.getRequestURI();
            String query = requestedUri.getRawQuery();
            parseQuery(query, parameters);
            int hue = 300;
            int duration = 2000;
            // send response
            String response = "";
            for (String key : parameters.keySet()) {
                response += key + " = " + parameters.get(key) + "\n";
                if(key.equals("hueValue"))
                {
                    hue = Integer.parseInt(parameters.get(key).toString());
                }
                else if(key.equals("duration"))
                {
                    duration = Integer.parseInt(parameters.get(key).toString());
                }
            }
            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.toString().getBytes());

            try
            {
                PiGPIOPin r = new PiGPIOPin(22);
                PiGPIOPin g = new PiGPIOPin(27);
                PiGPIOPin b = new PiGPIOPin(17);
                PiRgbLED   led3 = new PiRgbLED(r, g, b);

                CalibrationModulator mod5 = new CalibrationModulator(hue, 100, 100);

                // Create an transmitter implementation which connects a particular
                // LEDInterface object to a particular Modulator. Note this should
                // enforce strict type checking and it should not be possible to
                // connect LEDs with incompatible modulators. That should generate a compile-time error.
                Transmitter<?> t = new Transmitter<>(led3, mod5, duration);

                String data = "11";
                // Transmit the data stored in the buffer.

                t.tx(data);
                led3.set(Color.BLACK);
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
            }

            //testing purpose, make sure that the LED is off after any transmission

            os.close();
        }
    }


    static class hueValueHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            byte [] response = "Json".getBytes();
            t.sendResponseHeaders(200, response.length);
            OutputStream os = t.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    public static void parseQuery(String query, Map<String,
            Object> parameters) throws UnsupportedEncodingException {

        if (query != null) {
            String pairs[] = query.split("[&]");
            for (String pair : pairs) {
                String param[] = pair.split("[=]");
                String key = null;
                String value = null;
                if (param.length > 0) {
                    key = URLDecoder.decode(param[0],
                            System.getProperty("file.encoding"));
                }

                if (param.length > 1) {
                    value = URLDecoder.decode(param[1],
                            System.getProperty("file.encoding"));
                }

                if (parameters.containsKey(key)) {
                    Object obj = parameters.get(key);
                    if (obj instanceof List<?>) {
                        List<String> values = (List<String>) obj;
                        values.add(value);

                    } else if (obj instanceof String) {
                        List<String> values = new ArrayList<String>();
                        values.add((String) obj);
                        values.add(value);
                        parameters.put(key, values);
                    }
                } else {
                    parameters.put(key, value);
                }
            }
        }
    }
}
