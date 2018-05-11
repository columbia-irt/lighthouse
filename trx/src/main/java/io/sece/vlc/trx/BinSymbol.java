package io.sece.vlc.trx;

/**
 *
 * @author Hagen
 */
public final class BinSymbol extends Symbol {
    public enum Value
    {
        ONE,
        ZERO
    };
    
    public Value value;
    
    public BinSymbol(Value value)
    {
        this.value = value;
    }
}
