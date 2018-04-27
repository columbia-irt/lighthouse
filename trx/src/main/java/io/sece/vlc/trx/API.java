package io.sece.vlc.trx;

import java.util.concurrent.ExecutorService;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


public class API {
    private int port;
    private HttpServer server;

    public API(int port) throws IOException {
        this.port = port;
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new RootHandler());
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
}
