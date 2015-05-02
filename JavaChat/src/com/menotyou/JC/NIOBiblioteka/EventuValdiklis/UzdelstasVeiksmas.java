package com.menotyou.JC.NIOBiblioteka.EventuValdiklis;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

// TODO: Auto-generated Javadoc
/**
 * The Class UzdelstasVeiksmas.
 */
public class UzdelstasVeiksmas implements Comparable<UzdelstasVeiksmas>, UzdelstasIvykis{

	/** The Constant s_kitoId. */
	private final static AtomicLong s_kitoId = new AtomicLong(0L);
	
	/** The m_iskvietimas. */
	private volatile Runnable m_iskvietimas;
	
	/** The m_laikas. */
	private final long m_laikas;
	
	/** The m_id. */
	private final long m_id;
	
	/**
	 * Instantiates a new uzdelstas veiksmas.
	 *
	 * @param iskvietimas the iskvietimas
	 * @param laikas the laikas
	 */
	public UzdelstasVeiksmas(Runnable iskvietimas, long laikas){
		m_iskvietimas = iskvietimas;
		m_laikas = laikas;
		m_id = s_kitoId.incrementAndGet();
	}
	
	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.EventuValdiklis.UzdelstasIvykis#atsaukti()
	 */
	public void atsaukti() {
		m_iskvietimas = null;
	}
	
	/**
	 * Run.
	 */
	void run(){
		Runnable iskvietimas = m_iskvietimas;
		if(iskvietimas != null) iskvietimas.run();
	}


	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(UzdelstasVeiksmas uv) {
		if(m_laikas < uv.m_laikas) return -1;
		if(m_laikas > uv.m_laikas) return 1;
		if(m_id < uv.m_id) return -1;
		return m_id > uv.m_id ? 1 : 0;
	}
	
	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.EventuValdiklis.UzdelstasIvykis#gaukIskvietima()
	 */
	public Runnable gaukIskvietima() {
		return m_iskvietimas;
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.EventuValdiklis.UzdelstasIvykis#gaukLaika()
	 */
	public long gaukLaika() {
		return m_laikas;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return "UzdelstasVeiksmas @ " + new Date(m_laikas) + " [" + (m_iskvietimas == null ? "Atsauktas" : m_iskvietimas) + "]";
	}
}
