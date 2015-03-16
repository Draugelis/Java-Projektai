package com.menotyou.JC.NIOBiblioteka.Skaitytojai;

import java.nio.ByteBuffer;

import com.menotyou.JC.NIOBiblioteka.NIOIrankiai;
import com.menotyou.JC.NIOBiblioteka.ProtokoloPazeidimoIsimtis;

public class PaprastasPaketuSkaitytojas implements PaketuSkaitytojas {

	private final boolean m_bigEndian;
	private final int m_antrastesDydis;

	public PaprastasPaketuSkaitytojas(int antrastesDydis, boolean bigEndian) {
		if (antrastesDydis < 1 || antrastesDydis > 4) throw new IllegalStateException("Antrast�s dydis turi b�ti tarp 1 ir 4, o dabar:" + antrastesDydis);
		m_bigEndian = bigEndian;
		m_antrastesDydis = antrastesDydis;
	}

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
