package com.menotyou.JC.NIOBiblioteka;

import java.net.InetSocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

/**
 * KanaloValdiklis atsakingas už visas kanalo operacijas ir suteikai kanaliu
 * funkcionalumą.
 * Ši klasė yra bendra forma SasajosKanaloValdikliui ir ServerioSasajosKanaloValdikliui.
 * Tai leidžia NIOAptarnavimas objekte naudoti bendrą KanaloValdiklis objektą
 * neišskiriant šių dviejų klasių.
 */
public abstract class KanaloValdiklis implements NIOAbstraktiSasaja {

    private final NIOAptarnavimas m_aptarnavimas;

    /** Kanalo ip adresas.*/
    private final String m_ip;

    /** Kanalo adresas. */
    private final InetSocketAddress m_adresas;

    /** Kanalo portas (Port). */
    private final int m_portas;

    /** Kanalas. */
    private final SelectableChannel m_kanalas;

    /** Boolean kintamasis nusakantis ar kanalas atidarytas.
     *  Volatile reiškia lengvai keičiamas iš kelių procesų.
     */
    private volatile boolean m_atidarytas;

    /** Raktas su kuriuo yra susietas šis kanalas. */
    private volatile SelectionKey m_raktas;

    /** Operacijos kurias gali atlikti šis kanalas. */
    private volatile int m_dominanciosOperacijos;

    /** Boolean kintamasis nusakantis ar šis kanalas yra stebimas
     * ir iš jo laikiama duomenų.*/
    private boolean m_stebetojasPriskirtas;

    /** Kanalo asmeninė žymė. */
    private Object m_zyme;

    /**
     * Sukuriamas naujas kanalo valdiklis.
     *
     * @param aptarnavimas -> NIOAptarnavimas objektas kuriame saugomas kanalas.
     * @param kanalas -> kanalas kuriam priskiriamas valdiklis.
     * @param adresas -> InetSocketAdress objektas iš kurio gaunama informacija apie kanalo šaltinį.
     */
    protected KanaloValdiklis(NIOAptarnavimas aptarnavimas, SelectableChannel kanalas, InetSocketAddress adresas) {
        m_kanalas = kanalas;
        m_aptarnavimas = aptarnavimas;
        m_atidarytas = true;
        m_raktas = null;
        m_dominanciosOperacijos = 0;
        m_adresas = adresas;
        m_ip = adresas.getAddress().getHostAddress();
        m_portas = adresas.getPort();
        m_zyme = null;
    }

    public void uzdaryk() {
        uzdaryk(null);
    }

    public InetSocketAddress gaukAdresa() {
        return m_adresas;
    }

    public boolean atidarytas() {
        return m_atidarytas;
    }

    public String gaukIp() {
        return m_ip;
    }

    public int gaukPorta() {
        return m_portas;
    }

    public Object gaukZyme() {
        return m_zyme;
    }

    public void nustatykZyme(Object zyme) {
        m_zyme = zyme;
    }

    protected NIOAptarnavimas gaukNIOAptarnavima() {
        return m_aptarnavimas;
    }

    /**
     * Metodas pažymi, kad kanalo stebėtojas buvo priskirtas.
     */
    protected void pazymekKadStebetojasPriskirtas() {
        System.out.println("Zymima kad stebetojas priskirtas.");
        synchronized (this) {
            if (m_stebetojasPriskirtas) throw new IllegalStateException("Stebėtojas jau priskirtas");
            m_stebetojasPriskirtas = true;
        }
    }

    /**
     * Šis metodas bus relizuojamas  kitose klasėse.
     */
    void paruostasSkaitymui() {
        throw new UnsupportedOperationException(getClass() + " nepalaiko skaitymo");
    }

    /**
     * Šis metodas bus relizuojamas  kitose klasėse.
     */
    void paruostasRasymui() {
        throw new UnsupportedOperationException(getClass() + " nepalaiko rašymo");
    }

    /**
     * Šis metodas bus relizuojamas  kitose klasėse.
     */
    void paruostasPriemimui() {
        throw new UnsupportedOperationException(getClass() + " nepalaiko priėmimo");
    }

    /**
     * Šis metodas bus relizuojamas  kitose klasėse.
     */
    void paruostasSujungimui() {
        throw new UnsupportedOperationException(getClass() + " nepalaiko sujungimo");
    }

    protected SelectableChannel gaukKanala() {
        return m_kanalas;
    }

