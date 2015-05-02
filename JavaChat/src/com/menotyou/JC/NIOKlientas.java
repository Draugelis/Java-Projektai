package com.menotyou.JC;

import java.awt.Font;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.codec.DecoderException;

import com.menotyou.JC.NIOBiblioteka.NIOSasaja;
import com.menotyou.JC.NIOBiblioteka.SasajosStebetojas;
import com.menotyou.JC.NIOBiblioteka.EventuValdiklis.EventuValdiklis;
import com.menotyou.JC.NIOBiblioteka.Rasytojai.PaprastasPaketuRasytojas;
import com.menotyou.JC.NIOBiblioteka.Skaitytojai.PaprastasPaketuSkaitytojas;
import com.menotyou.JC.Serveris.VartotojoAutentifikacija;

// TODO: Auto-generated Javadoc
/**
 * The Class NIOKlientas.
 */
public class NIOKlientas implements SasajosStebetojas {
	
	/** The Constant DATOS_FORMA. */
	private final static DateFormat DATOS_FORMA = new SimpleDateFormat(
			"HH:mm:ss");
	
	/** The Constant ANTRASTES_DYDIS. */
	private final static int ANTRASTES_DYDIS = 2;
	
	/** The Constant BIG_ENDIAN. */
	private final static boolean BIG_ENDIAN = true;
	
	/** The m_eventu valdiklis. */
	private final EventuValdiklis m_eventuValdiklis;
	
	/** The m_sasaja. */
	private final NIOSasaja m_sasaja;
	
	/** The autentifikuotas. */
	private Boolean autentifikuotas;
	
	/** The m_vardas. */
	private String m_vardas;
	
	/** The m_slaptazodis. */
	private String m_slaptazodis;
	
	/** The issukis. */
	private String issukis;
	
	/** The m_kliento langas. */
	private KlientoLangas m_klientoLangas;
	
	/** The m_svecio prisijungimas. */
	private SvecioPrisijungimas m_svecioPrisijungimas;
	
	/** The m_kambariai. */
	private final HashMap<String, KambarioInterfeisas> m_kambariai;

	/**
	 * Instantiates a new NIO klientas.
	 *
	 * @param kl the kl
	 * @param sp the sp
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public NIOKlientas(KlientoLangas kl, SvecioPrisijungimas sp)
			throws IOException {
		m_eventuValdiklis = new EventuValdiklis();
		m_sasaja = m_eventuValdiklis.gaukNIOAptarnavima().sukurkSasaja("shared.fln.lt", 8192);
		m_svecioPrisijungimas = sp;
		m_klientoLangas = kl;
		autentifikuotas = false;
		m_kambariai = new HashMap<String, KambarioInterfeisas>();
		m_sasaja.nustatykPaketuRasytoja(new PaprastasPaketuRasytojas(
				ANTRASTES_DYDIS, BIG_ENDIAN));
		m_sasaja.nustatykPaketuSkaitytoja(new PaprastasPaketuSkaitytojas(
				ANTRASTES_DYDIS, BIG_ENDIAN));
		m_sasaja.stebek(this);
	}

	/**
	 * Start.
	 */
	public void start() {
		m_eventuValdiklis.start();
	}

	/**
	 * Nustatyk sriftus.
	 *
	 * @param sriftas the sriftas
	 */
	public void nustatykSriftus(Font sriftas) {
		Collection<KambarioInterfeisas> c = m_kambariai.values();
		Iterator<KambarioInterfeisas> itr = c.iterator();
		while (itr.hasNext())
			itr.next().nustatykIstorijosSrifta(sriftas);
	}

	/**
	 * Gauk varda.
	 *
	 * @return the string
	 */
	public String gaukVarda() {
		return m_vardas;
	}
	
	/**
	 * Jau atidarytas kambarys.
	 *
	 * @param kambarys the kambarys
	 * @return true, if successful
	 */
	public boolean jauAtidarytasKambarys(String kambarys){
		return m_kambariai.containsKey(kambarys);
	}

