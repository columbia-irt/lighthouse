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


    public void putPacket(int seqNumber, byte[] data) {
        putPacket(seqNumber, data, 0, data.length);
    }


    public void putPacket(int seqNumber, byte[] data, int offset, int length) {
        if (length > packetSize)
            throw new IllegalArgumentException("length > packetSize");
        blockDecoder.putEncodingPacket(blockDecoder.dataDecoder().parsePacket(0, seqNumber, data, offset, length, true).value());
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
