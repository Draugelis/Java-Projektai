package com.menotyou.JC.NIOBiblioteka.Skaitytojai;

import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.menotyou.JC.NIOBiblioteka.NIOAptarnavimas;
import com.menotyou.JC.NIOBiblioteka.NIOIrankiai;

/**
 * KLasė skirta skaityti informaciją iš nurodyto kanalo.
 */
public class KanaloSkaitytojas {

    private final NIOAptarnavimas m_aptarnavimas;

    /** Anksčiau nepilnai nuskaityti baitai. */
    private ByteBuffer m_ankstesniBaitai;

    /** Vienu kartu nuskaitytų baitų skaičius. */
    private long m_nuskaitytiBaitai;

    /**
     * Sukuriamas naujas kanalo skaitytojas.
     *
     * @param aptarnavimas -> NIOAptarnavimas objektas kuriam priklauso
     * kanalas ar jame jie saugomi
     */
    public KanaloSkaitytojas(NIOAptarnavimas aptarnavimas) {
        m_aptarnavimas = aptarnavimas;
        m_nuskaitytiBaitai = 0;
    }

    /**
     * Funkcija gražina nuskaitytų iš kanalo baitų skaičių.
     * Nuskaitoma tiek kiek galima vienu kartu, arba tiek kiek
     * duotu momentu yra kompiuterio registre.
     * Jei yra anksčiau nuskaitytų ir negražintų baitu. Jie supakuojami,
     * įterpiami į pagrindinį buferį ir vietinis baitų buferis išvalomas.
     *
     * @param kanalas -> kanalas iš kurio skaitoma.
     * @return nuskaitytu baitų skaičius.
     * @throws IOException tipo išimtis kuri kyla skaitymo metu.
     */
    public int skaityk(SocketChannel kanalas) throws IOException {
        ByteBuffer buferis = gaukBuferi();

        buferis.clear();
        if (m_ankstesniBaitai != null) {
            buferis.position(m_ankstesniBaitai.remaining());
        }
        int nuskaityta = kanalas.read(buferis);
        if (nuskaityta < 0) throw new EOFException("Bufferis nuskaitė -1");
        if (!buferis.hasRemaining()) throw new BufferOverflowException();

        m_nuskaitytiBaitai += nuskaityta;
        if (nuskaityta == 0) return 0;
        if (m_ankstesniBaitai != null) {
            int pozicija = buferis.position();
            buferis.position(0);
            buferis.put(m_ankstesniBaitai);
            buferis.position(pozicija);
            m_ankstesniBaitai = null;
        }
        buferis.flip();

        return nuskaityta;
    }

    /**
     * Metodas supakuoja ir sudeda baitus į vietinį buferį
     * iš pagrindinio buferio.
     */
    public void supakuok() {
        ByteBuffer buferis = gaukBuferi();
        if (buferis.remaining() > 0) {
            m_ankstesniBaitai = NIOIrankiai.kopijuok(buferis);
        }
    }

    public long gaukNuskaitytusBitus() {
        return m_nuskaitytiBaitai;
    }

    public ByteBuffer gaukBuferi() {
        return m_aptarnavimas.gaukBendraBuferi();
    }

}
