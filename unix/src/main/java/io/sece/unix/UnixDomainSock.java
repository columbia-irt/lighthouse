package io.sece.unix;

import java.net.SocketException;

public class UnixDomainSock {
    private UnixDomainSock() { }

    public static native int socket() throws SocketException;

    public static native void close(int sock);

    public static native void connect(int sock, String path)
        throws SocketException;

    public static native void bind(int sock, String path)
        throws SocketException;

    public static native int send(int sock, byte[] data)
        throws SocketException;

    public static native byte[] recv(int sock) throws SocketException;

    static {
        System.loadLibrary("unix-java");
    }
}
