package io.sece.vlc.trx;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import io.sece.vlc.BitVector;
import io.sece.vlc.Color;
import io.sece.vlc.DataFrame;
import io.sece.vlc.LineCoder;
import io.sece.vlc.RaptorQEncoder;
import io.sece.vlc.Symbol;
import io.sece.vlc.modem.FSK4Modem;


class WebLED implements HttpHandler {
    public void handle(HttpExchange he) throws IOException {
        OutputStream os = he.getResponseBody();
        String method = he.getRequestMethod().toUpperCase();

        if (method.equals("GET") || method.equals("HEAD")) {
            URL html = API.class.getResource(File.separator + "webled.html");
            if (html == null) {
                he.sendResponseHeaders(500, -1);
            } else {
                he.sendResponseHeaders(200, 0);
                Files.copy(new File(html.getFile()).toPath(), os);
            }
        } else {
            final Headers headers = he.getResponseHeaders();
            headers.set("Content-Type", String.format("application/json; charset=%s", StandardCharsets.UTF_8));
            he.sendResponseHeaders(200, 0);

            FSK4Modem modem = new FSK4Modem();
            Symbol symbol = new Symbol(modem.states);
            RaptorQEncoder dataEncoder = new RaptorQEncoder(BitVector.DEFAULT_DATA.data, DataFrame.MAX_PAYLOAD_SIZE);
            LineCoder lineCoder = new LineCoder(new int[] {1, 3, 2});
            DataFrame dataFrame = new DataFrame();

            Writer o = new OutputStreamWriter(os, StandardCharsets.UTF_8);

            o.write("[");
            boolean first = true;
            try {
                List<Color> waveform;
                for (int i = 0; i < 256; i++) {
                    dataFrame.seqNumber = i;
                    dataFrame.payload = dataEncoder.getPacket(i);
                    try {
                        waveform = modem.modulate(lineCoder.encode(symbol.fromBits(dataFrame.pack())));
                        for (Color c : waveform) {
                            o.write(String.format("%s\"%s\"", first ? "" : ",", c));
                            first = false;
                        }
                    } catch (LineCoder.FrameTooLong e) {
                        throw new RuntimeException("Bug in web LED transmitter", e);
                    }
                }
            } finally {
                o.write("]");
                o.flush();
            }
        }
        os.close();
    }
}
