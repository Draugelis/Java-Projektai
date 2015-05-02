package com.menotyou.JC.NIOBiblioteka.Rasytojai;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

// TODO: Auto-generated Javadoc
/**
 * The Class KanaloRasytojas.
 */
public class KanaloRasytojas {
	
	/** The m_irasyti baitai. */
	private long m_irasytiBaitai;
	
	/** The m_rasymo buferiai. */
	private ByteBuffer[] m_rasymoBuferiai;
	
	/** The m_paketu rasytojas. */
	private PaketuRasytojas m_paketuRasytojas;
	
	/** The m_zyme. */
	private Object m_zyme;
	
	/** The m_dabartinis buferis. */
	private int m_dabartinisBuferis;
	
	/**
	 * Instantiates a new kanalo rasytojas.
	 */
	public KanaloRasytojas(){
		m_irasytiBaitai = 0;
		m_rasymoBuferiai = null;
		m_paketuRasytojas = GrynasPaketuRasytojas.NUMATYTASIS;
	}
	
	/**
	 * Gauk paketu rasytoja.
	 *
	 * @return the paketu rasytojas
	 */
	public PaketuRasytojas gaukPaketuRasytoja(){
		return m_paketuRasytojas;
	}
	
	/**
	 * Nustatyk paketu rasytoja.
	 *
	 * @param pr the pr
	 */
	public void nustatykPaketuRasytoja(PaketuRasytojas pr){
		m_paketuRasytojas = pr;
	}
	
	/**
	 * Tuscias.
	 *
	 * @return true, if successful
	 */
	public boolean tuscias(){
		return m_rasymoBuferiai == null;
	}
	
	/**
	 * Pridek paketa.
	 *
	 * @param duomenys the duomenys
	 * @param zyme the zyme
	 */
	public void pridekPaketa(byte[] duomenys, Object zyme){
		if(!tuscias()) throw new IllegalStateException("�is metodas tur�t� buti kvie�iamas tik kai m_rasymoBuferiai == null");
		
		m_rasymoBuferiai = m_paketuRasytojas.rasyk(new ByteBuffer[]{ByteBuffer.wrap(duomenys) });
		m_dabartinisBuferis = 0;
		m_zyme = zyme;
	}
	
	/**
	 * Rasyk.
	 *
	 * @param kanalas the kanalas
	 * @return true, if successful
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public boolean rasyk(SocketChannel kanalas) throws IOException{
		if(m_rasymoBuferiai == null ||
				(m_dabartinisBuferis == m_rasymoBuferiai.length -1 
				&& !m_rasymoBuferiai[m_dabartinisBuferis].hasRemaining())){
			m_rasymoBuferiai = null;
			return false;
		}
		long irasyta = kanalas.write(m_rasymoBuferiai, m_dabartinisBuferis, m_rasymoBuferiai.length - m_dabartinisBuferis);
		if(irasyta == 0) return false;
		m_irasytiBaitai += irasyta;
		for(int i = m_dabartinisBuferis; i < m_rasymoBuferiai.length; i++){
			if(m_rasymoBuferiai[i].hasRemaining()){
				m_dabartinisBuferis = i;
				break;
			}
			m_rasymoBuferiai[i] = null;
		}
		if(m_rasymoBuferiai[m_dabartinisBuferis] == null){
			m_rasymoBuferiai = null;
		}
		return true;
	}
	
	/**
	 * Gauk kiek parasyta baitu.
	 *
	 * @return the long
	 */
	public long gaukKiekParasytaBaitu(){
		return m_irasytiBaitai;
	}
	
	/**
	 * Gauk zyme.
	 *
	 * @return the object
	 */
	public Object gaukZyme(){
		return m_zyme;
	}
}
