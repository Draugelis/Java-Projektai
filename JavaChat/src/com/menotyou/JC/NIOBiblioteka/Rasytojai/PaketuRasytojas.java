package com.menotyou.JC.NIOBiblioteka.Rasytojai;

import java.nio.ByteBuffer;

/**
 * Valdiklis(interface) kuris apibrėžiai visiems ateityje kuriamiems paketų
 * rašytojams reikalingas funkcijas.
 */
public interface PaketuRasytojas {

    ByteBuffer[] rasyk(ByteBuffer[] buferis);
}
