package com.menotyou.JC.NIOBiblioteka.EventuValdiklis;
// TODO: Auto-generated Javadoc

/**
 * The Interface UzdelstasIvykis.
 */
public interface UzdelstasIvykis {

	/**
	 * Atsaukti.
	 */
	void atsaukti();
	
	/**
	 * Gauk iskvietima.
	 *
	 * @return the runnable
	 */
	Runnable gaukIskvietima();
	
	/**
	 * Gauk laika.
	 *
	 * @return the long
	 */
	long gaukLaika();
}
