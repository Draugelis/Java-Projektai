package com.menotyou.JC.NIOBiblioteka;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Ši klasė yra skirta valdyti serverio sąsają.
 */
public class ServerioSasajosKanaloValdiklis extends KanaloValdiklis implements NIOServerioSasaja {

    private long m_isVisoAtmestaPrisijungimu;
    private long m_isVisoPriimtaPrisijungimu;
    private long m_isVisoNepavykusiuPrisijungimu;
    private long m_isVisoPrisijungimu;

    private volatile PrisijungimuFiltras m_prisijungimuFiltras;
    private ServerioSasajosStebetojas m_stebetojas;

    /**
     * Sukuriamas naujas sąsajos kanalo valdiklis.
     *
     * @param aptarnavimas -> NIOAptarnavimas objektas kuriame yra kanalas.
     * @param kanalas -> kanalas, kurio sąsają ši klasė valdys.
     * @param adresas -> adresas iš kuruo kanala sukurtas.
     */
    protected ServerioSasajosKanaloValdiklis(NIOAptarnavimas aptarnavimas, SelectableChannel kanalas, InetSocketAddress adresas) {
        super(aptarnavimas, kanalas, adresas);
        m_stebetojas = null;
        nustatykPrisijungimuFiltra(PrisijungimuFiltras.LEISK_VISUS);
        m_isVisoAtmestaPrisijungimu = 0;
        m_isVisoPriimtaPrisijungimu = 0;
        m_isVisoNepavykusiuPrisijungimu = 0;
        m_isVisoPrisijungimu = 0;
    }

    public long gaukVisuSujungimuSkaiciu() {
        return m_isVisoPrisijungimu;
    }

    public long gaukVisuAtmestuSujungimuSkaiciu() {
        return m_isVisoAtmestaPrisijungimu;
    }

    public long gaukVisuPriimtuSujungimuSkaiciu() {
        return m_isVisoPriimtaPrisijungimu;
    }

    public long gaukVisuNepavykusiuSujungimuSkaiciu() {
        return m_isVisoNepavykusiuPrisijungimu;
    }

    /**
     * Metodas priskiria stebėjoją serverio sąsajai.
     */
    public void stebek(ServerioSasajosStebetojas stebetojas) {
        if (stebetojas == null) throw new NullPointerException();
        pazymekKadStebetojasPriskirtas();
        gaukNIOAptarnavima().pridekIEile(new StebejimoPradziosIvykis(stebetojas));
    }

    /**
     * Metodas įspėja stebėtoją, kad buvo aptiktas naujas ryšys.
     *
     * @param sasaja -> sąsaja su kuria buvo užmegstas naujas ryšys.
     */
    private void ispekApieNaujaRysi(NIOSasaja sasaja) {
        try {
            if (m_stebetojas != null) m_stebetojas.naujasSujungimas(sasaja);
        } catch (Exception e) {
            gaukNIOAptarnavima().ispekApieIsimti(e);
            sasaja.uzdaryk();
        }
    }

    /**
     * Metodas įspėja stebėtoją, kad nepavyko sujungimas.
     *
     * @param isimtis -> išimtis dėl kurios nepavyko sujungimas.
     */
    private void ispekApieNepavkusiSujungima(IOException isimtis) {
        try {
            if (m_stebetojas != null) m_stebetojas.priemimasNepavyko(isimtis);
        } catch (Exception e) {
            gaukNIOAptarnavima().ispekApieIsimti(e);
        }
    }

    /**
     * Metodas įspėja stebėtoją, kad sąsaja mirė.
     *
     * @param isimtis -> Išimtis dėl kurio sąsaja mirė.
     */
    private void ispekStebetojaKadSasajaMire(Exception isimtis) {
        try {
            if (m_stebetojas != null) m_stebetojas.serverioSasajaMire(isimtis);
        } catch (Exception e) {
            gaukNIOAptarnavima().ispekApieIsimti(e);
        }
    }

    /**
     * Metodas apibrėžia kaip kanalo valdiklis turėtu elgtis jei su kanalu susietas
     * raktas sukelia OP_ACCEPT įvyki.
     */
    void paruostasPriemimui() {
        m_isVisoPrisijungimu++;
        SocketChannel kanalas = null;
        try {
            kanalas = gaukKanala().accept();
            if (kanalas == null) {
                m_isVisoPrisijungimu--;
                return;
            }
            InetSocketAddress adresas = (InetSocketAddress) kanalas.socket().getRemoteSocketAddress();
            if (!m_prisijungimuFiltras.priimkPrisijungima(adresas)) {
                m_isVisoAtmestaPrisijungimu++;
                NIOIrankiai.tyliaiUzdarykKanala(kanalas);
                return;
            }
            ispekApieNaujaRysi(registruokSasaja(kanalas, adresas));
            m_isVisoPrisijungimu++;
        } catch (IOException e) {
            NIOIrankiai.tyliaiUzdarykKanala(kanalas);
            m_isVisoNepavykusiuPrisijungimu++;
            ispekApieNepavkusiSujungima(e);
        }
    }

    public ServerSocketChannel gaukKanala() {
        return (ServerSocketChannel) super.gaukKanala();
    }

    void raktasPriskirtas() {
        pridekSusidomejima(SelectionKey.OP_ACCEPT);
    }

    /**
     * Funkcija iš NIOAptarnavimas objekto užregistruoja naują sąsają.
     * 
     * @param kanalas -> kanalas su kurio bus susieta sąsaja.
     * @param adresas -> kanalo adresas.
     * @return NIOSasaja objektas.
     * @throws IOException tipo iššimtis, kuri išmetama jei įvyksta klaida kuriant sąsają.
     */
    NIOSasaja registruokSasaja(SocketChannel kanalas, InetSocketAddress adresas) throws IOException {
        return gaukNIOAptarnavima().registruokSasajosKanala(kanalas, adresas);
    }

    protected void issijunk(Exception e) {
        ispekStebetojaKadSasajaMire(e);
    }

    public void nustatykPrisijungimuFiltra(PrisijungimuFiltras filtras) {
        m_prisijungimuFiltras = (filtras == null ? PrisijungimuFiltras.ATMESK_VISUS : filtras);
    }

    /**
     * Klasė/įvykis iškviečiamas kai reikia kanalui priskirti stebėtoją ir 
     * pranešti kad nurodytu kanalu galima laukti prisijungimų, t.y. OP_ACCEPT įvykių.
     */
    private class StebejimoPradziosIvykis implements Runnable {

        private final ServerioSasajosStebetojas m_naujasStebetojas;

        private StebejimoPradziosIvykis(ServerioSasajosStebetojas stebetojas) {
            m_naujasStebetojas = stebetojas;
        }

        public void run() {
            m_stebetojas = m_naujasStebetojas;
            if (!atidarytas()) {
                ispekStebetojaKadSasajaMire(null);
                return;
            }
            pridekSusidomejima(SelectionKey.OP_ACCEPT);
        }

        public String toString() {
            return "Pradedama stebeti [" + m_naujasStebetojas + "]";
        }
    }

    public ServerSocket gaukSasaja() {
        return gaukKanala().socket();
    }

}
