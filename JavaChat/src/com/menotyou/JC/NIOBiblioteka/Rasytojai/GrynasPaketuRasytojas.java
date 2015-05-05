package com.menotyou.JC.NIOBiblioteka.Rasytojai;

import java.nio.ByteBuffer;

/**
 *  Numatytas paketų rašytojas. Jis reikalingas tik tuomet kai nėra priskirtas
 *  norimas paketų rašytojas.
 */
public class GrynasPaketuRasytojas implements PaketuRasytojas {
    public static GrynasPaketuRasytojas NUMATYTASIS = new GrynasPaketuRasytojas();

    public GrynasPaketuRasytojas() {
    }

    public ByteBuffer[] rasyk(ByteBuffer[] buferis) {
        return buferis;
    }

}
