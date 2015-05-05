package com.menotyou.JC.NIOBiblioteka.Rasytojai;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Klasė atsakinga už informacijos rašymą į nurodytą kanalą.
 */
public class KanaloRasytojas {

    private long m_irasytiBaitai;

    /** ByteBuffer objektu masyvas siunčiamiems baitams talpinti. */
    private ByteBuffer[] m_rasymoBuferiai;

    /** The m_paketu rasytojas. */
    private PaketuRasytojas m_paketuRasytojas;

    /** žymė skirta pažymėti tam tikrą paketą, šiu metu ji nebenaudojama. */
    private Object m_zyme;

    /** Dabartinio ByteBuffer masyvo elemento indeksas. */
    private int m_dabartinisBuferis;

    /**
     * Sukuriamas naujas kanalo rašytojas.
     */
    public KanaloRasytojas() {
        m_irasytiBaitai = 0;
        m_rasymoBuferiai = null;
        m_paketuRasytojas = GrynasPaketuRasytojas.NUMATYTASIS;
    }

    public PaketuRasytojas gaukPaketuRasytoja() {
        return m_paketuRasytojas;
    }

    public void nustatykPaketuRasytoja(PaketuRasytojas pr) {
        m_paketuRasytojas = pr;
    }

    public boolean tuscias() {
        return m_rasymoBuferiai == null;
    }

    /**
     * Pridek paketa.
     *
     * @param duomenys -> duomenus kurie turėtų būti pridėti.
     * @param zyme -> žymė žyminti paketą.
     */
    public void pridekPaketa(byte[] duomenys, Object zyme) {
        if (!tuscias()) throw new IllegalStateException("Šis metodas turėtų būti kviečiamas tik kai bufferis == null");

        m_rasymoBuferiai = m_paketuRasytojas.rasyk(new ByteBuffer[] { ByteBuffer.wrap(duomenys) });
        m_dabartinisBuferis = 0;
        m_zyme = zyme;
    }

    /**
     * Funkcija skirta rašyti į tam tikrą kanalą.
     * Jei rašymo buferiai yra tušti ar == null, funkcija gražina false
     * reiškiantį, kad nieko nebuo įrašyta.
     * Naudojantis SocketChannel funkcija write().
     * kanalu įrašoma tiek kiek įmanoma tuo metu ir grąžinamas
     * įrašytų baitų skaičius.
     *
     * @param kanalas -> kanalas į kurį bus rašoma.
     * @return true arba false.
     * @throws IOException tipo iššimtis jei kyla klaida rašant į kanalą.
     */
    public boolean rasyk(SocketChannel kanalas) throws IOException {
        if (m_rasymoBuferiai == null || (m_dabartinisBuferis == m_rasymoBuferiai.length - 1 && !m_rasymoBuferiai[m_dabartinisBuferis].hasRemaining())) {
            m_rasymoBuferiai = null;
            return false;
        }
        long irasyta = kanalas.write(m_rasymoBuferiai, m_dabartinisBuferis, m_rasymoBuferiai.length - m_dabartinisBuferis);
        if (irasyta == 0) return false;
        m_irasytiBaitai += irasyta;
        for (int i = m_dabartinisBuferis; i < m_rasymoBuferiai.length; i++) {
            if (m_rasymoBuferiai[i].hasRemaining()) {
                m_dabartinisBuferis = i;
                break;
            }
            m_rasymoBuferiai[i] = null;
        }
        if (m_rasymoBuferiai[m_dabartinisBuferis] == null) {
            m_rasymoBuferiai = null;
        }
        return true;
    }

    public long gaukKiekParasytaBaitu() {
        return m_irasytiBaitai;
    }

    public Object gaukZyme() {
        return m_zyme;
    }
}
