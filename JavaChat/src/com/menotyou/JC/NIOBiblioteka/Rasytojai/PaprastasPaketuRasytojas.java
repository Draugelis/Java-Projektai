package com.menotyou.JC.NIOBiblioteka.Rasytojai;

import java.nio.ByteBuffer;

import com.menotyou.JC.NIOBiblioteka.NIOIrankiai;

/**
 * Paketų rašytojas kuris naudojamas šioje programoje.
 * Jis naudojais antraštėmis, kad apibrėžtų siunčiamų paketų dydžius.
 */
public class PaprastasPaketuRasytojas implements PaketuRasytojas {

    private final boolean m_bigEndian;
    private final ByteBuffer m_antraste;

    /**
     * Sukuriamas naujas paketų rašytjas.
     *
     * @param antrastesDydis -> antraštes dydis.
     * @param bigEndian -> nurodymas kokia forma koduojami baitai.
     */
    public PaprastasPaketuRasytojas(int antrastesDydis, boolean bigEndian) {
        if (antrastesDydis < 1 || antrastesDydis > 4) throw new IllegalStateException("Antrastšs dydis turi būti tarp 1 ir 4, o dabar:" + antrastesDydis);
        m_bigEndian = bigEndian;
        m_antraste = ByteBuffer.allocate(antrastesDydis);
    }

    /**
     * Funkcija grąžiną buferius kurios vėliau kanaloValdiklis perduos rašymo funkcijai.
     */
    public ByteBuffer[] rasyk(ByteBuffer[] buferiai) {
        m_antraste.clear();
        NIOIrankiai.nustatykPaketoDydiBuferyje(m_antraste, m_antraste.capacity(), (int) NIOIrankiai.likeBaitai(buferiai), m_bigEndian);
        m_antraste.flip();
        return NIOIrankiai.sumeskIViena(m_antraste, buferiai);
    }

}
