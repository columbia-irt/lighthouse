package io.sece.vlc;

/**
 * Created by alex on 7/10/18.
 */

public class StuffingBlock {
    public static String marker = "1001";
    public static String stuffing = "00";

    public static String removeStuffingBits(String input){
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
        return input;
    }

    public static String insertStuffingBits(String input){

        int equalCounter = 0;
        for(int i=0; i < input.length(); i++){
            if(input.charAt(i) == marker.charAt(equalCounter)){
                equalCounter++;
                if(equalCounter == marker.length() -1){
                    System.out.println("Insert " + i);
                    equalCounter = 0;
                    input = input.substring(0,i) + stuffing + input.substring(i,input.length());
                }
            }else{
                equalCounter = 0;
                if(input.charAt(i) == marker.charAt(equalCounter)){
                    equalCounter++;
                }
            }
        }
        return input;
    }
}
