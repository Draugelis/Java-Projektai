package com.menotyou.JC.NIOBiblioteka;

import java.net.ServerSocket;


// TODO: Auto-generated Javadoc
/**
 * The Interface NIOServerioSasaja.
 */
public interface NIOServerioSasaja extends NIOAbstraktiSasaja {
	
	/**
	 * Gauk visu sujungimu skaiciu.
	 *
	 * @return the long
	 */
	long gaukVisuSujungimuSkaiciu();
	
	/**
	 * Gauk visu atmestu sujungimu skaiciu.
	 *
	 * @return the long
	 */
	long gaukVisuAtmestuSujungimuSkaiciu();
	
	/**
	 * Gauk visu priimtu sujungimu skaiciu.
	 *
	 * @return the long
	 */
	long gaukVisuPriimtuSujungimuSkaiciu();
	
	/**
	 * Gauk visu nepavykusiu sujungimu skaiciu.
	 *
	 * @return the long
	 */
	long gaukVisuNepavykusiuSujungimuSkaiciu();
	
	/**
	 * Stebek.
	 *
	 * @param stebetojas the stebetojas
	 */
	void stebek(ServerioSasajosStebetojas stebetojas);
	
	/**
	 * Nustatyk prisijungimu filtra.
	 *
	 * @param f the f
	 */
	void nustatykPrisijungimuFiltra(PrisijungimuFiltras f);
	
	/**
	 * Gauk sasaja.
	 *
	 * @return the server socket
	 */
	ServerSocket gaukSasaja();
}
