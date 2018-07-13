package io.sece.vlc;

/**
 * Created by alex on 7/9/18.
 *
 * This class represents FSK4 stuffing and unstuffing of a framing block containing data and crc checksum
 *
 *  applyRx(String bits):
 *      - receives a sequence of bits including startingsequence (011110) + data + crc8 checksum
 *      - checks and drops startingsequence and returns unstuffed data+crc8
 *
 *  applyTx(String sequence, int bitsCount):
 *      - receives bits including data and crc8 WITHOUT starting sequence, bitsCount represents amount of bits being a symbol
 *      - returns stuffed bitsequence for startingsequence (011110)
 *
 * TODO: Replacing type String of rx_bits and tx_bits with StringBuilder
 */

public class FramingBlock {

    /**
     *  different statuses for Receiver Parts
     */

    private static final int RX_STATE_S0 = 0;
    private static final int RX_STATE_S1 = 1;
    private static final int RX_STATE_S2 = 2;
    private static final int RX_STATE_D = 3;
    private static final int RX_STATE_DS1 = 4;
    private static final int RX_STATE_DS2 = 5;

    private String rx_bits = "";
    private final int RX_MAX = 150;

    private static final int TX_STATE_D0 = 0;
    private static final int TX_STATE_D1 = 1;
    private String tx_bits = "";

    public static final String STARTING_SEQUENCE = "011110";

    int receiverState = RX_STATE_S0;
    int transmitterState = TX_STATE_D0;

    public FramingBlock(){}

    public String applyRX(String bits) {
        switch(receiverState){
            case RX_STATE_S0:
                if(bits.equals("01")){
                    receiverState = RX_STATE_S1;
                }
                break;
            case RX_STATE_S1:
                if(bits.equals("11")){
                    receiverState = RX_STATE_S2;
                }else{
                    receiverState = RX_STATE_S0;
                }
                break;
            case RX_STATE_S2:
                if(bits.equals("10")){
                    receiverState = RX_STATE_D;
                }else{
                    receiverState = RX_STATE_S0;
                }
                break;
            case RX_STATE_D:
                if(bits.equals("01")){
                    receiverState = RX_STATE_DS1;
                }else{
                    storeRX(bits);
                }
                break;
            case RX_STATE_DS1:
                if(bits.equals("11")){
                    receiverState = RX_STATE_DS2;
                }else if(bits.equals("01")){
                    storeRX("01");
                }else{
                    storeRX("01" + bits);
                    receiverState = RX_STATE_D;
                }
                break;
            case RX_STATE_DS2:
                if(bits.equals("10")){
                    String tempBits = rx_bits;
                    rx_bits = "";
                    receiverState = RX_STATE_D;
                    return tempBits;
                }else if(bits.equals("11")){
                    storeRX("0111");
                    receiverState = RX_STATE_D;
                }else{
                    receiverState = RX_STATE_S0;
                    rx_bits = "";
                }
                break;
            default:
                throw new RuntimeException("Error occurred by invoking applyRX #defaultCase ");
        }
        //System.out.println("State: " + receiverState);
        return null;
    }

    private void storeRX(String input){
        if(rx_bits.length() != RX_MAX){
            rx_bits += input;
        }else{
//          reaching the maximum limit leads to a reset
            rx_bits = "";
            receiverState = RX_STATE_S0;
        }
    }

    public String applyTX (String sequence, int bitsCount){
        tx_bits = "";
        for(int i = 0; i< sequence.length(); i+=bitsCount){
            addSymbolTX(sequence.substring(i, i + bitsCount));
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
                    transmitterState = TX_STATE_D0;
                }else if(symbol.equals("01"))
                {
                    tx_bits += "01";
                    transmitterState = TX_STATE_D1;
                }
                else{
                    tx_bits += symbol;
                    transmitterState = TX_STATE_D0;
                }
                break;
            default: throw new RuntimeException("Error occurred by invoking addSymbolTX #defaultCase ");
        }
    }

}
