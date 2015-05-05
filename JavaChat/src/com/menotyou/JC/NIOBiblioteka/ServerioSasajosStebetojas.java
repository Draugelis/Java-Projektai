package com.menotyou.JC.NIOBiblioteka;

import java.io.IOException;

/**
 * Valdiklis(interface) kuriame apibrėžtos visos
 * serverio sąsajos stebėtojui reikalingos funkcijos.
 */
public interface ServerioSasajosStebetojas {

    void priemimasNepavyko(IOException isimtis);

    void serverioSasajaMire(Exception isimtis);

    void naujasSujungimas(NIOSasaja soketas);

}
