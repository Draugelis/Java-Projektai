package com.menotyou.JC.Serveris;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import com.menotyou.JC.NIOBiblioteka.NIOSasaja;
import com.menotyou.JC.NIOBiblioteka.SasajosStebetojas;
import com.menotyou.JC.NIOBiblioteka.EventuValdiklis.UzdelstasIvykis;
import com.menotyou.JC.NIOBiblioteka.Rasytojai.PaprastasPaketuRasytojas;
import com.menotyou.JC.NIOBiblioteka.Skaitytojai.PaprastasPaketuSkaitytojas;

public class Vartotojas implements SasajosStebetojas {
	private final static long PRISIJUNGIMO_LAIKAS = 5 * 1000;
	private final static long MAX_NEVEIKSNUMO_LAIKAS = 20 * 60 * 1000;
	private final static int ANTRASTES_DYDIS = 2;
	private final static boolean BIG_ENDIAN = true;
	private final JCServeris m_serveris;
	private final NIOSasaja m_sasaja;
	private String m_vardas;
	private String m_slaptazodis;
	private String m_druska;
	private String m_issukis;
	private int m_ID;
	private UzdelstasIvykis m_atsijungimoIvykis;

	public Vartotojas(JCServeris serveris, NIOSasaja sasaja) {
		m_serveris = serveris;
		m_sasaja = sasaja;
		m_sasaja.nustatykPaketuRasytoja(new PaprastasPaketuRasytojas(ANTRASTES_DYDIS, BIG_ENDIAN));
		m_sasaja.nustatykPaketuSkaitytoja(new PaprastasPaketuSkaitytojas(ANTRASTES_DYDIS, BIG_ENDIAN));
		m_sasaja.stebek(this);
		m_vardas = null;
		m_ID = -1;
	}

	public void rysysUztvirtintas(NIOSasaja sasaja) {
		m_atsijungimoIvykis = m_serveris.gaukEventuValdikli().vykdytiVeliau(new Runnable() {
			public void run() {
				m_sasaja.rasyk("Prisijungimo laikas baigėsi!".getBytes());
				m_sasaja.uzsidarykPoRasymo();
			}
		}, PRISIJUNGIMO_LAIKAS);
		m_sasaja.rasyk(generuokIssuki());
		System.out.println("Iššūkis išsiųstas");
	}

	public String toString() {
		return m_vardas != null ? m_vardas + "@" + m_sasaja.gaukIp() : "Anonimas@" + m_sasaja.gaukIp();
	}

	public String gaukVarda() {
		return m_vardas;
	}

	public void rysysNutrauktas(NIOSasaja sasaja, Exception isimtis) {
		sasaja.uzdaryk();
		m_serveris.pasalinkKlienta(this);

	}

	public void nustatykAuthDuomenis(String slaptazodis, String druska) {
		if (slaptazodis == null && druska == null) {
			m_sasaja.rasyk("<ER>".getBytes());
			m_sasaja.uzsidarykPoRasymo();
			m_serveris.pasalinkKlienta(this);
		} else {
			m_slaptazodis = slaptazodis;
			m_druska = druska;
			m_sasaja.rasyk(("<C2>" + m_druska).getBytes());
		}
	}

	private void paruoskNeveiksnumoIvyki() {
		if (m_atsijungimoIvykis != null) m_atsijungimoIvykis.atsaukti();
		m_atsijungimoIvykis = m_serveris.gaukEventuValdikli().vykdytiVeliau(new Runnable() {
			public void run() {
				m_sasaja.rasyk("<S>Atjungta dėl neveiksnumo.".getBytes());
				m_sasaja.uzsidarykPoRasymo();
			}
		}, MAX_NEVEIKSNUMO_LAIKAS);
	}

	public void paketasGautas(NIOSasaja sasaja, byte[] paketas) {
		System.out.println("Gautas paketas.");
		String zinute = new String(paketas).trim();
		if (zinute.length() == 0) return;
		System.out.println(zinute);
		paruoskNeveiksnumoIvyki();
		apdorokZinute(zinute);
	}

