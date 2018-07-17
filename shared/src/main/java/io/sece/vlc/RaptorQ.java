package io.sece.vlc;

import net.fec.openrq.parameters.FECParameters;


abstract class RaptorQ {
    static final int OVERHEAD = 0;
    final FECParameters fecParams;
    private final int dataLength;
    final int packetSize;


    RaptorQ(int dataLength, int packetSize) {
        if (packetSize <= 0 || packetSize > dataLength)
            throw new IllegalArgumentException("Invalid packet size " + packetSize);

        this.dataLength = dataLength;
        this.packetSize = packetSize;
        fecParams = FECParameters.newParameters(dataLength, packetSize, 1);
    }


    public int minPacketSize() {
        int r = dataLength % packetSize;
        return r == 0 ? packetSize : r;
    }


    public int maxPacketSize() {
        return packetSize;
    }
}
