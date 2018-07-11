package io.sece.vlc;

import net.fec.openrq.ArrayDataDecoder;
import net.fec.openrq.OpenRQ;
import net.fec.openrq.decoder.SourceBlockDecoder;

public class RaptorCodeDecode
{
    private ArrayDataDecoder decoder = OpenRQ.newDecoder(RaptorCodeParam.FEC_PARAMS, 0);
    private SourceBlockDecoder sbd = decoder.sourceBlock(0);

    /*static private int hamming(byte[] a, byte[] b) {
        if (a.length != b.length)
            throw new IllegalArgumentException("a.length != b.length");

        int rv = 0;
        for(int i = 0; i < a.length; i++)
            rv += Integer.bitCount((a[i] & 0xff) ^ (b[i] & 0xff));
        return rv;
    }*/

    public boolean receiveNextPackage(byte[] data)
    {
        int esi = data[0];
        byte[] tmp = new byte[data.length - 1];

        for (int i = 0; i < data.length -1; i++)
        {
            tmp[i] = data[i + 1];
        }

        sbd.putEncodingPacket(sbd.dataDecoder().parsePacket(0, esi, tmp, false).value());

        //System.out.println(("distance: " + hamming(RaptorCodeParam.data(), decoder.dataArray())));

        for(int i = 0; i < decoder.dataArray().length; i++)
        {
            System.out.println(i + ",Decoded: " + decoder.dataArray()[i] + " Encoded: " + RaptorCodeParam.data()[i]);
        }

        return decoder.isDataDecoded();//(hamming(RaptorCodeParam.data(), decoder.dataArray()) == 0);

    }
}
