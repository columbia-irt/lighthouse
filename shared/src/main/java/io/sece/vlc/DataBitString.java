package io.sece.vlc;

public class DataBitString {
    private static final String DATA_BIT_STRING = "1000001101011110010110111001100010110100100011101000010110110100001111000011101001110010110110010010001110010011101000011101011000011110110111010011111100100001011100111010011010000000101101100010101111011101110010111111111000110001101000010101001001011010";

    public static byte[] dataBitString()
    {
        byte[] data = new byte[DATA_BIT_STRING.length()/8];

        for(int i = 0; i < data.length; i++)
        {
            data[i] = (byte)Integer.parseInt(DATA_BIT_STRING.substring(i*8,(i*8) + 8),2);
        }

        return data;
    }

    public static String bytesToString(byte[] data)
    {
        String tmp = "";

        for(int i = 0; i < data.length; i++)
        {
            tmp += String.format("%8s", Integer.toBinaryString((int)(data[i]&0xff))).replace(' ', '0');
        }
        return tmp;
    }
}
