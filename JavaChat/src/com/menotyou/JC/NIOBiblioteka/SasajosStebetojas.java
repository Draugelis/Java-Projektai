package com.menotyou.JC.NIOBiblioteka;

/**
 * Valdiklis(interface), kuriame apibrėžiamos visos 
 * sąsajos stebėtojui reikalingos funkcijos.
 */
public interface SasajosStebetojas {

    void rysysUztvirtintas(NIOSasaja sasaja);

    void rysysNutrauktas(NIOSasaja sasaja, Exception isimtis);

    void paketasGautas(NIOSasaja sasaja, byte[] paketas);

    void paketasIssiustas(NIOSasaja sasaja, Object zyme);
}
