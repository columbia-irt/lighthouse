package io.sece.vlc.trx;

/**
 *
 * @author Hagen
 */
public final class QuadSymbol extends Symbol {
    public enum Value {
        ZEROZERO,
        ZEROONE,
        ONEZERO,
        ONEONE
    };

    public Value value;

    public QuadSymbol(Value value)
    {
        this.value = value;
    }
}