	public void apdorokZinute(String zinute) {
		if (m_vardas == null) {
			if (zinute.startsWith("<R1>")) {
				zinute = zinute.substring(4);
				if(!m_serveris.jauPrisijunges(zinute)) m_serveris.gaukDuomenis(this, zinute);
				else m_sasaja.rasyk("<EP>".getBytes());
			} else if (zinute.startsWith("<R2>")) {
				zinute = zinute.substring(4);
				String vardas = zinute.split("<P>")[0];
				String bandymas = zinute.substring(vardas.length() + 3);
				if(bandymas.isEmpty()) bandymas = "AAAAAAAAAAAAAAAAAAAAAA";
				try {
					String tikrasis = VartotojoAutentifikacija.gaukVAValdikli().UzkoduokSlaptazodi(m_slaptazodis, m_issukis, "SHA-512");
					System.out.println("Tikrinamas vartotjas " + vardas );
					System.out.println("Jis siūlo slaptazodį: " + bandymas);
					System.out.println("Tikrasis slaptazodis: " + tikrasis);
					if(tikrasis.equals(bandymas)){
						System.out.println("Vartotjas patvirtintas..");
						m_vardas = vardas;
						m_sasaja.rasyk("<R+>".getBytes());
					} else {
						System.out.println("Vartotojo autentifikacija nepavyko, vartotojas šalinamas..");
						m_sasaja.rasyk("<ER>".getBytes());
						m_sasaja.uzsidarykPoRasymo();
						m_serveris.pasalinkKlienta(this);
					}
				} catch (NoSuchAlgorithmException e) {
					m_serveris.gaukEventuValdikli().gaukNIOAptarnavima().ispekApieIsimti(e);
				} catch (DecoderException e) {
					m_serveris.gaukEventuValdikli().gaukNIOAptarnavima().ispekApieIsimti(e);
				}

			}
		} else {
			if (zinute.startsWith("<K>")) {
				zinute = zinute.substring(3);
				m_serveris.perduokKambariui(this, zinute);
			} else if(zinute.startsWith("<KS>")){
				m_serveris.siuskKambariuSarasa(Vartotojas.this);
			} else if (zinute.startsWith("<NK>")) {
				zinute = zinute.substring(4);
				String k_pavadinimas;
				Boolean suZinute = false;
				if (zinute.contains("<KZ>")) {
					k_pavadinimas = zinute.split("<KZ>")[0];
					suZinute = true;
				} else {
					k_pavadinimas = zinute;
				}
				if (m_serveris.arYraKambarys(k_pavadinimas)) {
					m_sasaja.rasyk("<ENK>".getBytes());
				} else {
					if (suZinute) {
						m_serveris.pridekKambari(k_pavadinimas, zinute.split("<KZ>")[1], Vartotojas.this);
					} else {
						m_serveris.pridekKambari(k_pavadinimas, null, Vartotojas.this);
					}
				}
			} else if (zinute.startsWith("<K+>")) {
				Kambarys kambarys = m_serveris.gaukKambari(zinute.substring(4));
				if (kambarys == null) {
					System.out.println("Operacija: <K+> Klaida: Kambario pavadinimu " + zinute.substring(4) + " nera");
					m_sasaja.rasyk("<EK+>".getBytes());
					return;
				}
				kambarys.pridekKlienta(this);
			} else if (zinute.startsWith("<K->")) {
				System.out.println("Bandoma šalinti klientą");
				Kambarys kambarys = m_serveris.gaukKambari(zinute.substring(4));
				if (kambarys == null) System.out.println("Operacija: <K-> Klaida: Kambario pavadinimu " + zinute.substring(4) + " nera");
				else kambarys.pasalinkKlienta(this);
			} else if(zinute.startsWith("<KP>")){
				Kambarys kambarys = m_serveris.gaukKambari("Pagrindinis");
				kambarys.pridekKlienta(this);
			} else if (zinute.startsWith("<Q>")) {
				m_serveris.pasalinkKlienta(this);
			}
		}
	}

	private byte[] generuokIssuki() {
		final Random r = new SecureRandom();
		byte[] salt = new byte[22];
		r.nextBytes(salt);
		m_issukis = Hex.encodeHexString(salt);
		return ("<C1>" + m_issukis).getBytes();
	}

	public void paketasIssiustas(NIOSasaja sasaja, Object zyme) {

	}

	public void siuskZinute(Kambarys kambarys, String zinute) {
		zinute = "<K>" + kambarys.gaukPavadinima() + zinute;
		siuskZinute(zinute);
	}

	public void siuskZinute(String zinute) {
		siuskZinute(zinute.getBytes());
	}

	public void siuskZinute(byte[] zinuteBaitais) {
		if (m_vardas != null) {
			m_sasaja.rasyk(zinuteBaitais);
		}
	}

}
