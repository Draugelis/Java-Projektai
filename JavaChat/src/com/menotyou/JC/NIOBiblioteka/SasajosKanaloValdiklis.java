package com.menotyou.JC.NIOBiblioteka;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import com.menotyou.JC.NIOBiblioteka.Rasytojai.KanaloRasytojas;
import com.menotyou.JC.NIOBiblioteka.Rasytojai.PaketuRasytojas;
import com.menotyou.JC.NIOBiblioteka.Skaitytojai.GrynasPaketuSkaitytojas;
import com.menotyou.JC.NIOBiblioteka.Skaitytojai.KanaloSkaitytojas;
import com.menotyou.JC.NIOBiblioteka.Skaitytojai.PaketuSkaitytojas;

/**
 * Ši klasė skirtas valdyti kliento sasajos kanalą.
 */
public class SasajosKanaloValdiklis extends KanaloValdiklis implements NIOSasaja {

    /** The m_max eiles ilgis. */
    private int m_maxEilesIlgis;

    /** Laikas kada buvo sukurta sąsaja. */
    private long m_sujungimoLaikas;

    /** Baitų skaičius eilėje. */
    private final AtomicLong m_baitaiEileje;

    /** Kanalo siunčiamų paketų eilė.*/
    private ConcurrentLinkedQueue<Object> m_paketuEile;

    private PaketuSkaitytojas m_paketuSkaitytojas;
    private volatile SasajosStebetojas m_sasajosStebetojas;
    private final KanaloSkaitytojas m_kanaloSkaitytojas;
    private final KanaloRasytojas m_kanaloRasytojas;

    /**
     * Sukuriamas naujas sąsajos kanalo valdiklis.
     *
     * @param aptarnavimas -> NIOAptarnavimas objektas kuriame randamas kanalas.
     * @param kanalas -> kanalas kurį reikės valdyti.
     * @param adresas -> andresas iš kurio kanalas sukurtas.
     */
    public SasajosKanaloValdiklis(NIOAptarnavimas aptarnavimas, SelectableChannel kanalas, InetSocketAddress adresas) {
        super(aptarnavimas, kanalas, adresas);
        m_sasajosStebetojas = null;
        m_maxEilesIlgis = -1;
        m_sujungimoLaikas = -1;
        m_paketuSkaitytojas = GrynasPaketuSkaitytojas.NUMATYTASIS;
        m_baitaiEileje = new AtomicLong(0L);
        m_paketuEile = new ConcurrentLinkedQueue<Object>();
        m_kanaloSkaitytojas = new KanaloSkaitytojas(aptarnavimas);
        m_kanaloRasytojas = new KanaloRasytojas();
    }

    public boolean rasyk(byte[] paketas) {
        return rasyk(paketas, null);
    }

    public boolean sujungtas() {
        return gaukKanala().isConnected();
    }

    /**
     * Funkcija skirta rašyti į kanalą.
     * Įdėjus paketą į siunčiamų paketų eilę, NIOAptarnavimas objektas
     * įspėjamas, kad šis kanalas nori rašyti.
     * @return true arba false priklausomai nuo to ar pavyko įrašyti į eilę.
     */
    public boolean rasyk(byte[] paketas, Object zyme) {
        long dabartinisEilesIlgis = m_baitaiEileje.addAndGet(paketas.length);
        if (m_maxEilesIlgis > 0 && dabartinisEilesIlgis > m_maxEilesIlgis) {
            m_baitaiEileje.addAndGet(-paketas.length);
            return false;
        }
        m_paketuEile.offer(zyme == null ? paketas : new Object[] { paketas, zyme });
        gaukNIOAptarnavima().pridekIEile(new SusidomejimoPridejimas(SelectionKey.OP_WRITE));
        return true;
    }

    /**
     * Veiksmas r pridedamas į įvykių eilę.
     */
    public void pridekIEile(Runnable r) {
        m_paketuEile.offer(r);
        gaukNIOAptarnavima().pridekIEile(new SusidomejimoPridejimas(SelectionKey.OP_WRITE));
    }

    /**
     * Metodas įspėja su sąsają susietą stebėtoją(NIOKlientas arba Vartotojas), kad gautas paketas.
     *
     * @param paketas -> gautas paketas baitais.
     */
    private void ispekApieGautaPaketa(byte[] paketas) {
        try {
            if (m_sasajosStebetojas != null) m_sasajosStebetojas.paketasGautas(this, paketas);
        } catch (Exception e) {
            gaukNIOAptarnavima().ispekApieIsimti(e);
        }
    }

    /**
     * Metodas įspėja su sąsają susietą stebėtoją(NIOKlientas arba Vartotojas), kad paketas išsiųstas.
     *
     * @param zyme -> išsiųsto paketo žymė.
     */
    private void ispekApieIsiustaPaketa(Object zyme) {
        try {
            if (m_sasajosStebetojas != null) m_sasajosStebetojas.paketasIssiustas(this, zyme);
        } catch (Exception e) {
            gaukNIOAptarnavima().ispekApieIsimti(e);
        }
    }

