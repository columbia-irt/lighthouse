package io.sece.vlc;

import net.fec.openrq.ArrayDataDecoder;
import net.fec.openrq.OpenRQ;
import net.fec.openrq.decoder.SourceBlockDecoder;


public class RaptorQDecoder extends RaptorQ implements DataDecoder {
    private ArrayDataDecoder dataDecoder;
    private final SourceBlockDecoder blockDecoder;

    public RaptorQDecoder(int dataLength, int packetSize) {
        super(dataLength, packetSize);

        dataDecoder = OpenRQ.newDecoder(fecParams, OVERHEAD);
        blockDecoder = dataDecoder.sourceBlock(0);
    }


    public void putPacket(byte[] data) {
        putPacket(data, 0);
    }


    public void putPacket(byte[] data, int offset) {
        if (data.length - offset > packetSize)
            throw new IllegalArgumentException("data.length > packetSize");
        blockDecoder.putEncodingPacket(blockDecoder.dataDecoder().parsePacket(0, data[offset] & 0xff, data, offset + 1, data.length - offset - 1, true).value());
    }


    public boolean hasCompleted() {
        return blockDecoder.isSourceBlockDecoded();
    }


    public float percentCompleted() {
        if (hasCompleted())
            return 100f;

        float needed = blockDecoder.numberOfSourceSymbols() + blockDecoder.symbolOverhead();
        int ss = blockDecoder.numberOfSourceSymbols() - blockDecoder.missingSourceSymbols().size();
        return 100f * (ss + blockDecoder.availableRepairSymbols().size()) / needed;
    }


    public byte[] getData() {
        return dataDecoder.dataArray();
    }
}
