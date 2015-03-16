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

public class SasajosKanaloValdiklis extends KanaloValdiklis implements NIOSasaja {

	private int m_maxEilesIlgis;
	private long m_sujungimoLaikas;
	private final AtomicLong m_baitaiEileje;
	private ConcurrentLinkedQueue<Object> m_paketuEile;
	private PaketuSkaitytojas m_paketuSkaitytojas;
	private volatile SasajosStebetojas m_sasajosStebetojas;
	private final KanaloSkaitytojas m_kanaloSkaitytojas;
	private final KanaloRasytojas m_kanaloRasytojas;

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

	public void pridekIEile(Runnable r) {
		m_paketuEile.offer(r);
		gaukNIOAptarnavima().pridekIEile(new SusidomejimoPridejimas(SelectionKey.OP_WRITE));
	}

	private void ispekApieGautaPaketa(byte[] paketas) {
		try {
			if (m_sasajosStebetojas != null) m_sasajosStebetojas.paketasGautas(this, paketas);
		} catch (Exception e) {
			gaukNIOAptarnavima().ispekApieIsimti(e);
		}
	}

	private void ispekApieIsiustaPaketa(Object zyme) {
		try {
			if (m_sasajosStebetojas != null) m_sasajosStebetojas.paketasIssiustas(this, zyme);
		} catch (Exception e) {
			gaukNIOAptarnavima().ispekApieIsimti(e);
		}
	}

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
	public void ispekKadBuvoAtsauktas(){
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
			return "Uzdaryta NIO S�saja";
		}
	}

	public int gaukMaxEilesIlgi() {
		return m_maxEilesIlgis;
	}

	private void ispekStebetojaDelSujungimo() {
		try {
			if (m_sasajosStebetojas != null) m_sasajosStebetojas.rysysUztvirtintas(this);
		} catch (Exception e) {
			gaukNIOAptarnavima().ispekApieIsimti(e);
		}
	}

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

	public void stebek(SasajosStebetojas stebetojas) {
	//	System.out.println("Pradedama stebeti.");
		pazymekKadStebetojasPriskirtas();
	//	System.out.println("Pridedama i eile StebejimoPradziosIvykis");
	//	System.out.println("Stebetojas == null: " + stebetojas == null);
		gaukNIOAptarnavima().pridekIEile(new StebejimoPradziosIvykis(this, stebetojas == null ? SasajosStebetojas.NULL : stebetojas));
	}

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

	protected void issijunk(Exception e) {
		m_sujungimoLaikas = -1;
		m_paketuEile.clear();
		m_baitaiEileje.set(0);
		ispekStebetojaDelAtsijungimo(e);
	}

	private class SusidomejimoPridejimas implements Runnable {
		private final int m_susidomejimas;

		private SusidomejimoPridejimas(int susidomejimas) {
			m_susidomejimas = susidomejimas;
		}

		public void run() {
			pridekSusidomejima(m_susidomejimas);
		}
	}

	private class StebejimoPradziosIvykis implements Runnable {
		private final SasajosStebetojas m_naujasStebetojas;
		private final SasajosKanaloValdiklis m_valdiklis;

		private StebejimoPradziosIvykis(SasajosKanaloValdiklis valdiklis, SasajosStebetojas stebetojas) {
			m_valdiklis = valdiklis;
			m_naujasStebetojas = stebetojas;
		}

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

		public String toString() {
			return "Pradedama stebeti [" + m_naujasStebetojas + "]";
		}
	}

}