    /**
     * Metodas apibrėžia kaip kanalo valdiklis turėtu elgtis jei su kanalu susietas
     * raktas sukelia OP_READ įvyki.
     */
    void paruostasSkaitymui() {
        if (!atidarytas()) return;
        try {
            if (!sujungtas()) throw new IOException("Kanalas nėra sujungtas");
            while (m_kanaloSkaitytojas.skaityk(gaukKanala()) > 0) {
                byte[] paketas;
                ByteBuffer buferis = m_kanaloSkaitytojas.gaukBuferi();
                while (buferis.remaining() > 0 && (paketas = m_paketuSkaitytojas.kitasPaketas(buferis)) != null) {
                    if (paketas == PaketuSkaitytojas.PRALEISK_PAKETA) continue;
                    ispekApieGautaPaketa(paketas);
                }
                m_kanaloSkaitytojas.supakuok();
            }
        } catch (Exception e) {
            issijunk(e);
        }
    }

    /**
     * Metodas užpildo siuntimų buferį, kurį vėliau bus galima išsiųsti kai
     * sąsaja praneš kad galima rašyti. T.y. bus sukeltas OP_WRITE įvykis.
     *
     * @throws IOException tipo iššimtis jei įvyksta klaida rašant.
     */
    private void uzpildykSiuntimuBuferi() throws IOException {
        if (m_kanaloRasytojas.tuscias()) {
            Object kitasPaketas = m_paketuEile.poll();
            while (kitasPaketas != null && kitasPaketas instanceof Runnable) {
                ((Runnable) kitasPaketas).run();
                kitasPaketas = m_paketuEile.poll();
            }
            if (kitasPaketas == null) return;
            byte[] duomenys;
            Object zyme = null;
            if (kitasPaketas instanceof byte[]) {
                duomenys = (byte[]) kitasPaketas;
            } else {
                duomenys = (byte[]) ((Object[]) kitasPaketas)[0];
                zyme = ((Object[]) kitasPaketas)[1];
            }
            m_kanaloRasytojas.pridekPaketa(duomenys, zyme);
            m_baitaiEileje.addAndGet(-duomenys.length);
        }
    }

    /**
     * Metodas apibrėžia kaip kanalo valdiklis turėtu elgtis jei su kanalu susietas
     * raktas sukelia OP_WRITE įvyki.
     */
    void paruostasRasymui() {
        try {
            panaikinkSusidomejima(SelectionKey.OP_WRITE);
            if (!atidarytas()) return;
            uzpildykSiuntimuBuferi();
            if (m_kanaloRasytojas.tuscias()) return;
            while (!m_kanaloRasytojas.tuscias()) {
                boolean baitaiBuvoIrasyti = m_kanaloRasytojas.rasyk(gaukKanala());
                if (!baitaiBuvoIrasyti) {
                    pridekSusidomejima(SelectionKey.OP_WRITE);
                    return;
                }
                if (m_kanaloRasytojas.tuscias()) {
                    ispekApieIsiustaPaketa(m_kanaloRasytojas.gaukZyme());
                    uzpildykSiuntimuBuferi();
                }
            }
        } catch (Exception e) {
            issijunk(e);
        }
    }

    /**
     *  Metodas apibrėžia kaip kanalo valdiklis turėtu elgtis jei su kanalu susietas
     *  raktas sukelia OP_CONNECT įvyki.
     *  T.y. kanalas kitame gale buvo sujungtas.
     */
    void paruostasSujungimui() {
        try {
            if (!atidarytas()) return;
            if (gaukKanala().finishConnect()) {
                panaikinkSusidomejima(SelectionKey.OP_CONNECT);
                m_sujungimoLaikas = System.currentTimeMillis();
                ispekStebetojaDelSujungimo();
            }
        } catch (Exception e) {
            issijunk(e);
        }
    }

    public void ispekKadBuvoAtsauktas() {
        uzdaryk();
    }

    public long gaukNuskaitytuBaituSkaiciu() {
        return m_kanaloSkaitytojas.gaukNuskaitytusBitus();
    }

    public SocketChannel gaukKanala() {
        return (SocketChannel) super.gaukKanala();
    }

    public long gaukParasytuBaituSkaiciu() {
        return m_kanaloRasytojas.gaukKiekParasytaBaitu();
    }

    public long gaukLaikaNuoSujungimo() {
        return m_sujungimoLaikas > 0 ? System.currentTimeMillis() - m_sujungimoLaikas : -1;
    }

    public long gaukRasymoEilesDydi() {
        return m_baitaiEileje.get();
    }

