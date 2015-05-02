package com.menotyou.JC.NIOBiblioteka.Skaitytojai;

import java.nio.ByteBuffer;

import com.menotyou.JC.NIOBiblioteka.NIOIrankiai;
import com.menotyou.JC.NIOBiblioteka.ProtokoloPazeidimoIsimtis;

// TODO: Auto-generated Javadoc
/**
 * The Class PaprastasPaketuSkaitytojas.
 */
public class PaprastasPaketuSkaitytojas implements PaketuSkaitytojas {

	/** The m_big endian. */
	private final boolean m_bigEndian;
	
	/** The m_antrastes dydis. */
	private final int m_antrastesDydis;

	/**
	 * Instantiates a new paprastas paketu skaitytojas.
	 *
	 * @param antrastesDydis the antrastes dydis
	 * @param bigEndian the big endian
	 */
	public PaprastasPaketuSkaitytojas(int antrastesDydis, boolean bigEndian) {
		if (antrastesDydis < 1 || antrastesDydis > 4) throw new IllegalStateException("Antrast�s dydis turi b�ti tarp 1 ir 4, o dabar:" + antrastesDydis);
		m_bigEndian = bigEndian;
		m_antrastesDydis = antrastesDydis;
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.Skaitytojai.PaketuSkaitytojas#kitasPaketas(java.nio.ByteBuffer)
	 */
	public byte[] kitasPaketas(ByteBuffer buferis) throws ProtokoloPazeidimoIsimtis {
		if (buferis.remaining() < m_antrastesDydis) return null;
		buferis.mark();
		int ilgis = NIOIrankiai.gaukPaketoDydiBuferyje(buferis, m_antrastesDydis, m_bigEndian);
		if (buferis.remaining() >= ilgis) {
			byte[] paketas = new byte[ilgis];
			buferis.get(paketas);
			return paketas;
		} else {
			buferis.reset();
			return null;
		}
	}

}
