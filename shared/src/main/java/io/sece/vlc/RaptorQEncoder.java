package io.sece.vlc;

import net.fec.openrq.EncodingPacket;
import net.fec.openrq.OpenRQ;
import net.fec.openrq.encoder.SourceBlockEncoder;


public class RaptorQEncoder extends RaptorQ implements DataEncoder {
    private final SourceBlockEncoder encoder;


    public RaptorQEncoder(byte[] data, int packetSize) {
        super(data.length, packetSize);

        byte[] src = new byte[data.length];
        System.arraycopy(data, 0, src, 0, data.length);

        encoder = OpenRQ.newEncoder(src, fecParams).sourceBlock(0);
    }


    public byte[] getPacket(int number) {
        EncodingPacket p = encoder.encodingPacket(number % 256);

        byte[] data = new byte[1 + p.symbolsLength()];
        data[0] = (byte)p.encodingSymbolID();
        p.symbols().get(data, 1, p.symbolsLength());
        return data;
    }
}