	/**
	 * Pradek autentifikacija.
	 *
	 * @param vardas the vardas
	 * @param slaptazodis the slaptazodis
	 */
	public void pradekAutentifikacija(String vardas, String slaptazodis) {
		m_vardas = vardas;
		m_slaptazodis = slaptazodis;
		siuskZinute("<R1>" + vardas);
	}

	/**
	 * Siusk zinute.
	 *
	 * @param zinute the zinute
	 */
	public void siuskZinute(String zinute) {
		byte[] zinuteBaitais = zinute.getBytes();
		m_sasaja.rasyk(zinuteBaitais);
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.SasajosStebetojas#rysysUztvirtintas(com.menotyou.JC.NIOBiblioteka.NIOSasaja)
	 */
	public void rysysUztvirtintas(NIOSasaja sasaja) {
		System.out.println("Rysys su serveriu uzmegstas");
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.SasajosStebetojas#rysysNutrauktas(com.menotyou.JC.NIOBiblioteka.NIOSasaja, java.lang.Exception)
	 */
	public void rysysNutrauktas(NIOSasaja sasaja, Exception isimtis) {
		
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.SasajosStebetojas#paketasGautas(com.menotyou.JC.NIOBiblioteka.NIOSasaja, byte[])
	 */
	public void paketasGautas(NIOSasaja sasaja, byte[] paketas) {
		String zinute = new String(paketas);
		System.out.println("Gauta zinute: " + zinute);
		apdorokZinute(zinute);
	}

	/**
	 * Apdorok zinute.
	 *
	 * @param zinute the zinute
	 */
	public void apdorokZinute(String zinute) {
		if (!autentifikuotas) {
			if (zinute.startsWith("<C1>")) {
				issukis = zinute.substring(4);
			} else if (zinute.startsWith("<C2>")) {
				VartotojoAutentifikacija VA = VartotojoAutentifikacija.gaukVAValdikli();
				zinute = zinute.substring(4);
				try {
					String bandymas = VA.UzkoduokSlaptazodi(m_slaptazodis,zinute, "SHA-256", 0);
					String galutinisSlaptazodis = VA.UzkoduokSlaptazodi(bandymas, issukis, "SHA-512");
					m_sasaja.rasyk(("<R2>" + m_vardas + "<P>" + galutinisSlaptazodis).getBytes());
				} catch (NoSuchAlgorithmException e) {
					m_eventuValdiklis.gaukNIOAptarnavima().ispekApieIsimti(e);
				} catch (DecoderException e) {
					m_eventuValdiklis.gaukNIOAptarnavima().ispekApieIsimti(e);
				}
			} else if (zinute.startsWith("<R+>")) {
				m_svecioPrisijungimas.KeistiKrovimoTeksta("Prijungiama...", 62);
				autentifikuotas = true;
				m_sasaja.rasyk("<KS>".getBytes());
				m_sasaja.rasyk("<KP>".getBytes());
			} else if (zinute.startsWith("<ER>")) {
				atsijunk();
				m_svecioPrisijungimas.klaida("Netinkamas vardas arba slaptazodis!");
			} else if(zinute.startsWith("<EP>")){
				m_svecioPrisijungimas.klaida("Vartotojas jau yra prisijungęs!");
			}
		} else {
			if (zinute.startsWith("<K>")) {
				perduokKambariui(zinute.substring(3));
			} else if (zinute.startsWith("<K+>") || zinute.startsWith("<K++>")) {
				System.out.println("Pridedamas naujas kambarys");
				if(zinute.startsWith("<K+>"))
					m_klientoLangas.sukurkKambarioInterfeisa(zinute.substring(4), false);
				else
					m_klientoLangas.sukurkKambarioInterfeisa(zinute.substring(5), true);
				pasalinkKambariuLangus();
			} else if(zinute.startsWith("<KP>")){
				m_svecioPrisijungimas.KeistiKrovimoTeksta("Užbaigiama...", 99);
				m_svecioPrisijungimas.PrisijungimoUzbaigimas(m_vardas);
			} else if (zinute.startsWith("<KS>")) {
				m_klientoLangas.nustatykKambariuSarasa(zinute.substring(4));
			} else if(zinute.startsWith("<EKP>")) {
				m_klientoLangas.gaukPPk().klaida("Jau prisijungta prie šio kambario!");
			} else if(zinute.startsWith("<EKK>")){
				m_klientoLangas.gaukPPk().klaida("Jus negalie sugrįžti į kambarį iš kurio buvote išspirtas!");
			} else if (zinute.startsWith("<EK+>")) {
				m_klientoLangas.gaukPPk().klaida("Toks kambarys nebegzistuoja!");
			} else if (zinute.startsWith("<NEK>")) {
				m_klientoLangas.gaukKK().klaida("Toks kambarys jau egzistuoja!");
			}
		}
	}
	
	/**
	 * Perduok kambariui.
	 *
	 * @param zinute the zinute
	 */
	public void perduokKambariui(String zinute){
		String kambarioPavadinimas = zinute.split("<")[0];
		KambarioInterfeisas kambarys = m_kambariai.get(kambarioPavadinimas);
		if (kambarys != null) {
			zinute = zinute.substring(kambarioPavadinimas.length());
			if (zinute.startsWith("<V>")) {
				zinute = zinute.substring(3);
				String siuntejas = zinute.split("<Z>")[0];
				siuntejas = siuntejas.contentEquals("NULL") ? null: siuntejas;
				zinute = zinute.split("<Z>")[1];
				kambarys.spausdinkZinute(zinute, siuntejas);
			} else if (zinute.startsWith("<I>")) {
				zinute = zinute.substring(3).trim();
				System.out.println("Gauta zinute \'" +  zinute + "\'");
				if(!zinute.isEmpty()) kambarys.spausdintiTeksta(zinute);
			} else if (zinute.startsWith("<V+>")) {
				kambarys.pridekVartotoja(zinute.substring(4));
			} else if (zinute.startsWith("<V->")) {
				kambarys.pasalinkVartotoja(zinute.substring(4), false);
			} else if (zinute.startsWith("<VKK>")) {
				kambarys.pasalinkVartotoja(zinute.substring(5), true);
			} else if (zinute.startsWith("<VS>")) {
				kambarys.nustatykPrisijungusiusVartotojus(zinute.substring(4));
			}
		}
	}
	
	/**
	 * Pasalink kambariu langus.
	 */
	public void pasalinkKambariuLangus(){
		if (m_klientoLangas.gaukKK() != null && m_klientoLangas.gaukKK().isVisible()) {
				m_klientoLangas.gaukKK().pasalink();
		} else if (m_klientoLangas.gaukPPk() != null && m_klientoLangas.gaukPPk().isVisible()){
				m_klientoLangas.gaukPPk().pasalink();
		}
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.SasajosStebetojas#paketasIssiustas(com.menotyou.JC.NIOBiblioteka.NIOSasaja, java.lang.Object)
	 */
	public void paketasIssiustas(NIOSasaja sasaja, Object zyme) {
		System.out.println("Zinute buvo issiusta");
	}

	/**
	 * Atsijunk.
	 */
	public void atsijunk() {
		m_eventuValdiklis.isjunk();
		m_sasaja.uzdaryk();
	}

	/**
	 * Prasyk fokusavimo.
	 *
	 * @param kambarioPav the kambario pav
	 */
	public void prasykFokusavimo(String kambarioPav) {
		m_kambariai.get(kambarioPav).fokusuokZinutesLaukeli();
	}


	/**
	 * Pridek kambari.
	 *
	 * @param pavadinimas the pavadinimas
	 * @param k the k
	 * @return true, if successful
	 */
	public boolean pridekKambari(String pavadinimas, KambarioInterfeisas k) {
		if (m_kambariai.containsKey(pavadinimas))
			return false;
		m_kambariai.put(pavadinimas, k);
		return true;
	}

	/**
	 * Panaikink kambari.
	 *
	 * @param pavadinimas the pavadinimas
	 */
	public void panaikinkKambari(String pavadinimas) {
		m_sasaja.rasyk(("<K->" + pavadinimas).getBytes());
		m_kambariai.remove(pavadinimas);
	}

	/**
	 * Gauk datos forma.
	 *
	 * @return the date format
	 */
	public static DateFormat gaukDatosForma() {
		return DATOS_FORMA;
	}
}
