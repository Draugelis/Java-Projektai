package com.menotyou.JC.NIOBiblioteka;

import java.net.ServerSocket;


public interface NIOServerioSasaja extends NIOAbstraktiSasaja {
	
	long gaukVisuSujungimuSkaiciu();
	
	long gaukVisuAtmestuSujungimuSkaiciu();
	
	long gaukVisuPriimtuSujungimuSkaiciu();
	
	long gaukVisuNepavykusiuSujungimuSkaiciu();
	
	void stebek(ServerioSasajosStebetojas stebetojas);
	
	void nustatykPrisijungimuFiltra(PrisijungimuFiltras f);
	
	ServerSocket gaukSasaja();
}
