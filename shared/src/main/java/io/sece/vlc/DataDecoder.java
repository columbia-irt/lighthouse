package io.sece.vlc;

public interface DataDecoder {
    static int hammingDistance(byte[] a, byte[] b) {
        if (a.length != b.length)
            throw new IllegalArgumentException("a.length != b.length");

        int rv = 0;
        for (int i = 0; i < a.length; i++)
            rv += Integer.bitCount((a[i] & 0xff) ^ (b[i] & 0xff));
        return rv;
    }

    int hammingDistance();
    void putPacket(byte[] data);
    float percentCompleted();
    boolean hasCompleted();
}