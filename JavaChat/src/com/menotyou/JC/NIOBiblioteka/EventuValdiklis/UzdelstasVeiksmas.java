package com.menotyou.JC.NIOBiblioteka.EventuValdiklis;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Klasė skirta naudoti įvykių ilėje kaip laisvai paliedžiamas ir atšaukiamas įvykis
 * Jis naudoja Comparable valdiklį (interface) todėl jį galima rikiuoti pagal laiką iki paleidimo.
 */
public class UzdelstasVeiksmas implements Comparable<UzdelstasVeiksmas>, UzdelstasIvykis {

    /** Kito veiksmo id*/
    private final static AtomicLong s_kitoId = new AtomicLong(0L);

    /** Objektas kuris talpina veiksmo operaciją. */
    private volatile Runnable m_iskvietimas;

    /** Laikas iki paleidimo. */
    private final long m_laikas;

    /** Veiksmo id. */
    private final long m_id;

    /**
     * Sukiriamas naujas veiksmas.
     *
     * @param iskvietimas -> nurodyta operacija.
     * @param laikas -> laikas iki paleidimo.
     */
    public UzdelstasVeiksmas(Runnable iskvietimas, long laikas) {
        m_iskvietimas = iskvietimas;
        m_laikas = laikas;
        m_id = s_kitoId.incrementAndGet();
    }

    /** 
     *  Metodas skiratas atšaukti įvykiui.
     */
    public void atsaukti() {
        m_iskvietimas = null;
    }

    /**
     * Metodas skirtas paleisti įvykio operacijai.
     */
    void run() {
        Runnable iskvietimas = m_iskvietimas;
        if (iskvietimas != null) iskvietimas.run();
    }

    /** 
     * Comparable<UzdelstasVeiksmas> funkcija kurią perrašo ši funkcija.
     * Taip galima lyginti du tokios klasės objektus.
     */
    public int compareTo(UzdelstasVeiksmas uv) {
        if (m_laikas < uv.m_laikas) return -1;
        if (m_laikas > uv.m_laikas) return 1;
        if (m_id < uv.m_id) return -1;
        return m_id > uv.m_id ? 1 : 0;
    }

    public Runnable gaukIskvietima() {
        return m_iskvietimas;
    }

    public long gaukLaika() {
        return m_laikas;
    }

    public String toString() {
        return "UzdelstasVeiksmas @ " + new Date(m_laikas) + " [" + (m_iskvietimas == null ? "Atsauktas" : m_iskvietimas) + "]";
    }
}
