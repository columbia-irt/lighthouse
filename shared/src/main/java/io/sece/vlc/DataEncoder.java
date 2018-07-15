package io.sece.vlc;

public interface DataEncoder {
    byte[] getPacket(int number);
    int minPacketSize();
    int maxPacketSize();
}
