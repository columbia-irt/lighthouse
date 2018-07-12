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
    private FECParameters fecParams;
    private SourceBlockDecoder sbd;
    private SourceBlockEncoder sbe;

    private byte[] data;
    private int packetSize;
    private int packetsReceived;

    public RaptorCode(byte[] data, int packetSize)
    {
        if(packetSize > 1)
        {
            this.data = data;
            this.packetsReceived = 0;
            this.packetSize = packetSize;

            fecParams = FECParameters.newParameters(data.length, packetSize - 1, 1);
            decoder = OpenRQ.newDecoder(fecParams, 0);
            sbd = decoder.sourceBlock(0);
            encoder = OpenRQ.newEncoder(data, fecParams);
            sbe = encoder.sourceBlock(0);
        }
        else
        {
            throw new IllegalArgumentException("packetSize");
        }
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
        if(data.length == packetSize) {
            packetsReceived += 1;
            int esi = data[0];
            byte[] tmp = new byte[data.length - 1];

            System.arraycopy(data, 1, tmp, 0, data.length - 1);

            sbd.putEncodingPacket(sbd.dataDecoder().parsePacket(0, esi, tmp, false).value());

            for(int i = 0; i < decoder.dataArray().length; i++)
            {
                System.out.println(i + ",Decoded: " + decoder.dataArray()[i] + " Encoded: " + this.data[i]);
            }
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    public boolean isDataReceived()
    {
        return (hamming(data, decoder.dataArray()) == 0);
    }

    public byte[] getPacket(int number)
    {
        packet = sbe.encodingPacket(number);

        byte[] encData = new byte[packetSize];

        encData[0] = (byte)number;
        System.arraycopy(packet.asArray(),8,encData, 1, fecParams.symbolSize());

        return encData;
    }

    public int progress()
    {
        if(this.isDataReceived())
            return 100;


        return (int)(100*((double) packetsReceived /(((double)data.length/((double)(packetSize - 1))) + 1)));
    }

}
