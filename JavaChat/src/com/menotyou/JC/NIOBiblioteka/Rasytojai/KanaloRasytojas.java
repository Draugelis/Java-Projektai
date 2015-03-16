package com.menotyou.JC.NIOBiblioteka.Rasytojai;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class KanaloRasytojas {
	private long m_irasytiBaitai;
	private ByteBuffer[] m_rasymoBuferiai;
	private PaketuRasytojas m_paketuRasytojas;
	private Object m_zyme;
	private int m_dabartinisBuferis;
	
	public KanaloRasytojas(){
		m_irasytiBaitai = 0;
		m_rasymoBuferiai = null;
		m_paketuRasytojas = GrynasPaketuRasytojas.NUMATYTASIS;
	}
	
	public PaketuRasytojas gaukPaketuRasytoja(){
		return m_paketuRasytojas;
	}
	
	public void nustatykPaketuRasytoja(PaketuRasytojas pr){
		m_paketuRasytojas = pr;
	}
	public boolean tuscias(){
		return m_rasymoBuferiai == null;
	}
	public void pridekPaketa(byte[] duomenys, Object zyme){
		if(!tuscias()) throw new IllegalStateException("�is metodas tur�t� buti kvie�iamas tik kai m_rasymoBuferiai == null");
		
		m_rasymoBuferiai = m_paketuRasytojas.rasyk(new ByteBuffer[]{ByteBuffer.wrap(duomenys) });
		m_dabartinisBuferis = 0;
		m_zyme = zyme;
	}
	
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
	public long gaukKiekParasytaBaitu(){
		return m_irasytiBaitai;
	}
	public Object gaukZyme(){
		return m_zyme;
	}
}
