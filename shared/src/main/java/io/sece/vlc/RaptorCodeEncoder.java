package io.sece.vlc;

import net.fec.openrq.EncodingPacket;
import net.fec.openrq.OpenRQ;
import net.fec.openrq.Parsed;
import net.fec.openrq.decoder.DataDecoder;
import net.fec.openrq.encoder.DataEncoder;
import net.fec.openrq.encoder.SourceBlockEncoder;
import net.fec.openrq.parameters.FECParameters;


public class RaptorCodeEncoder {
    private static final FECParameters FEC_PARAMS = FECParameters.newParameters(254, 1,1);

    public void test()
    {
        int numb = 254;
        byte[] data = new byte[numb];
        for (short i = 0; i < numb; i++) {
            data[i] = (byte)i;
            System.out.println(i);
        }

        final DataEncoder dataEnc = OpenRQ.newEncoder(data, FEC_PARAMS);

        SourceBlockEncoder sbe = dataEnc.sourceBlock(0);
        System.out.println("Data Length: " + dataEnc.dataLength() + " number of Sourceblocks: " + dataEnc.numberOfSourceBlocks() + " number of source symbols: " + sbe.numberOfSourceSymbols());

        SourceBlockEncoder.IterableBuilder itBulder = sbe.newIterableBuilder();

        itBulder.endAtFinalSourceSymbol();

        Iterable<EncodingPacket> itPackets = itBulder.build();

        itPackets.forEach(encPacket -> {
            for(int i = 0; i < encPacket.asArray().length; i++)
            {
                System.out.println(encPacket.asArray()[i]);
            }
        });
    }




}
