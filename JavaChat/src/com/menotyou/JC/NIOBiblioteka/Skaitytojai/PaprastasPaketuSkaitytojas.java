package com.menotyou.JC.NIOBiblioteka.Skaitytojai;

import java.nio.ByteBuffer;

import com.menotyou.JC.NIOBiblioteka.NIOIrankiai;
import com.menotyou.JC.NIOBiblioteka.ProtokoloPazeidimoIsimtis;

/**
 * Paketų skaitytojas kuris yra naudojamas šioje programoje. Jis naudoja
 * paketų antraštes tam, kad žinotų kokio ilgio paketą turi nuskaityti.
 */
public class PaprastasPaketuSkaitytojas implements PaketuSkaitytojas {

    private final boolean m_bigEndian;
    private final int m_antrastesDydis;

    /**
     * Sukuramas naujas paketų skaitytojas.
     *
     * @param antrastesDydis -> antraštes dydis
     * @param bigEndian -> kintamasis nurodantis kokio formatu sistemoje koduojami baitai.
     */
    public PaprastasPaketuSkaitytojas(int antrastesDydis, boolean bigEndian) {
        if (antrastesDydis < 1 || antrastesDydis > 4) throw new IllegalStateException("Antrastšs dydis turi būti tarp 1 ir 4, o dabar:" + antrastesDydis);
        m_bigEndian = bigEndian;
        m_antrastesDydis = antrastesDydis;
    }

    /**
     * Funkcija tikrina ar buferyje saugomo paketo ilgis atitinka, antraštėje nurodytą ilgį,
     * jei taip buferis paimamas, jei ne gražinamas null.
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
