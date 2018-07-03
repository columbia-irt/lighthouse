package io.sece.vlc;

import java.math.BigInteger;
import java.util.zip.CRC32;

public class Framing {

    private String marker = "101100";
    private String data;
    private String checksum;

    public Framing(String marker, String data, String checksum)
    {
        this.marker = marker;
        this.data = data;
        this.checksum = checksum;
    }

    public Framing(String marker, String data)
    {
        this.marker = marker;
        this.data = data;
    }
    public Framing(String data)
    {
        this.data = data;
    }

    public String frame()
    {
        if(checksum == null)
        {
            checksum = calcChecksum();
        }
        return marker + data + checksum;
    }

    public String data()
    {

        if(this.checksum.equals(calcChecksum())) {
            return data;
        }
        else
        {
            return null;
        }
    }

    private String calcChecksum()
    {
        CRC32 crc = new CRC32();
        crc.update(this.data.getBytes());
        String binary = new BigInteger(String.valueOf(crc.getValue()).getBytes()).toString(2);
        return binary;
    }
}
