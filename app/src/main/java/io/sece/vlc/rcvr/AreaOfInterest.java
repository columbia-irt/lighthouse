package io.sece.vlc.rcvr;

import org.opencv.core.Mat;
import org.opencv.core.Rect;


public class AreaOfInterest {
    private int posX;
    private int posY;
    private int width;
    private int height;
    private Mat content;
    Rect rectangle;

    public AreaOfInterest(int posX, int posY, int width, int height) {
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
        rectangle = new Rect(posX,posY,width,height);
    }

    public Rect getRectangle() {
        return rectangle;
    }
}
