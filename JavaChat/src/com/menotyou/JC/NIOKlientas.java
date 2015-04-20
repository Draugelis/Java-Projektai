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

public class NIOKlientas implements SasajosStebetojas {
	private final static DateFormat DATOS_FORMA = new SimpleDateFormat(
			"HH:mm:ss");
	private final static int ANTRASTES_DYDIS = 2;
	private final static boolean BIG_ENDIAN = true;
	private final EventuValdiklis m_eventuValdiklis;
	private final NIOSasaja m_sasaja;
	private Boolean autentifikuotas;
	private String m_vardas;
	private String m_slaptazodis;
	private String issukis;
	private KlientoLangas m_klientoLangas;
	private SvecioPrisijungimas m_svecioPrisijungimas;
	private final HashMap<String, KambarioInterfeisas> m_kambariai;

	public NIOKlientas(KlientoLangas kl, SvecioPrisijungimas sp)
			throws IOException {
		m_eventuValdiklis = new EventuValdiklis();
		m_sasaja = m_eventuValdiklis.gaukNIOAptarnavima().sukurkSasaja(
				"shared.fln.lt", 8192);
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

	public void start() {
		m_eventuValdiklis.start();
	}

	public void nustatykSriftus(Font sriftas) {
		Collection<KambarioInterfeisas> c = m_kambariai.values();
		Iterator<KambarioInterfeisas> itr = c.iterator();
		while (itr.hasNext())
			itr.next().nustatykIstorijosSrifta(sriftas);
	}

	public String gaukVarda() {
		return m_vardas;
	}
	public boolean jauAtidarytasKambarys(String kambarys){
		return m_kambariai.containsKey(kambarys);
	}

	public void pradekAutentifikacija(String vardas, String slaptazodis) {
		m_vardas = vardas;
		m_slaptazodis = slaptazodis;
		siuskZinute("<R1>" + vardas);
	}

	public void siuskZinute(String zinute) {
		byte[] zinuteBaitais = zinute.getBytes();
		m_sasaja.rasyk(zinuteBaitais);
	}

	public void rysysUztvirtintas(NIOSasaja sasaja) {
		System.out.println("Rysys su serveriu uzmegstas");
	}

	public void rysysNutrauktas(NIOSasaja sasaja, Exception isimtis) {
		// TODO Auto-generated method stub
	}

	public void paketasGautas(NIOSasaja sasaja, byte[] paketas) {
		String zinute = new String(paketas);
		System.out.println("Gauta zinute: " + zinute);
		apdorokZinute(zinute);
	}

	public void apdorokZinute(String zinute) {
		if (!autentifikuotas) {
			if (zinute.startsWith("<C1>")) {
				issukis = zinute.substring(4);
				System.out.println("Gautas iššūkis: " + issukis);
			} else if (zinute.startsWith("<C2>")) {
				VartotojoAutentifikacija VA = VartotojoAutentifikacija.gaukVAValdikli();
				zinute = zinute.substring(4);
				try {
					System.out.println("Vartotojas gavo savo druska: " + zinute);
					String bandymas = VA.UzkoduokSlaptazodi(m_slaptazodis,zinute, "SHA-256", 0);
					System.out.println("Bandymas: " + bandymas);
					String galutinisSlaptazodis = VA.UzkoduokSlaptazodi(bandymas, issukis, "SHA-512");
					System.out.println("Vartotojas siunčia žinutę: " + "<R2>"+ m_vardas + "<P>" + galutinisSlaptazodis);
					m_sasaja.rasyk(("<R2>" + m_vardas + "<P>" + galutinisSlaptazodis).getBytes());
				} catch (NoSuchAlgorithmException e) {
					m_eventuValdiklis.gaukNIOAptarnavima().ispekApieIsimti(e);
				} catch (DecoderException e) {
					e.printStackTrace();
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
				zinute = zinute.substring(3);
				String kambarioPavadinimas = zinute.split("<")[0];
				KambarioInterfeisas kambarys = m_kambariai.get(kambarioPavadinimas);
				if (kambarys == null) {
					System.out.println("Kambarys pavadinimu "
							+ kambarioPavadinimas + " neegzistuoja");
				} else {
					zinute = zinute.substring(kambarioPavadinimas.length());
					if (zinute.startsWith("<V>")) {
						zinute = zinute.substring(3);
						String siuntejas = zinute.split("<Z>")[0];
						siuntejas = siuntejas.contentEquals("NULL") ? null: siuntejas;
						zinute = zinute.split("<Z>")[1];
						kambarys.spausdinkZinute(zinute, siuntejas);
					} else if (zinute.startsWith("<I>")) {
						kambarys.spausdintiTeksta(zinute.substring(3));
					} else if (zinute.startsWith("<V+>")) {
						kambarys.pridekVartotoja(zinute.substring(4));
					} else if (zinute.startsWith("<V->")) {
						kambarys.pasalinkVartotoja(zinute.substring(4));
					} else if (zinute.startsWith("<VS>")) {
						kambarys.nustatykPrisijungusiusVartotojus(zinute.substring(4));
					}
				}
			} else if (zinute.startsWith("<K+>")) {
				System.out.println("Pridedamas naujas kambarys");
				m_klientoLangas.sukurkKambarioInterfeisa(zinute.substring(4));	
				if (m_klientoLangas.gaukKK() != null && m_klientoLangas.gaukKK().isVisible()) {
						m_klientoLangas.gaukKK().pasalink();
				} else if (m_klientoLangas.gaukPPk() != null && m_klientoLangas.gaukPPk().isVisible()){
						m_klientoLangas.gaukPPk().pasalink();
				}
			} else if(zinute.startsWith("<KP>")){
				m_svecioPrisijungimas.KeistiKrovimoTeksta("Užbaigiama...", 99);
				m_svecioPrisijungimas.PrisijungimoUzbaigimas(m_vardas);
			} else if (zinute.startsWith("<KS>")) {
				m_klientoLangas.nustatykKambariuSarasa(zinute.substring(4));
			} else if(zinute.startsWith("<EKP>")) {
				m_klientoLangas.gaukPPk().klaida("Jau prisijungta prie šio kambario!");
			} else if (zinute.startsWith("<EK+>")) {
				m_klientoLangas.gaukPPk().klaida("Toks kambarys nebegzistuoja!");
			} else if (zinute.startsWith("<NEK>")) {
				m_klientoLangas.gaukKK().klaida("Toks kambarys jau egzistuoja!");
			}
		}
	}

	public void paketasIssiustas(NIOSasaja sasaja, Object zyme) {
		System.out.println("Zinute buvo issiusta");
	}

	public void atsijunk() {
		m_eventuValdiklis.isjunk();
		m_sasaja.uzdaryk();
	}

	public void prasykFokusavimo(String kambarioPav) {
		m_kambariai.get(kambarioPav).fokusuokZinutesLaukeli();
	}


	public boolean pridekKambari(String pavadinimas, KambarioInterfeisas k) {
		if (m_kambariai.containsKey(pavadinimas))
			return false;
		m_kambariai.put(pavadinimas, k);
		return true;
	}

	public void panaikinkKambari(String pavadinimas) {
		m_sasaja.rasyk(("<K->" + pavadinimas).getBytes());
		m_kambariai.remove(pavadinimas);
	}

	public static DateFormat gaukDatosForma() {
		return DATOS_FORMA;
	}
}
