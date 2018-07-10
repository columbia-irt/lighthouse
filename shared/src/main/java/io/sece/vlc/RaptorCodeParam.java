package io.sece.vlc;

import net.fec.openrq.parameters.FECParameters;

public class RaptorCodeParam {

    public static final FECParameters FEC_PARAMS = FECParameters.newParameters(32, 8,1);

    public static byte[] data()
    {
        int numb = 32;
        byte[] data = new byte[numb];
        for (short i = 0; i < numb; i++) {
            data[i] = (byte)(numb - 1 - i);
        }
        return data;
    }
}
