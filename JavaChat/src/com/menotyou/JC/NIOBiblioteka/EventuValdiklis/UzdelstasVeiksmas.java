package com.menotyou.JC.NIOBiblioteka.EventuValdiklis;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

public class UzdelstasVeiksmas implements Comparable<UzdelstasVeiksmas>, UzdelstasIvykis{

	private final static AtomicLong s_kitoId = new AtomicLong(0L);
	private volatile Runnable m_iskvietimas;
	private final long m_laikas;
	private final long m_id;
	
	public UzdelstasVeiksmas(Runnable iskvietimas, long laikas){
		m_iskvietimas = iskvietimas;
		m_laikas = laikas;
		m_id = s_kitoId.incrementAndGet();
	}
	public void atsaukti() {
		m_iskvietimas = null;
	}
	
	void run(){
		Runnable iskvietimas = m_iskvietimas;
		if(iskvietimas != null) iskvietimas.run();
	}


	public int compareTo(UzdelstasVeiksmas uv) {
		if(m_laikas < uv.m_laikas) return -1;
		if(m_laikas > uv.m_laikas) return 1;
		if(m_id < uv.m_id) return -1;
		return m_id > uv.m_id ? 1 : 0;
	}
	public Runnable gaukIskvietima() {
		return m_iskvietimas;
	}

	public long gaukLaika() {
		return m_laikas;
	}
	
	public String toString(){
		return "UzdelstasVeiksmas @ " + new Date(m_laikas) + " [" + (m_iskvietimas == null ? "Atsauktas" : m_iskvietimas) + "]";
	}
}
