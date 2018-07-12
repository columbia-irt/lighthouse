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
        if (data.length != packetSize)
            throw new IllegalArgumentException("data.length != packetSize");

        int esi = data[0] & 0xff;

        byte[] tmp = new byte[data.length - 1];
        System.arraycopy(data, 1, tmp, 0, tmp.length);

        sbd.putEncodingPacket(sbd.dataDecoder().parsePacket(0, esi, tmp, false).value());
    }


    public byte[] getPacket(int number) {
        packet = sbe.encodingPacket(number % 256);

        byte[] encData = new byte[packetSize];

        encData[0] = (byte)number;
        System.arraycopy(packet.asArray(),8,encData, 1, fecParams.symbolSize());
        return encData;
    }


    public boolean hasCompleted() {
        return sbd.isSourceBlockDecoded();
    }


    public float percentCompleted() {
        if (sbd.isSourceBlockDecoded())
            return 100f;

        float needed = sbd.numberOfSourceSymbols() + sbd.symbolOverhead();
        return 100f * sbd.availableRepairSymbols().size() / needed;
    }
}
