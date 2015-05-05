package com.menotyou.JC.NIOBiblioteka;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Klasė kuri atsako už bendravimą tarp sąsajų internetu.
 */
public class NIOAptarnavimas {

    /** Numatytasis įvesties ir išvesties bufferis. */
    public final static int NUMATYTASIS_IO_BUFFERIO_DYDIS = 64 * 1024;

    /** Selektorius - objektas atsakingas už atskiro kanalo pasirinkimą. */
    private final Selector m_selektorius;

    /** Vidinė įvykių eilė skirta selektoriaus įvykiams tvarkyti. */
    private final Queue<Runnable> m_vidineIvykiuEile;

    private ByteBuffer m_bendrasBuferis;
    private IsimciuStebetojas m_isimciuStebetojas;

    /**
     * Sukuriamas numatytasis NIOAptarnavimas objektas su numatytu buferio dydžiu.
     *
     * @throws IOException tipo iššimtis jei įvyksta klaida kuriant sąsają.
     */
    public NIOAptarnavimas() throws IOException {
        this(NUMATYTASIS_IO_BUFFERIO_DYDIS);
    }

    /**
     * Sukuriamas NIOAptarnavimas objektas su nurodytu buferio dydžiu.
     *
     * @param ioBuferioDydis -> buferio dydis.
     *  @throws IOException tipo iššimtis jei įvyksta klaida kuriant sąsają.
     */
    public NIOAptarnavimas(int ioBuferioDydis) throws IOException {
        m_selektorius = Selector.open();
        m_vidineIvykiuEile = new ConcurrentLinkedQueue<Runnable>();
        m_isimciuStebetojas = IsimciuStebetojas.NUMATYTASIS;
        nustatykBuferioDydi(ioBuferioDydis);
    }

    /**
     * Metodas įvykdo visus eilėje esančius veiksmus ir laukia kol selektorius
     * gražins bent vieną naują įvesties ar išvesties įvyki.
     *
     *  @throws IOException tipo iššimtis jei įvyksta klaida skaitymo ar rašimo metu.
     */
    public synchronized void pasirinkBlokuodamas() throws IOException {

        vykdykEile();
        if (m_selektorius.select() > 0) {
            apdorokPasirinktusRaktus();
        }
        vykdykEile();
    }

    /**
     * Metodas įvykdo visus eilėje esančius veiksmus ir pasirenk visus turimus
     * IO įvykius nelaukdamas naujų.
     *
     *  @throws IOException tipo iššimtis jei įvyksta klaida skaitymo ar rašimo metu.
     */
    public synchronized void pasirinkNeblokuodamas() throws IOException {
        vykdykEile();
        if (m_selektorius.selectNow() > 0) {
            apdorokPasirinktusRaktus();
        }
        vykdykEile();
    }

    /**
     * Metodas įvykdo visus eilėje esančius veiksmus ir laukia nurodyta laiką kol selektorius
     * gražins bent vieną naują įvesties ar išvesties įvyki.
     * @param pauzesLaikas -> 
     * @throws IOException tipo iššimtis jei įvyksta klaida skaitymo ar rašimo metu.
     */
    public synchronized void pasirinkBlokuodamas(long pauzesLaikas) throws IOException {
        vykdykEile();
        if (m_selektorius.select(pauzesLaikas) > 0) {
            apdorokPasirinktusRaktus();
        }
        vykdykEile();
    }

    /**
     * Metodas įvykdo visus eilėje esančius veiksmus.
     */
    private void vykdykEile() {
        Runnable ivykis;
        while ((ivykis = m_vidineIvykiuEile.poll()) != null) {
            try {
                ivykis.run();
            } catch (Throwable t) {
                ispekApieIsimti(t);
            }
        }
    }

    /**
     * Nustato buferio dydi.
     *
     * @param naujasBuferioDysis -> naujas buferio dysis
     */
    public void nustatykBuferioDydi(int naujasBuferioDysis) {
        if (naujasBuferioDysis < 256) throw new IllegalArgumentException("Buferis negali buti mažesnins nei 256 baitai");
        m_bendrasBuferis = ByteBuffer.allocate(naujasBuferioDysis);
    }

