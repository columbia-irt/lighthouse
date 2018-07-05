package io.sece.vlc.trx;


public class TransmissionID {
    private int tID;

    public TransmissionID()
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
