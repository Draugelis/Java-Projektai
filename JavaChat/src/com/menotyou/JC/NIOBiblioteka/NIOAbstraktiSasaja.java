package com.menotyou.JC.NIOBiblioteka;
import java.net.InetSocketAddress;


// TODO: Auto-generated Javadoc
/**
 * The Interface NIOAbstraktiSasaja.
 */
public interface NIOAbstraktiSasaja {


	/**
	 * Uzdaryk.
	 */
	void uzdaryk();
	
	/**
	 * Gauk adresa.
	 *
	 * @return the inet socket address
	 */
	InetSocketAddress gaukAdresa();
	
	/**
	 * Atidarytas.
	 *
	 * @return true, if successful
	 */
	boolean atidarytas();
	
	/**
	 * Gauk ip.
	 *
	 * @return the string
	 */
	String gaukIp();
	
	/**
	 * Gauk porta.
	 *
	 * @return the int
	 */
	int gaukPorta();
	
	/**
	 * Gauk zyme.
	 *
	 * @return the object
	 */
	Object gaukZyme();
	
	/**
	 * Nustatyk zyme.
	 *
	 * @param zyme the zyme
	 */
	void nustatykZyme(Object zyme);
}