    /**
     * Metodas nustato kanalo raktą.
     * Šį metodą paveldi tiek SasajosKanaloValdiklis, tiek ServerioSasajosKanaloValdiklis.
     * 
     * @param raktas -> raktas su kuriuo susiejamas kanalas.
     */
    void nustatykRakta(SelectionKey raktas) {
        if (m_raktas != null) throw new IllegalStateException("Bandyta priskirti raktą dukart");
        m_raktas = raktas;
        if (!atidarytas()) {
            NIOIrankiai.tyliaiAtsaukRakta(m_raktas);
            return;
        }
        raktasPriskirtas();
        sinchronizuokRaktoDominanciasOperacijas();
    }

    protected SelectionKey gaukRakta() {
        return m_raktas;
    }

    /**
     * metodas bus realizuojamas kitose klasėse.
     */
    abstract void raktasPriskirtas();

    /**
     * Metodas skirtas uždaryti kanalą.
     * Jei nenurodoma iššimtis, kanalas uždaromas
     * tyliai.
     *
     * @param isimtis -> Iššimtis dėl kurios uždaromas kanalas.
     */
    protected void uzdaryk(Exception isimtis) {
        if (atidarytas()) {
            gaukNIOAptarnavima().pridekIEile(new UzdarymoIvykis(this, isimtis));
        }
    }

    /**
     * Metodas skirtas sinchronizuoti kanalo operacijas.
     * Pagrinde naudojamas tuomet kai reikia pažymėti, kad
     * kanalas jau prijungtas ir operacija OP_CONNECT nebedomina.
     */
    private void sinchronizuokRaktoDominanciasOperacijas() {
        if (m_raktas != null) {
            try {
                int senosOperacijos = m_raktas.interestOps();
                if ((m_dominanciosOperacijos & SelectionKey.OP_CONNECT) != 0) {
                    m_raktas.interestOps(SelectionKey.OP_CONNECT);
                } else {
                    m_raktas.interestOps(m_dominanciosOperacijos);
                }
                if (m_raktas.interestOps() != senosOperacijos) {
                    m_aptarnavimas.pabusk();
                }
            } catch (CancelledKeyException e) {

            }
        }
    }

    /**
     * Naudojamasi bitwise operacija & ir taip iš
     * skaičiaus susidomėjimas pašalinami 
     * nereikalingos operacijos bitai.
     * ~ yra anti ženklas 0000 0001 virsta 1111 1110.
     * pvz. 
     *       0111 0110 $= ~0000 0110
     * tuomet: 
     * 		 0111 0110 $= 1111 1001
     * lygu: 
     * 		 0111 0000
     *
     * @param susidomejimas -> skaičius kurį norima pašalinti.
     */
    protected void panaikinkSusidomejima(int susidomejimas) {
        m_dominanciosOperacijos &= ~susidomejimas;
        sinchronizuokRaktoDominanciasOperacijas();
    }

    /**
     * Naudojamasi bitwise operacija | ir taip pridedami
     * skaičius susidomėjimas bitai į dominančias operacijas.
     * 
     * pvz. 
     *       0111 0000 |= 0000 0110
     * lygu: 
     * 		 0111 0110
     *
     * @param susidomejimas -> skaičius kuri norima pridėti
     */
    protected void pridekSusidomejima(int susidomejimas) {
        m_dominanciosOperacijos |= susidomejimas;
        sinchronizuokRaktoDominanciasOperacijas();
    }

    public String toString() {
        return m_ip + ":" + m_portas;
    }

    /**
     * metodas bus realizuojamas kitose klasėse.
     *
     * @param e -> išimtis dėl kurios išjungiamas kanalo valdiklis.
     */
    protected abstract void issijunk(Exception e);

    /**
     * Privati klasė kuri talpina įvykį kuris turėtų uždaryti kanalą.
     */
    private static class UzdarymoIvykis implements Runnable {

        private final KanaloValdiklis m_valdiklis;
        private final Exception m_isimtis;

        /**
         * Sukuriamas naujas įvykis
         *
         * @param valdiklis -> valdiklis kurį ketinama uždaryti.
         * @param e -> iššimtis kuri yra uždarymo priežastis.
         */
        private UzdarymoIvykis(KanaloValdiklis valdiklis, Exception e) {
            m_valdiklis = valdiklis;
            m_isimtis = e;
        }

        public void run() {
            if (m_valdiklis.atidarytas()) {
                m_valdiklis.m_atidarytas = false;
                NIOIrankiai.tyliaiUzdarykRaktaIrKanala(m_valdiklis.gaukRakta(), m_valdiklis.gaukKanala());
                m_valdiklis.issijunk(m_isimtis);
            }
        }
    }
}
