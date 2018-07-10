package io.sece.vlc;

import java.math.BigInteger;
import java.util.zip.CRC32;

public class DataFrame {

    private String marker = "101100";
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
        removeStuffingBits(data);
        return data;
//        if(this.checksum.equals(calcChecksum())) {
//            return data;
//        }
//        else
//        {
//            return null;
//        }
    }



    private void removeStuffingBits(String input){
        int equalCounter = 0;
        for(int i=0; i < input.length(); i++){
            if(input.charAt(i) == marker.charAt(equalCounter)){
                equalCounter++;
                if(equalCounter == marker.length() -1){
                    System.out.println("remove " + (i+1) + (i+2));
                    equalCounter = 0;
                    input = (input.substring(0,i) + input.substring(i +2,input.length()));
                }
            }else{
                equalCounter = 0;
                if(input.charAt(i) == marker.charAt(equalCounter)){
                    equalCounter++;
                }
            }
        }
    }

    private String insertStuffingBits(){
        String output = data;

        int equalCounter = 0;
        for(int i=0; i < output.length(); i++){
            if(output.charAt(i) == marker.charAt(equalCounter)){
                equalCounter++;
                if(equalCounter == marker.length() -1){
                    System.out.println("Insert " + i);
                    equalCounter = 0;
                    output = output.substring(0,i) + stuffing + output.substring(i,output.length());
                }
            }else{
                equalCounter = 0;
                if(output.charAt(i) == marker.charAt(equalCounter)){
                    equalCounter++;
                }
            }
        }
        return output;
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
