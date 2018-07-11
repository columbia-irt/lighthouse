package io.sece.vlc;

import net.fec.openrq.EncodingPacket;
import net.fec.openrq.OpenRQ;
import net.fec.openrq.encoder.DataEncoder;
import net.fec.openrq.encoder.SourceBlockEncoder;


public class RaptorCodeEncoder {

    private final DataEncoder dataEnc = OpenRQ.newEncoder(RaptorCodeParam.data(), RaptorCodeParam.FEC_PARAMS);

    public void test()
    {


        final DataEncoder dataEnc = OpenRQ.newEncoder(RaptorCodeParam.data(), RaptorCodeParam.FEC_PARAMS);

            SourceBlockEncoder sbe = dataEnc.sourceBlock(0);
            System.out.println("Data Length: " + dataEnc.dataLength() + " number of Sourceblocks: " + dataEnc.numberOfSourceBlocks() + " number of source symbols: " + sbe.numberOfSourceSymbols());

        for (int i = 0; i < 255; i++) {
            EncodingPacket packet = sbe.encodingPacket(i);
            System.out.println("encoding " + packet + " " + packet.symbolType());

            byte[] tmp = packet.asArray();
            byte[] encData = new byte[2];
            encData[0] = tmp[3];
            encData[1] = tmp[8];
        }

        /*while(itPackets.iterator().hasNext())
        {
            //System.out.println(j);
            itPackets.iterator().next();
            j++;
        }*/


    }

    public byte[] getNextPackage(int number)
    {

        SourceBlockEncoder sbe = dataEnc.sourceBlock(0);

        EncodingPacket packet = sbe.encodingPacket(number);
        //System.out.println("encoding " + packet + " " + packet.symbolType());

        byte[] tmp = packet.asArray();
        byte[] encData = new byte[RaptorCodeParam.FEC_PARAMS.symbolSize()+1];
        encData[0] = tmp[3];
        for(int i = 0; i < RaptorCodeParam.FEC_PARAMS.symbolSize(); i++)
        {
            encData[i + 1] = tmp[8 + i];
        }



        return encData;
    }




}