    public int gaukBuferioDydi() {
        return m_bendrasBuferis.capacity();
    }

    public ByteBuffer gaukBendraBuferi() {
        return m_bendrasBuferis;
    }

    /**
     * Sukuriama sąsaja su nurodytu kūrėju (host).
     * Pasirenkamas neblokavimo rėžimas.
     * Iškviečiama funkcija registruokSasajosKanala(), kuri
     * užregistruoja ir užbaigia kurti sąsają.
     *
     * @param kurejas -> kūrėjas(host) su kuriuo norima susisiekti.
     * @param portas -> portas kuriuo kūrėjas laukiaryšio.
     * @return NIOSasaja objektas.
     * @throws IOException tipo iššimtis jei iškyla kliada kuriant sąsają.
     */
    public NIOSasaja sukurkSasaja(String kurejas, int portas) throws IOException {
        return sukurkSasaja(InetAddress.getByName(kurejas), portas);
    }

    public NIOSasaja sukurkSasaja(InetAddress inetAdresas, int portas) throws IOException {
        SocketChannel kanalas = SocketChannel.open();
        kanalas.configureBlocking(false);
        InetSocketAddress adresas = new InetSocketAddress(inetAdresas, portas);
        kanalas.connect(adresas);
        return registruokSasajosKanala(kanalas, adresas);
    }

    /**
     * Sukuriama serverio sąsaja. Ji skiriasi nuo parastos sąsajos tuo,
     * kad ji gali tik priimti naujus ryšius- ja rašyti ir skaityti negalima.
     *
     * @param portas the portas
     * @return the NIO serverio sasaja
     * @throws IOException tipo iššimtis jei iškyla kliada kuriant sąsają.
     */
    public NIOServerioSasaja sukurkServerioSasaja(int portas) throws IOException {
        return sukurkServerioSasaja(portas, -1);
    }

    public NIOServerioSasaja sukurkServerioSasaja(int portas, int limitas) throws IOException {
        return sukurkServerioSasaja(new InetSocketAddress(portas), limitas);
    }

    public NIOServerioSasaja sukurkServerioSasaja(InetSocketAddress adresas, int limitas) throws IOException {
        ServerSocketChannel kanalas = ServerSocketChannel.open();
        kanalas.socket().setReuseAddress(true);
        kanalas.socket().bind(adresas, limitas);
        kanalas.configureBlocking(false);
        ServerioSasajosKanaloValdiklis kanaloValdiklis = new ServerioSasajosKanaloValdiklis(this, kanalas, adresas);
        pridekIEile(new KanaloRegistravimoIvykis(kanaloValdiklis));
        return kanaloValdiklis;
    }

    /**
     * Funkcija užregistruoja naują sąsają ir priskiria jai valdiklį.
     *
     * @param kanalas -> kanalas, kurį norima užregistruoti.
     * @param adresas -> adresas iš kurio jungiamas kanalas.
     * @return NIOSasaja objektas į kurį įvilktas SasajosKanaloValdiklis objektas.
     * @throws IOException tipo iššimtis jei iškyla kliada kuriant sąsają.
     */
    NIOSasaja registruokSasajosKanala(SocketChannel kanalas, InetSocketAddress adresas) throws IOException {
        kanalas.configureBlocking(false);
        SasajosKanaloValdiklis kanaloValdiklis = new SasajosKanaloValdiklis(this, kanalas, adresas);
        pridekIEile(new KanaloRegistravimoIvykis(kanaloValdiklis));
        return kanaloValdiklis;
    }

    /**
     * Metodas apdoroja pasirinkus raktus.
     */
    private void apdorokPasirinktusRaktus() {
        Iterator<SelectionKey> it = m_selektorius.selectedKeys().iterator();
        while (it.hasNext()) {
            SelectionKey raktas = it.next();
            it.remove();
            try {
                apdorokRakta(raktas);
            } catch (Throwable t) {
                ispekApieIsimti(t);
            }
        }
    }

