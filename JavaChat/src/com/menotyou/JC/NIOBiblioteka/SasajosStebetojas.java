package com.menotyou.JC.NIOBiblioteka;

public interface SasajosStebetojas {

	SasajosStebetojas NULL = new SasajosStebetojoAdapteris();
	
	void rysysUztvirtintas(NIOSasaja sasaja);
	
	void rysysNutrauktas(NIOSasaja sasaja, Exception isimtis);
	
	void paketasGautas(NIOSasaja sasaja, byte[] paketas);
	
	void paketasIssiustas(NIOSasaja sasaja, Object zyme);
}
