package com.menotyou.JC.NIOBiblioteka;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

// TODO: Auto-generated Javadoc
/**
 * The Class ServerioSasajosKanaloValdiklis.
 */
public class ServerioSasajosKanaloValdiklis extends KanaloValdiklis implements NIOServerioSasaja {

	/** The m_is viso atmesta prisijungimu. */
	private long m_isVisoAtmestaPrisijungimu;
	
	/** The m_is viso priimta prisijungimu. */
	private long m_isVisoPriimtaPrisijungimu;
	
	/** The m_is viso nepavykusiu prisijungimu. */
	private long m_isVisoNepavykusiuPrisijungimu;
	
	/** The m_is viso prisijungimu. */
	private long m_isVisoPrisijungimu;
	
	/** The m_prisijungimu filtras. */
	private volatile PrisijungimuFiltras m_prisijungimuFiltras;
	
	/** The m_stebetojas. */
	private ServerioSasajosStebetojas m_stebetojas;

	/**
	 * Instantiates a new serverio sasajos kanalo valdiklis.
	 *
	 * @param aptarnavimas the aptarnavimas
	 * @param kanalas the kanalas
	 * @param adresas the adresas
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

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.NIOServerioSasaja#gaukVisuSujungimuSkaiciu()
	 */
	public long gaukVisuSujungimuSkaiciu() {
		return m_isVisoPrisijungimu;
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.NIOServerioSasaja#gaukVisuAtmestuSujungimuSkaiciu()
	 */
	public long gaukVisuAtmestuSujungimuSkaiciu() {
		return m_isVisoAtmestaPrisijungimu;
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.NIOServerioSasaja#gaukVisuPriimtuSujungimuSkaiciu()
	 */
	public long gaukVisuPriimtuSujungimuSkaiciu() {
		return m_isVisoPriimtaPrisijungimu;
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.NIOServerioSasaja#gaukVisuNepavykusiuSujungimuSkaiciu()
	 */
	public long gaukVisuNepavykusiuSujungimuSkaiciu() {
		return m_isVisoNepavykusiuPrisijungimu;
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.NIOServerioSasaja#stebek(com.menotyou.JC.NIOBiblioteka.ServerioSasajosStebetojas)
	 */
	public void stebek(ServerioSasajosStebetojas stebetojas) {
		if (stebetojas == null) throw new NullPointerException();
		pazymekKadStebetojasPriskirtas();
		gaukNIOAptarnavima().pridekIEile(new StebejimoPradziosIvykis(stebetojas));
	}

	/**
	 * Ispek apie nauja rysi.
	 *
	 * @param sasaja the sasaja
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
	 * Ispek apie nepavkusi sujungima.
	 *
	 * @param isimtis the isimtis
	 */
	private void ispekApieNepavkusiSujungima(IOException isimtis) {
		try {
			if (m_stebetojas != null) m_stebetojas.priemimasNepavyko(isimtis);
		} catch (Exception e) {
			gaukNIOAptarnavima().ispekApieIsimti(e);
		}
	}

	/**
	 * Ispek stebetoja kad sasaja mire.
	 *
	 * @param isimtis the isimtis
	 */
	private void ispekStebetojaKadSasajaMire(Exception isimtis) {
		try {
			if (m_stebetojas != null) m_stebetojas.serverioSasajaMire(isimtis);
		} catch (Exception e) {
			gaukNIOAptarnavima().ispekApieIsimti(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.KanaloValdiklis#paruostasPriemimui()
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

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.KanaloValdiklis#gaukKanala()
	 */
	public ServerSocketChannel gaukKanala() {
		return (ServerSocketChannel) super.gaukKanala();
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.KanaloValdiklis#raktasPriskirtas()
	 */
	void raktasPriskirtas() {
		pridekSusidomejima(SelectionKey.OP_ACCEPT);
	}

	/**
	 * Registruok sasaja.
	 *
	 * @param kanalas the kanalas
	 * @param adresas the adresas
	 * @return the NIO sasaja
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	NIOSasaja registruokSasaja(SocketChannel kanalas, InetSocketAddress adresas) throws IOException {
		return gaukNIOAptarnavima().registruokSasajosKanala(kanalas, adresas);
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.KanaloValdiklis#issijunk(java.lang.Exception)
	 */
	protected void issijunk(Exception e) {
		ispekStebetojaKadSasajaMire(e);
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.NIOServerioSasaja#nustatykPrisijungimuFiltra(com.menotyou.JC.NIOBiblioteka.PrisijungimuFiltras)
	 */
	public void nustatykPrisijungimuFiltra(PrisijungimuFiltras filtras) {
		m_prisijungimuFiltras =filtras == null ? PrisijungimuFiltras.ATMESK_VISUS : filtras;
	}

	/**
	 * The Class StebejimoPradziosIvykis.
	 */
	private class StebejimoPradziosIvykis implements Runnable {
		
		/** The m_naujas stebetojas. */
		private final ServerioSasajosStebetojas m_naujasStebetojas;

		/**
		 * Instantiates a new stebejimo pradzios ivykis.
		 *
		 * @param stebetojas the stebetojas
		 */
		private StebejimoPradziosIvykis(ServerioSasajosStebetojas stebetojas) {
			m_naujasStebetojas = stebetojas;
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			m_stebetojas = m_naujasStebetojas;
			if (!atidarytas()) {
				ispekStebetojaKadSasajaMire(null);
				return;
			}
			pridekSusidomejima(SelectionKey.OP_ACCEPT);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "Pradedama stebeti [" + m_naujasStebetojas + "]";
		}
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.NIOServerioSasaja#gaukSasaja()
	 */
	public ServerSocket gaukSasaja() {
		return gaukKanala().socket();
	}

}
