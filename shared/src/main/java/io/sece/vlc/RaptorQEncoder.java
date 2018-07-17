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


    public byte[] getPacket(int seqNumber) {
        EncodingPacket p = encoder.encodingPacket(seqNumber);
        byte[] data = new byte[p.symbolsLength()];
        p.symbols().get(data, 0, p.symbolsLength());
        return data;
    }
}
