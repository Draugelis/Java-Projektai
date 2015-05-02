package com.menotyou.JC.NIOBiblioteka.Rasytojai;

import java.nio.ByteBuffer;

import com.menotyou.JC.NIOBiblioteka.NIOIrankiai;

// TODO: Auto-generated Javadoc
/**
 * The Class PaprastasPaketuRasytojas.
 */
public class PaprastasPaketuRasytojas implements PaketuRasytojas {

	/** The m_big endian. */
	private final boolean m_bigEndian;
	
	/** The m_antraste. */
	private final ByteBuffer m_antraste;
	
	/**
	 * Instantiates a new paprastas paketu rasytojas.
	 *
	 * @param antrastesDydis the antrastes dydis
	 * @param bigEndian the big endian
	 */
	public PaprastasPaketuRasytojas(int antrastesDydis, boolean bigEndian){
		if(antrastesDydis < 1 || antrastesDydis > 4) throw new IllegalStateException("Antrast�s dydis turi b�ti tarp 1 ir 4, o dabar:" + antrastesDydis);
		m_bigEndian = bigEndian;
		m_antraste = ByteBuffer.allocate(antrastesDydis);
	}
	
	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.Rasytojai.PaketuRasytojas#rasyk(java.nio.ByteBuffer[])
	 */
	public ByteBuffer[] rasyk(ByteBuffer[] buferiai) {
		m_antraste.clear();
		NIOIrankiai.nustatykPaketoDydiBuferyje(m_antraste, m_antraste.capacity(), (int) NIOIrankiai.likeBaitai(buferiai), m_bigEndian);
		m_antraste.flip();
		return NIOIrankiai.sumeskIViena(m_antraste, buferiai);
	}

}
