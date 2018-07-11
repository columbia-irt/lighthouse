package io.sece.vlc;

import java.math.BigInteger;
import java.util.zip.CRC32;

public class DataFrame {

    private String marker = "1001";
    private String data;
    private String checksum;
    private int expectedBitsAmount;

    private String stuffing = "00";

    public DataFrame(String marker, String data, String checksum, int expectedBitsAmount)
    {
        this.marker = marker;
        this.data = data;
        this.checksum = checksum;
        this.expectedBitsAmount = expectedBitsAmount;
    }

    public DataFrame(String marker, String data, int expectedBitsAmount)
    {
        this.marker = marker;
        this.data = data;
        this.expectedBitsAmount = expectedBitsAmount;
    }
    public DataFrame(String data,  int expectedBitsAmount)
    {
        this.data = data;
        this.expectedBitsAmount = expectedBitsAmount;
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
        if(data.length() > expectedBitsAmount){
            data = data.substring(0, expectedBitsAmount);
        }
        System.out.println(data);
        return StuffingBlock.removeStuffingBits(data);

//        if(this.checksum.equals(calcChecksum())) {
//            return data;
//        }
//        else
//        {
//            return null;
//        }
    }






    private String calcChecksum()
    {
//        CRC32 crc = new CRC32();
//        crc.update(this.data.getBytes());
//        String binary = new BigInteger(String.valueOf(crc.getValue()).getBytes()).toString(2);
//        return binary;
        return null;
    }
}
