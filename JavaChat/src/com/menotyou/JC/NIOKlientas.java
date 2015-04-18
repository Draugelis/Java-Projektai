package com.menotyou.JC;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import com.menotyou.JC.NIOBiblioteka.NIOSasaja;
import com.menotyou.JC.NIOBiblioteka.SasajosStebetojas;
import com.menotyou.JC.NIOBiblioteka.EventuValdiklis.EventuValdiklis;
import com.menotyou.JC.NIOBiblioteka.Rasytojai.PaprastasPaketuRasytojas;
import com.menotyou.JC.NIOBiblioteka.Skaitytojai.PaprastasPaketuSkaitytojas;
import com.menotyou.JC.Serveris.VartotojoAutentifikacija;

public class NIOKlientas implements SasajosStebetojas {
	private final static DateFormat DATOS_FORMA = new SimpleDateFormat("HH:mm:ss");
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
	private final HashMap<String, KambarioInterfeisas> kambariai;

	public NIOKlientas(KlientoLangas kl, SvecioPrisijungimas sp) throws IOException {
		m_eventuValdiklis = new EventuValdiklis();
		m_sasaja = m_eventuValdiklis.gaukNIOAptarnavima().sukurkSasaja("shared.fln.lt", 8192);
		m_svecioPrisijungimas = sp;
		m_klientoLangas = kl;
		autentifikuotas = false;
		kambariai = new HashMap<String, KambarioInterfeisas>();
		m_sasaja.nustatykPaketuRasytoja(new PaprastasPaketuRasytojas(ANTRASTES_DYDIS, BIG_ENDIAN));
		m_sasaja.nustatykPaketuSkaitytoja(new PaprastasPaketuSkaitytojas(ANTRASTES_DYDIS, BIG_ENDIAN));
		m_sasaja.stebek(this);
	}

	public void start() {
		m_eventuValdiklis.start();
	}

	public String gaukVarda() {
		return m_vardas;
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
		System.out.println("Gauta zinute: " + zinute + " Vardas = " + m_vardas);
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
					String bandymas = VA.UzkoduokSlaptazodi(m_slaptazodis, zinute, "SHA-256", 0);
					System.out.println("Bandymas: " + bandymas);
					String galutinisSlaptazodis = VA.UzkoduokSlaptazodi(bandymas, issukis, "SHA-512");
					System.out.println("Vartotojas siunčia žinutę: " + "<R2>" + m_vardas + "<P>" + galutinisSlaptazodis);
					m_sasaja.rasyk(("<R2>" + m_vardas + "<P>" + galutinisSlaptazodis).getBytes());
				} catch (NoSuchAlgorithmException e) {
					m_eventuValdiklis.gaukNIOAptarnavima().ispekApieIsimti(e);
				} catch (DecoderException e) {
					e.printStackTrace();
				}
			} else if (zinute.startsWith("<R+>")) {
				m_vardas = zinute.substring(4);
				m_sasaja.rasyk("<K+>Pagrindinis".getBytes());
				m_svecioPrisijungimas.KeistiKrovimoTeksta("Prijungiama...", 62);
				autentifikuotas = true;
			} else if (zinute.startsWith("<ER>")) {
				atsijunk();
				m_svecioPrisijungimas.Klaida("Netinkamas vardas arba slaptazodis");
			}
		} else {
			if (zinute.startsWith("<K>")) {
				zinute = zinute.substring(3);
				String kambarioPavadinimas = zinute.split("<")[0];
				KambarioInterfeisas kambarys = kambariai.get(kambarioPavadinimas);
				if (kambarys == null) {
					System.out.println("Kambarys pavadinimu " + kambarioPavadinimas + " neegzistuoja");
				} else {
					zinute = zinute.substring(kambarioPavadinimas.length());
					if (zinute.startsWith("<V>")) {
						zinute = zinute.substring(3);
						String siuntejas = zinute.split("<Z>")[0];
						siuntejas = siuntejas.contentEquals("NULL") ? null : siuntejas;
						zinute = zinute.split("<Z>")[1];
						kambarys.spausdinkZinute(zinute, siuntejas);
					} else if (zinute.startsWith("<I>")) {
						kambarys.spausdintiTeksta(zinute.substring(3));
					} else if (zinute.startsWith("<V+>")) {
						kambarys.pridekVartotoja(zinute.substring(4));
					} else if (zinute.startsWith("<V->")) {
						kambarys.pasalinkVartotoja(zinute.substring(4));
					}
				}
			} else if (zinute.startsWith("<K+>")) {
				System.out.println("Pridedamas naujas kambarys");
				if (zinute.substring(4).contentEquals("Pagrindinis")) {
					m_svecioPrisijungimas.KeistiKrovimoTeksta("Užbaigiama...", 99);
					m_svecioPrisijungimas.PrisijungimoUzbaigimas(m_vardas);

				} else {
					m_klientoLangas.sukurkKambarioInterfeisa(zinute.substring(4));
				}
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

	public boolean pridekKambari(String pavadinimas, KambarioInterfeisas k) {
		if (kambariai.containsKey(pavadinimas)) return false;
		kambariai.put(pavadinimas, k);
		return true;
	}

	public void panaikinkKambari(String pavadinimas) {
		m_sasaja.rasyk(("<K->" + pavadinimas).getBytes());
		kambariai.remove(pavadinimas);
	}
}