    /**
     * Metodas apdoroja vieną raktą.
     *
     * @param raktas -> raktas kurį ketinama apdoroti.
     */
    private void apdorokRakta(SelectionKey raktas) {
        KanaloValdiklis valdiklis = (KanaloValdiklis) raktas.attachment();
        try {
            if (raktas.isReadable()) {
                valdiklis.paruostasSkaitymui();
            }
            if (raktas.isWritable()) {
                valdiklis.paruostasRasymui();
            }
            if (raktas.isAcceptable()) {
                valdiklis.paruostasPriemimui();
            }
            if (raktas.isConnectable()) {
                valdiklis.paruostasSujungimui();
            }
        } catch (CancelledKeyException e) {
            System.out.println("Raktas atšauktas");
            valdiklis.uzdaryk(e);
        }
    }

    public void isjunk() {
        if (!atidarytas()) return;
        pridekIEile(new IsjungimoIvykis());
    }

    public boolean atidarytas() {
        return m_selektorius.isOpen();
    }

    public void pridekIEile(Runnable ivykis) {
        m_vidineIvykiuEile.add(ivykis);
        pabusk();
    }

    public Queue<Runnable> gaukEile() {
        return new LinkedList<Runnable>(m_vidineIvykiuEile);
    }

    /**
     * Praneša Selector objektui, kad reikia paleisti select veiksmą.
     * T.y. jei duotu momentu selektorius yra užblokuotas select() funkcijos
     * šiuo metodu blokavimas atšaukiamas.
     */
    public void pabusk() {
        m_selektorius.wakeup();
    }

    public void nustatykIsimciuPriziuretoja(IsimciuStebetojas isimciuStebetojas) {
        final IsimciuStebetojas naujasIsimciuStebetojas = isimciuStebetojas == null ? IsimciuStebetojas.NUMATYTASIS : isimciuStebetojas;
        pridekIEile(new Runnable() {
            public void run() {
                m_isimciuStebetojas = naujasIsimciuStebetojas;
            }
        });
    }

    public void ispekApieIsimti(Throwable t) {
        try {
            m_isimciuStebetojas.ispekApieIsimti(t);
        } catch (Exception e) {
            System.err.println("Nepavyko įrašyti šios iššimties į išimčių stebėtoją:");
            System.err.println(e);
            e.printStackTrace();
        }
    }

    /**
     * Privati klasė/įvykis, kuris vykdomas kas kart kai reikia užregistruoti naują kanalą.
     */
    private class KanaloRegistravimoIvykis implements Runnable {

        /** Kanalo valdiklis kuriam reikia priskirti kanalą. */
        private final KanaloValdiklis m_kanaloValdiklis;

        private KanaloRegistravimoIvykis(KanaloValdiklis valdiklis) {
            m_kanaloValdiklis = valdiklis;
        }

        public void run() {
            try {
                System.out.println("Registruojamas naujas kanalas adresu: " + m_kanaloValdiklis.gaukAdresa());
                SelectionKey raktas = m_kanaloValdiklis.gaukKanala().register(m_selektorius, m_kanaloValdiklis.gaukKanala().validOps());
                m_kanaloValdiklis.nustatykRakta(raktas);
                raktas.attach(m_kanaloValdiklis);
            } catch (Exception e) {
                System.out.println("Nepavyko užregistruoti " + m_kanaloValdiklis.gaukAdresa());
                m_kanaloValdiklis.uzdaryk(e);
            }
        }

        public String toString() {
            return "Registruojamas [" + m_kanaloValdiklis + "]";
        }
    }

    /**
     * Privati klasė/įvykis, kuris vykdomas kas kart kai reikia išjungti NIOAptarnavimo procesą.
     * Visi su procesu susieti kanalų raktai yra atšaukiami, o kanalų valdikliai uždaromi.
     */
    private class IsjungimoIvykis implements Runnable {

        public void run() {
            if (!atidarytas()) return;
            for (SelectionKey raktas : m_selektorius.keys()) {
                try {
                    NIOIrankiai.tyliaiAtsaukRakta(raktas);
                    ((KanaloValdiklis) raktas.attachment()).uzdaryk();
                } catch (Exception e) {

                }
            }
            try {
                m_selektorius.close();
            } catch (IOException e) {

            }
        }
    }
}
