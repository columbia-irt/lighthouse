package io.sece.vlc;

/**
 * Created by alex on 7/9/18.
 */

public class FramingBlock {

    /**
     *  Different statuses for Receiver Parts
     */

    final int RX_STATE_S0 = 0;
    final int RX_STATE_S1 = 1;
    final int RX_STATE_S2 = 2;
    final int RX_STATE_D = 3;
    final int RX_STATE_DS1 = 4;
    final int RX_STATE_DS2 = 5;
    String rx_bits = "";

    final int TX_STATE_D0 = 0;
    final int TX_STATE_D1 = 1;
    String tx_bits = "";

    int receiverState;
    int transmitterState;

    public FramingBlock(){
        receiverState = RX_STATE_S0;
        transmitterState = TX_STATE_D0;
    }

    public String applyRX(String symbol) {
        switch(receiverState){
            case RX_STATE_S0:
                if(symbol.equals("01")){
                    receiverState = RX_STATE_S1;
                }
                break;
            case RX_STATE_S1:
                if(symbol.equals("11")){
                    receiverState = RX_STATE_S2;
                }else{
                    receiverState = RX_STATE_S0;
                }
                break;
            case RX_STATE_S2:
                if(symbol.equals("10")){
                    receiverState = RX_STATE_D;
                }else{
                    receiverState = RX_STATE_S0;
                }
                break;
            case RX_STATE_D:
                if(symbol.equals("01")){
                    receiverState = RX_STATE_DS1;
                }else{
                    storeRX(symbol);
                }
                break;
            case RX_STATE_DS1:
                if(symbol.equals("11")){
                    receiverState = RX_STATE_DS2;
                }else{
                    storeRX("01" + symbol);
                    receiverState = RX_STATE_D;
                }
                break;
            case RX_STATE_DS2:
                if(symbol.equals("10")){
                    String tempBits = rx_bits;
                    rx_bits = "";
                    return tempBits;
                }else if(symbol.equals("11")){
                    storeRX("0111");
                    receiverState = RX_STATE_D;
                }else{
                    receiverState = RX_STATE_S0;
                    rx_bits = "";
                }
                break;
            default: break;
        }
        return null;
    }

    private void storeRX(String input){
        rx_bits += input;
    }

    public String applyTX (String sequence, int bitsAmount){
        for(int i = 0; i< sequence.length(); i+=bitsAmount){
            addSymbolTX(sequence.substring(i, i + bitsAmount));
        }
        return tx_bits;
    }

    public void addSymbolTX (String symbol){
        switch(transmitterState){
            case TX_STATE_D0:
                if(symbol.equals("01")) {
                    transmitterState = TX_STATE_D1;
                }
                tx_bits += symbol;
                break;
            case TX_STATE_D1:
                if(symbol.equals("11")){
                    tx_bits += "1111";
                }else{
                    tx_bits += symbol;
                }
                transmitterState = TX_STATE_D0;
                break;
            default: break;
        }
    }

}
