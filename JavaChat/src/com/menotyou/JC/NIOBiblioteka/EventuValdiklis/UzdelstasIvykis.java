package com.menotyou.JC.NIOBiblioteka.EventuValdiklis;

/**
 * Paprastas interfeisas kurio gali naudotis UždelstasVeiksmas.
 * Jame aprašomi tik funkicijų pavadinimai.
 */
public interface UzdelstasIvykis {

    void atsaukti();

    Runnable gaukIskvietima();

    long gaukLaika();
}
