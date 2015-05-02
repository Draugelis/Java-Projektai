package com.menotyou.JC.NIOBiblioteka;

// TODO: Auto-generated Javadoc
/**
 * The Interface SasajosStebetojas.
 */
public interface SasajosStebetojas {

	/** The null. */
	SasajosStebetojas NULL = new SasajosStebetojoAdapteris();
	
	/**
	 * Rysys uztvirtintas.
	 *
	 * @param sasaja the sasaja
	 */
	void rysysUztvirtintas(NIOSasaja sasaja);
	
	/**
	 * Rysys nutrauktas.
	 *
	 * @param sasaja the sasaja
	 * @param isimtis the isimtis
	 */
	void rysysNutrauktas(NIOSasaja sasaja, Exception isimtis);
	
	/**
	 * Paketas gautas.
	 *
	 * @param sasaja the sasaja
	 * @param paketas the paketas
	 */
	void paketasGautas(NIOSasaja sasaja, byte[] paketas);
	
	/**
	 * Paketas issiustas.
	 *
	 * @param sasaja the sasaja
	 * @param zyme the zyme
	 */
	void paketasIssiustas(NIOSasaja sasaja, Object zyme);
}
