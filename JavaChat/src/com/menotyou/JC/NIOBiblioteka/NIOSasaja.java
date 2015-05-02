package com.menotyou.JC.NIOBiblioteka;

import java.net.Socket;
import java.nio.channels.SocketChannel;

import com.menotyou.JC.NIOBiblioteka.Rasytojai.PaketuRasytojas;
import com.menotyou.JC.NIOBiblioteka.Skaitytojai.PaketuSkaitytojas;


// TODO: Auto-generated Javadoc
/**
 * The Interface NIOSasaja.
 */
public interface NIOSasaja extends NIOAbstraktiSasaja {
	
	/**
	 * Rasyk.
	 *
	 * @param paketas the paketas
	 * @return true, if successful
	 */
	boolean rasyk(byte[] paketas);
	
	
	/**
	 * Rasyk.
	 *
	 * @param paketas the paketas
	 * @param zyme the zyme
	 * @return true, if successful
	 */
	boolean rasyk(byte[] paketas, Object zyme);
	
	/**
	 * Pridek i eile.
	 *
	 * @param r the r
	 */
	void pridekIEile(Runnable r);
	
	/**
	 * Gauk nuskaitytu baitu skaiciu.
	 *
	 * @return the long
	 */
	long gaukNuskaitytuBaituSkaiciu();
	
	/**
	 * Gauk parasytu baitu skaiciu.
	 *
	 * @return the long
	 */
	long gaukParasytuBaituSkaiciu();
	
	/**
	 * Gauk laika nuo sujungimo.
	 *
	 * @return the long
	 */
	long gaukLaikaNuoSujungimo();
	
	/**
	 * Gauk rasymo eiles dydi.
	 *
	 * @return the long
	 */
	long gaukRasymoEilesDydi();
	
	/**
	 * Gauk max eiles ilgi.
	 *
	 * @return the int
	 */
	int gaukMaxEilesIlgi();
	
	/**
	 * Nustatyk max eiles ilgi.
	 *
	 * @param maxEilesIlgis the max eiles ilgis
	 */
	void nustatykMaxEilesIlgi(int maxEilesIlgis);
	
	/**
	 * Nustatyk paketu skaitytoja.
	 *
	 * @param paketuSkaitytojas the paketu skaitytojas
	 */
	void nustatykPaketuSkaitytoja(PaketuSkaitytojas paketuSkaitytojas);
	
	/**
	 * Nustatyk paketu rasytoja.
	 *
	 * @param paketuRasytojas the paketu rasytojas
	 */
	void nustatykPaketuRasytoja(PaketuRasytojas paketuRasytojas);
	
	/**
	 * Stebek.
	 *
	 * @param stebetojas the stebetojas
	 */
	void stebek(SasajosStebetojas stebetojas);
	
	/**
	 * Uzsidaryk po rasymo.
	 */
	void uzsidarykPoRasymo();
	
	/**
	 * Gauk sasaja.
	 *
	 * @return the socket
	 */
	Socket gaukSasaja();
	
}
