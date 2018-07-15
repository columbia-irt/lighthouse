package io.sece.vlc;

import net.fec.openrq.ArrayDataDecoder;
import net.fec.openrq.decoder.SourceBlockDecoder;
import net.fec.openrq.encoder.SourceBlockEncoder;
import net.fec.openrq.EncodingPacket;
import net.fec.openrq.OpenRQ;
import net.fec.openrq.parameters.FECParameters;

public class RaptorQ implements DataEncoder, DataDecoder {
    private static final int OVERHEAD = 1;

    private final ArrayDataDecoder decoder;
    private final net.fec.openrq.encoder.DataEncoder encoder;
    private final FECParameters fecParams;
    private final SourceBlockDecoder sbd;
    private final SourceBlockEncoder sbe;
    private final int packetSize;

    private EncodingPacket packet;
    private byte[] data;


    public RaptorQ(byte[] data, int packetSize) {
        if (packetSize <= 0 || packetSize > data.length)
            throw new IllegalArgumentException("Invalid packet size " + packetSize);

        this.data = data;
        this.packetSize = packetSize;

        fecParams = FECParameters.newParameters(data.length, packetSize - 1, 1);
        decoder = OpenRQ.newDecoder(fecParams, OVERHEAD);
        sbd = decoder.sourceBlock(0);
        encoder = OpenRQ.newEncoder(data, fecParams);
        sbe = encoder.sourceBlock(0);
    }


    public int hammingDistance() {
        return DataDecoder.hammingDistance(decoder.dataArray(), data);
    }


    public void putPacket(byte[] data) {
        putPacket(data, 0);
    }


    public void putPacket(byte[] data, int offset) {
        if (data.length - offset > packetSize)
            throw new IllegalArgumentException("data.length > packetSize");
        sbd.putEncodingPacket(sbd.dataDecoder().parsePacket(0, data[offset] & 0xff, data, offset + 1, data.length - offset - 1, true).value());
    }


    public int minPacketSize() {
        int r = data.length % packetSize;
        return r == 0 ? packetSize : r;
    }


    public int maxPacketSize() {
        return packetSize;
    }


    public byte[] getPacket(int number) {
        packet = sbe.encodingPacket(number % 256);

        byte[] data = new byte[1 + packet.symbolsLength()];
        data[0] = (byte)packet.encodingSymbolID();
        packet.symbols().get(data, 1, packet.symbolsLength());
        return data;
    }


    public boolean hasCompleted() {
        return sbd.isSourceBlockDecoded();
    }


    public float percentCompleted() {
        if (sbd.isSourceBlockDecoded())
            return 100f;

        float needed = sbd.numberOfSourceSymbols() + sbd.symbolOverhead();
        int ss = sbd.numberOfSourceSymbols() - sbd.missingSourceSymbols().size();
        return 100f * (ss + sbd.availableRepairSymbols().size()) / needed;
    }
}
