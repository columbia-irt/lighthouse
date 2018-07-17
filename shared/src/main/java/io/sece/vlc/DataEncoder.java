package io.sece.vlc;

public interface DataEncoder {
    byte[] getPacket(int seqNumber);
    int minPacketSize();
    int maxPacketSize();
}
