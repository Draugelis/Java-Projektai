package com.menotyou.JC.NIOBiblioteka;

import java.net.InetSocketAddress;


// TODO: Auto-generated Javadoc
/**
 * The Interface PrisijungimuFiltras.
 */
public interface PrisijungimuFiltras {
	
	/** The atmesk visus. */
	PrisijungimuFiltras ATMESK_VISUS = new PrisijungimuFiltras(){
		public boolean priimkPrisijungima(InetSocketAddress adresas){
			return false;
		}
	};
	
	/** The leisk visus. */
	PrisijungimuFiltras LEISK_VISUS = new PrisijungimuFiltras(){
		public boolean priimkPrisijungima(InetSocketAddress adresas){
			return true;
		}
	};
	
	/**
	 * Priimk prisijungima.
	 *
	 * @param adresas the adresas
	 * @return true, if successful
	 */
	public boolean priimkPrisijungima(InetSocketAddress adresas);
}
