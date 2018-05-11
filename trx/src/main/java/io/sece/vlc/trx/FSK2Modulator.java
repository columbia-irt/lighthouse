package io.sece.vlc.trx;

import java.awt.Color;
/**
 *
 * @author Hagen
 */
public class FSK2Modulator extends FreqModulator implements ModulatorInterface<BinSymbol> {
    private static final Color red  = RGBColor.red;
    private static final Color blue = RGBColor.blue;
    

    public Color modulate(BinSymbol symbol) {
        switch(symbol.value) {
        case ONE:
            return red;

        case ZERO:
            return blue;
        }
        throw new AssertionError();
    }
}
