package com.menotyou.JC.NIOBiblioteka;

import java.io.IOException;

public interface ServerioSasajosStebetojas {
	
	void priemimasNepavyko(IOException isimtis);
	
	void serverioSasajaMire(Exception isimtis);
	
	void naujasSujungimas(NIOSasaja soketas);

}
