package com.menotyou.JC.NIOBiblioteka;

import java.io.IOException;

import com.menotyou.JC.NIOBiblioteka.EventuValdiklis.EventuValdiklis;


// TODO: Auto-generated Javadoc
/**
 * The Class NIOServeris.
 */
public class NIOServeris implements ServerioSasajosStebetojas {
	
	/** The m_eventu valdiklis. */
	private EventuValdiklis m_eventuValdiklis;
	
	/**
	 * Instantiates a new NIO serveris.
	 *
	 * @param eventuValdiklis the eventu valdiklis
	 */
	public NIOServeris(EventuValdiklis eventuValdiklis){
		
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.ServerioSasajosStebetojas#priemimasNepavyko(java.io.IOException)
	 */
	public void priemimasNepavyko(IOException isimtis) {
		
	}
	
	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.ServerioSasajosStebetojas#serverioSasajaMire(java.lang.Exception)
	 */
	public void serverioSasajaMire(Exception isimtis) {

	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.ServerioSasajosStebetojas#naujasSujungimas(com.menotyou.JC.NIOBiblioteka.NIOSasaja)
	 */
	public void naujasSujungimas(NIOSasaja soketas) {

	}

}
