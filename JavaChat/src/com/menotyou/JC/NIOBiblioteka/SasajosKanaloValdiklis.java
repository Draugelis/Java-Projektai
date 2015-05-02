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

// TODO: Auto-generated Javadoc
/**
 * The Class SasajosKanaloValdiklis.
 */
public class SasajosKanaloValdiklis extends KanaloValdiklis implements NIOSasaja {

	/** The m_max eiles ilgis. */
	private int m_maxEilesIlgis;
	
	/** The m_sujungimo laikas. */
	private long m_sujungimoLaikas;
	
	/** The m_baitai eileje. */
	private final AtomicLong m_baitaiEileje;
	
	/** The m_paketu eile. */
	private ConcurrentLinkedQueue<Object> m_paketuEile;
	
	/** The m_paketu skaitytojas. */
	private PaketuSkaitytojas m_paketuSkaitytojas;
	
	/** The m_sasajos stebetojas. */
	private volatile SasajosStebetojas m_sasajosStebetojas;
	
	/** The m_kanalo skaitytojas. */
	private final KanaloSkaitytojas m_kanaloSkaitytojas;
	
	/** The m_kanalo rasytojas. */
	private final KanaloRasytojas m_kanaloRasytojas;

	/**
	 * Instantiates a new sasajos kanalo valdiklis.
	 *
	 * @param aptarnavimas the aptarnavimas
	 * @param kanalas the kanalas
	 * @param adresas the adresas
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

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.NIOSasaja#rasyk(byte[])
	 */
	public boolean rasyk(byte[] paketas) {
		return rasyk(paketas, null);
	}

