package io.sece.unix;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;


public class Sock {
    private int sock;
    public String src;
    private String dst;

    public Sock(String path) {
        sock = -1;
        src = null;
        dst = path;
    }

    public void open() throws IOException {
        sock = UnixDomainSock.socket();

        String tmp = System.getProperty("java.io.tmpdir");
        /* We need to bind the socket to a temporary file so that we can
         * receive responses from the smart object server. Hence, we use File
         * to create a file with a temporary filename and close the file
         * immediately so that the socket can be bound to it. Essentially, the
         * code is using java.io.File just to generate a unique filename on
         * the local filesystem. Note: there is a race condition where
         * somebody else could recreate the file between tmpFile.delete() and
         * socket.bind and in that case the bind will fail. */
        File tmpFile = File.createTempFile("io-sece-unix", ".sock");
        tmpFile.delete();
        UnixDomainSock.bind(sock, tmpFile.getAbsolutePath());
        tmpFile.setReadable(true);
        tmpFile.setWritable(true);
        src = tmpFile.getAbsolutePath();

        UnixDomainSock.connect(sock, dst);
    }

    public void close() {
        UnixDomainSock.close(sock);
    }

    public void send(byte[] message) throws SocketException {
        int cnt = 0;
        while (true) {
            try {
                UnixDomainSock.send(sock, message);
                return;
            } catch (SocketException e) {
                cnt += 1;
                if (cnt < 5) {
                    try {
                        UnixDomainSock.connect(sock, dst);
                    } catch (SocketException se) { }
                    continue;
                }
                throw e;
            }
        }
    }

    public byte[] recv() throws SocketException {
        return UnixDomainSock.recv(sock);
    }
}
