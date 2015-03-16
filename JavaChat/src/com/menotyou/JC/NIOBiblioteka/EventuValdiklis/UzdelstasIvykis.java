package com.menotyou.JC.NIOBiblioteka.EventuValdiklis;
public interface UzdelstasIvykis {

	void atsaukti();
	
	Runnable gaukIskvietima();
	
	long gaukLaika();
}
