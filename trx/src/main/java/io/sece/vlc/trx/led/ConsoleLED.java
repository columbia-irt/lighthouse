package io.sece.vlc.trx.led;

import java.util.HashMap;
import java.util.Map;

import io.sece.vlc.Color;
import io.sece.vlc.trx.ColorLEDInterface;


public class ConsoleLED implements ColorLEDInterface {
    private static final String PREFIX  = "\033[";
    private static final String HOME    = "0;0H";
    private static final String CLEAR   = "2J";
    private static final String RESET   = "0m";
    private static final String BLACK   = "40m";
    private static final String WHITE   = "47m";
    private static final String RED     = "41m";
    private static final String GREEN   = "42m";
    private static final String BLUE    = "44m";
    private static final String YELLOW  = "43m";
    private static final String CYAN    = "46m";
    private static final String MAGENTA = "45m";

    private static final Map<Color, String> colorMap;

    static {
        colorMap = new HashMap<>();
        colorMap.put(Color.BLACK,   BLACK);
        colorMap.put(Color.WHITE,   WHITE);
        colorMap.put(Color.RED,     RED);
        colorMap.put(Color.GREEN,   GREEN);
        colorMap.put(Color.BLUE,    BLUE);
        colorMap.put(Color.YELLOW,  YELLOW);
        colorMap.put(Color.CYAN,    CYAN);
        colorMap.put(Color.MAGENTA, MAGENTA);
    }

    private static void home() {
        System.out.print(PREFIX + HOME);
    }


    private static void clear() {
        System.out.print(PREFIX + CLEAR);
    }


    private static void setColor(String color) {
        System.out.print(PREFIX + color);
    }


    private static void resetColor() {
        System.out.print(PREFIX + RESET);
    }


    private static void draw(String color) {
        home();
        for(int i = 0; i < 5; i++) {
            setColor(color);
            System.out.print("          ");
            resetColor();
            System.out.println();
        }
    }


    private String colorToEscape(Color color) {
        color = color.nearestNeighbor(Color.BLACK, Color.WHITE, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA);
        String rv = colorMap.get(color);
        if (rv == null)
            throw new RuntimeException("Bug in ConsoleLED implementation");
        return rv;
    }


    @Override
    public void set(Color color) {
        draw(colorToEscape(color));
    }
}