	/**
	 * Sujungtas.
	 *
	 * @return true, if successful
	 */
	public boolean sujungtas() {
		return gaukKanala().isConnected();
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.NIOSasaja#rasyk(byte[], java.lang.Object)
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

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.NIOSasaja#pridekIEile(java.lang.Runnable)
	 */
	public void pridekIEile(Runnable r) {
		m_paketuEile.offer(r);
		gaukNIOAptarnavima().pridekIEile(new SusidomejimoPridejimas(SelectionKey.OP_WRITE));
	}

	/**
	 * Ispek apie gauta paketa.
	 *
	 * @param paketas the paketas
	 */
	private void ispekApieGautaPaketa(byte[] paketas) {
		try {
			if (m_sasajosStebetojas != null) m_sasajosStebetojas.paketasGautas(this, paketas);
		} catch (Exception e) {
			gaukNIOAptarnavima().ispekApieIsimti(e);
		}
	}

	/**
	 * Ispek apie isiusta paketa.
	 *
	 * @param zyme the zyme
	 */
	private void ispekApieIsiustaPaketa(Object zyme) {
		try {
			if (m_sasajosStebetojas != null) m_sasajosStebetojas.paketasIssiustas(this, zyme);
		} catch (Exception e) {
			gaukNIOAptarnavima().ispekApieIsimti(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.KanaloValdiklis#paruostasSkaitymui()
	 */
	void paruostasSkaitymui() {
		if (!atidarytas()) return;
		//System.out.println("Pranesta kad galima skaityti iš " + gaukAdresa());
		try {
			if (!sujungtas()) throw new IOException("Kanalas n�ra sujungtas");
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
	 * Uzpildyk siuntimu buferi.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
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

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.KanaloValdiklis#paruostasRasymui()
	 */
	void paruostasRasymui() {
		try {
		//	System.out.println("Pranesta kad galima rašyti " + gaukAdresa());
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


	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.KanaloValdiklis#paruostasSujungimui()
	 */
	void paruostasSujungimui() {
		//System.out.println("Pranesta kad galima baigti sujungi su" + gaukAdresa());
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
	
	/**
	 * Ispek kad buvo atsauktas.
	 */
	public void ispekKadBuvoAtsauktas(){
		uzdaryk();
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.NIOSasaja#gaukNuskaitytuBaituSkaiciu()
	 */
	public long gaukNuskaitytuBaituSkaiciu() {
		return m_kanaloSkaitytojas.gaukNuskaitytusBitus();
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.KanaloValdiklis#gaukKanala()
	 */
	public SocketChannel gaukKanala() {
		return (SocketChannel) super.gaukKanala();
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.NIOSasaja#gaukParasytuBaituSkaiciu()
	 */
	public long gaukParasytuBaituSkaiciu() {
		return m_kanaloRasytojas.gaukKiekParasytaBaitu();
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.NIOSasaja#gaukLaikaNuoSujungimo()
	 */
	public long gaukLaikaNuoSujungimo() {
		return m_sujungimoLaikas > 0 ? System.currentTimeMillis() - m_sujungimoLaikas : -1;
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.NIOSasaja#gaukRasymoEilesDydi()
	 */
	public long gaukRasymoEilesDydi() {
		return m_baitaiEileje.get();
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.KanaloValdiklis#toString()
	 */
	public String toString() {
		try {
			return gaukSasaja().toString();
		} catch (Exception e) {
			return "Uzdaryta NIO S�saja";
		}
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.NIOSasaja#gaukMaxEilesIlgi()
	 */
	public int gaukMaxEilesIlgi() {
		return m_maxEilesIlgis;
	}

	/**
	 * Ispek stebetoja del sujungimo.
	 */
	private void ispekStebetojaDelSujungimo() {
		try {
			if (m_sasajosStebetojas != null) m_sasajosStebetojas.rysysUztvirtintas(this);
		} catch (Exception e) {
			gaukNIOAptarnavima().ispekApieIsimti(e);
		}
	}

	/**
	 * Ispek stebetoja del atsijungimo.
	 *
	 * @param isimtis the isimtis
	 */
	private void ispekStebetojaDelAtsijungimo(Exception isimtis) {
		try {
			if (m_sasajosStebetojas != null) m_sasajosStebetojas.rysysNutrauktas(this, isimtis);
		} catch (Exception e) {
			gaukNIOAptarnavima().ispekApieIsimti(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.NIOSasaja#nustatykMaxEilesIlgi(int)
	 */
	public void nustatykMaxEilesIlgi(int maxEilesIlgis) {
		m_maxEilesIlgis = maxEilesIlgis;
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.NIOSasaja#nustatykPaketuSkaitytoja(com.menotyou.JC.NIOBiblioteka.Skaitytojai.PaketuSkaitytojas)
	 */
	public void nustatykPaketuSkaitytoja(PaketuSkaitytojas paketuSkaitytojas) {
		m_paketuSkaitytojas = paketuSkaitytojas;
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.NIOSasaja#nustatykPaketuRasytoja(com.menotyou.JC.NIOBiblioteka.Rasytojai.PaketuRasytojas)
	 */
	public void nustatykPaketuRasytoja(final PaketuRasytojas paketuRasytojas) {
		if (paketuRasytojas == null) throw new NullPointerException();
		pridekIEile(new Runnable() {
			public void run() {
				m_kanaloRasytojas.nustatykPaketuRasytoja(paketuRasytojas);
			}
		});
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.NIOSasaja#stebek(com.menotyou.JC.NIOBiblioteka.SasajosStebetojas)
	 */
	public void stebek(SasajosStebetojas stebetojas) {
	//	System.out.println("Pradedama stebeti.");
		pazymekKadStebetojasPriskirtas();
	//	System.out.println("Pridedama i eile StebejimoPradziosIvykis");
	//	System.out.println("Stebetojas == null: " + stebetojas == null);
		gaukNIOAptarnavima().pridekIEile(new StebejimoPradziosIvykis(this, stebetojas == null ? SasajosStebetojas.NULL : stebetojas));
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.NIOSasaja#uzsidarykPoRasymo()
	 */
	public void uzsidarykPoRasymo() {
		pridekIEile(new Runnable() {
			public void run() {
				m_paketuEile.clear();
				issijunk(null);
			}
		});
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.NIOSasaja#gaukSasaja()
	 */
	public Socket gaukSasaja() {
		return gaukKanala().socket();
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.KanaloValdiklis#raktasPriskirtas()
	 */
	void raktasPriskirtas() {
		if (!sujungtas()) {
			pridekSusidomejima(SelectionKey.OP_CONNECT);
		}
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.KanaloValdiklis#issijunk(java.lang.Exception)
	 */
	protected void issijunk(Exception e) {
		m_sujungimoLaikas = -1;
		m_paketuEile.clear();
		m_baitaiEileje.set(0);
		ispekStebetojaDelAtsijungimo(e);
	}

	/**
	 * The Class SusidomejimoPridejimas.
	 */
	private class SusidomejimoPridejimas implements Runnable {
		
		/** The m_susidomejimas. */
		private final int m_susidomejimas;

		/**
		 * Instantiates a new susidomejimo pridejimas.
		 *
		 * @param susidomejimas the susidomejimas
		 */
		private SusidomejimoPridejimas(int susidomejimas) {
			m_susidomejimas = susidomejimas;
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			pridekSusidomejima(m_susidomejimas);
		}
	}

	/**
	 * The Class StebejimoPradziosIvykis.
	 */
	private class StebejimoPradziosIvykis implements Runnable {
		
		/** The m_naujas stebetojas. */
		private final SasajosStebetojas m_naujasStebetojas;
		
		/** The m_valdiklis. */
		private final SasajosKanaloValdiklis m_valdiklis;

		/**
		 * Instantiates a new stebejimo pradzios ivykis.
		 *
		 * @param valdiklis the valdiklis
		 * @param stebetojas the stebetojas
		 */
		private StebejimoPradziosIvykis(SasajosKanaloValdiklis valdiklis, SasajosStebetojas stebetojas) {
			m_valdiklis = valdiklis;
			m_naujasStebetojas = stebetojas;
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
	//		System.out.println("Pradedamas stebeti klientas " + m_valdiklis.gaukAdresa());
			m_valdiklis.m_sasajosStebetojas = m_naujasStebetojas;
			if (m_valdiklis.sujungtas()) {
	//			System.out.println("Rysys sujungtas!");
				m_valdiklis.ispekStebetojaDelSujungimo();
			}
			if (!m_valdiklis.atidarytas()) {
	//			System.out.println("Valdiklis neatidarytas - uždaroma...");
				m_valdiklis.ispekStebetojaDelAtsijungimo(null);
			}
	//		System.out.println("Pridedamas OP_READ susidomejimas.");
			m_valdiklis.pridekSusidomejima(SelectionKey.OP_READ);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "Pradedama stebeti [" + m_naujasStebetojas + "]";
		}
	}

}
