package com.menotyou.JC.NIOBiblioteka;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ServerioSasajosKanaloValdiklis extends KanaloValdiklis implements NIOServerioSasaja {

	private long m_isVisoAtmestaPrisijungimu;
	private long m_isVisoPriimtaPrisijungimu;
	private long m_isVisoNepavykusiuPrisijungimu;
	private long m_isVisoPrisijungimu;
	private volatile PrisijungimuFiltras m_prisijungimuFiltras;
	private ServerioSasajosStebetojas m_stebetojas;

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

	public void stebek(ServerioSasajosStebetojas stebetojas) {
		if (stebetojas == null) throw new NullPointerException();
		pazymekKadStebetojasPriskirtas();
		gaukNIOAptarnavima().pridekIEile(new StebejimoPradziosIvykis(stebetojas));
	}

	private void ispekApieNaujaRysi(NIOSasaja sasaja) {
		try {
			if (m_stebetojas != null) m_stebetojas.naujasSujungimas(sasaja);
		} catch (Exception e) {
			gaukNIOAptarnavima().ispekApieIsimti(e);
			sasaja.uzdaryk();
		}
	}

	private void ispekApieNepavkusiSujungima(IOException isimtis) {
		try {
			if (m_stebetojas != null) m_stebetojas.priemimasNepavyko(isimtis);
		} catch (Exception e) {
			gaukNIOAptarnavima().ispekApieIsimti(e);
		}
	}

	private void ispekStebetojaKadSasajaMire(Exception isimtis) {
		try {
			if (m_stebetojas != null) m_stebetojas.serverioSasajaMire(isimtis);
		} catch (Exception e) {
			gaukNIOAptarnavima().ispekApieIsimti(e);
		}
	}

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

	NIOSasaja registruokSasaja(SocketChannel kanalas, InetSocketAddress adresas) throws IOException {
		return gaukNIOAptarnavima().registruokSasajosKanala(kanalas, adresas);
	}

	protected void issijunk(Exception e) {
		ispekStebetojaKadSasajaMire(e);
	}

	public void nustatykPrisijungimuFiltra(PrisijungimuFiltras filtras) {
		m_prisijungimuFiltras =filtras == null ? PrisijungimuFiltras.ATMESK_VISUS : filtras;
	}

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
