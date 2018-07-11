package io.sece.vlc;

import net.fec.openrq.ArrayDataDecoder;
import net.fec.openrq.decoder.SourceBlockDecoder;
import net.fec.openrq.encoder.DataEncoder;
import net.fec.openrq.encoder.SourceBlockEncoder;
import net.fec.openrq.EncodingPacket;
import net.fec.openrq.OpenRQ;
import net.fec.openrq.parameters.FECParameters;

public class RaptorCode
{
    private ArrayDataDecoder decoder;
    private DataEncoder encoder;
    private EncodingPacket packet;
    private FECParameters FEC_PARAMS;
    private SourceBlockDecoder sbd;
    private SourceBlockEncoder sbe;

    private byte[] certificateData;
    private int symbolAmount;
    private int packetCount;


    public RaptorCode(byte[] certificateData, int symbolAmount)
    {
        this.certificateData = certificateData;
        this.packetCount = 0;
        this.symbolAmount = symbolAmount;

        FEC_PARAMS = FECParameters.newParameters(certificateData.length, symbolAmount,1);
        decoder = OpenRQ.newDecoder(FEC_PARAMS, 0);
        sbd = decoder.sourceBlock(0);
        encoder = OpenRQ.newEncoder(certificateData, FEC_PARAMS);
        sbe = encoder.sourceBlock(0);
    }

    static private int hamming(byte[] a, byte[] b) {
        if (a.length != b.length)
            throw new IllegalArgumentException("a.length != b.length");

        int rv = 0;
        for(int i = 0; i < a.length; i++)
            rv += Integer.bitCount((a[i] & 0xff) ^ (b[i] & 0xff));
        return rv;
    }

    public void putPacket(byte[] data)
    {
        packetCount += 1;
        int esi = data[0];
        byte[] tmp = new byte[data.length - 1];

        System.arraycopy(data,1,tmp, 0,data.length -1);

        sbd.putEncodingPacket(sbd.dataDecoder().parsePacket(0, esi, tmp, false).value());
    }

    public boolean isDataReceived()
    {
        return (hamming(certificateData, decoder.dataArray()) == 0);
    }

    public byte[] getPacket(int number)
    {
        packet = sbe.encodingPacket(number);

        byte[] encData = new byte[FEC_PARAMS.symbolSize()+1];

        encData[0] = (byte)number;
        System.arraycopy(packet.asArray(),8,encData, 1,FEC_PARAMS.symbolSize());

        return encData;
    }

    public int progress()
    {
        if(this.isDataReceived())
            return 100;


        return (int)(100*((double)packetCount/(((double)certificateData.length/(double)symbolAmount) + 1)));
    }

}
