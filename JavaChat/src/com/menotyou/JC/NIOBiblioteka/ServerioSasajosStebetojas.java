package com.menotyou.JC.NIOBiblioteka;

import java.io.IOException;

// TODO: Auto-generated Javadoc
/**
 * The Interface ServerioSasajosStebetojas.
 */
public interface ServerioSasajosStebetojas {
	
	/**
	 * Priemimas nepavyko.
	 *
	 * @param isimtis the isimtis
	 */
	void priemimasNepavyko(IOException isimtis);
	
	/**
	 * Serverio sasaja mire.
	 *
	 * @param isimtis the isimtis
	 */
	void serverioSasajaMire(Exception isimtis);
	
	/**
	 * Naujas sujungimas.
	 *
	 * @param soketas the soketas
	 */
	void naujasSujungimas(NIOSasaja soketas);

}
