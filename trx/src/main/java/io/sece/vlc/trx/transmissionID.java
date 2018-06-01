package io.sece.vlc.trx;

public class transmissionID {
    private int tID;

    public transmissionID()
    {
        tID = -1;
    }
    public int getID() {
        return tID;
    }

    @Override
    public String toString() {
        return "Transmission ID: " + tID;
    }
}