    public String toString() {
        try {
            return gaukSasaja().toString();
        } catch (Exception e) {
            return "Uzdaryta NIO Sąsaja";
        }
    }

    public int gaukMaxEilesIlgi() {
        return m_maxEilesIlgis;
    }

    /**
     * Metodas įspėja sąsajos stebėtoją(NIOKlientas arba Vartotojas), kad su serveriu ar klientu
     * buvo susisiekta ir ryšys užtvirtintas.
     */
    private void ispekStebetojaDelSujungimo() {
        try {
            if (m_sasajosStebetojas != null) m_sasajosStebetojas.rysysUztvirtintas(this);
        } catch (Exception e) {
            gaukNIOAptarnavima().ispekApieIsimti(e);
        }
    }

    /**
     * Metodas įspėja sąsajos stebėtoją(NIOKlientas arba Vartotojas), kad ryšys serveriu ar klientu
     * buvo nutrauktas.
     * @param isimtis -> išimtis dėl kurios įvyko nutraukimas.
     */
    private void ispekStebetojaDelAtsijungimo(Exception isimtis) {
        try {
            if (m_sasajosStebetojas != null) m_sasajosStebetojas.rysysNutrauktas(this, isimtis);
        } catch (Exception e) {
            gaukNIOAptarnavima().ispekApieIsimti(e);
        }
    }

    public void nustatykMaxEilesIlgi(int maxEilesIlgis) {
        m_maxEilesIlgis = maxEilesIlgis;
    }

    public void nustatykPaketuSkaitytoja(PaketuSkaitytojas paketuSkaitytojas) {
        m_paketuSkaitytojas = paketuSkaitytojas;
    }

    public void nustatykPaketuRasytoja(final PaketuRasytojas paketuRasytojas) {
        if (paketuRasytojas == null) throw new NullPointerException();
        pridekIEile(new Runnable() {
            public void run() {
                m_kanaloRasytojas.nustatykPaketuRasytoja(paketuRasytojas);
            }
        });
    }

    /**
     * Metodas priskiria sasajos kanalui stebėtoją.
     */
    public void stebek(SasajosStebetojas stebetojas) {
        pazymekKadStebetojasPriskirtas();
        gaukNIOAptarnavima().pridekIEile(new StebejimoPradziosIvykis(this, stebetojas));
    }

    /**
     * Metodas skirtas uždaryti sąsają po to kai bus baigta į ją rašyti.
     */
    public void uzsidarykPoRasymo() {
        pridekIEile(new Runnable() {
            public void run() {
                m_paketuEile.clear();
                issijunk(null);
            }
        });
    }

    public Socket gaukSasaja() {
        return gaukKanala().socket();
    }

    void raktasPriskirtas() {
        if (!sujungtas()) {
            pridekSusidomejima(SelectionKey.OP_CONNECT);
        }
    }

    /**
     * Metodas išjungia sąsajos kanalo valdiklį ir įspėja dėl atsijungimo.
     */
    protected void issijunk(Exception e) {
        m_sujungimoLaikas = -1;
        m_paketuEile.clear();
        m_baitaiEileje.set(0);
        ispekStebetojaDelAtsijungimo(e);
    }

    /**
     * Klasė/įvykis, kurio pagalba kanalui pridedamas kokios nors operacijos susidomėjimas,
     * tai daroma per įvykį tam kad išvengti susidomėjimo pakeitimo viduryje operacijos.
     */
    private class SusidomejimoPridejimas implements Runnable {

        private final int m_susidomejimas;

        private SusidomejimoPridejimas(int susidomejimas) {
            m_susidomejimas = susidomejimas;
        }

        public void run() {
            pridekSusidomejima(m_susidomejimas);
        }
    }

    /**
     * Klasė/įvykis iškviečiamas kai reikia kanalui priskirti stebėtoją ir 
     * pranešti kad nurodytu kanalu galima laukti įvesties, t.y. OP_READ įvykių.
     */
    private class StebejimoPradziosIvykis implements Runnable {

        private final SasajosStebetojas m_naujasStebetojas;
        private final SasajosKanaloValdiklis m_valdiklis;

        private StebejimoPradziosIvykis(SasajosKanaloValdiklis valdiklis, SasajosStebetojas stebetojas) {
            m_valdiklis = valdiklis;
            m_naujasStebetojas = stebetojas;
        }

        public void run() {
            m_valdiklis.m_sasajosStebetojas = m_naujasStebetojas;
            if (m_valdiklis.sujungtas()) {
                m_valdiklis.ispekStebetojaDelSujungimo();
            }
            if (!m_valdiklis.atidarytas()) {
                m_valdiklis.ispekStebetojaDelAtsijungimo(null);
            }
            m_valdiklis.pridekSusidomejima(SelectionKey.OP_READ);
        }

        public String toString() {
            return "Pradedama stebeti [" + m_naujasStebetojas + "]";
        }
    }

}
