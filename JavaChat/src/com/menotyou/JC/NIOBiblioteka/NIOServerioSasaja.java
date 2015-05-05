package com.menotyou.JC.NIOBiblioteka;

import java.net.ServerSocket;

/**
 * Kaip ir NIOSasaja šis valdiklis(interface) apgaubia ServerioKalanoValdiklį.
 * Čia apibrėžiami tik funkcijų prototipai.
 */
public interface NIOServerioSasaja extends NIOAbstraktiSasaja {

    long gaukVisuSujungimuSkaiciu();

    long gaukVisuAtmestuSujungimuSkaiciu();

    long gaukVisuPriimtuSujungimuSkaiciu();

    long gaukVisuNepavykusiuSujungimuSkaiciu();

    void stebek(ServerioSasajosStebetojas stebetojas);

    void nustatykPrisijungimuFiltra(PrisijungimuFiltras f);

    ServerSocket gaukSasaja();
}
