package io.sece.vlc;

public interface DataDecoder {
    void putPacket(int seqNumber, byte[] data);
    float percentCompleted();
    boolean hasCompleted();
    int minPacketSize();
    int maxPacketSize();
    byte[] getData();
}