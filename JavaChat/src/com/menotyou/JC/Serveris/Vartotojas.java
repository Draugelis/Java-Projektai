package com.menotyou.JC.Serveris;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Random;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import com.menotyou.JC.NIOBiblioteka.NIOSasaja;
import com.menotyou.JC.NIOBiblioteka.SasajosStebetojas;
import com.menotyou.JC.NIOBiblioteka.EventuValdiklis.UzdelstasIvykis;
import com.menotyou.JC.NIOBiblioteka.Rasytojai.PaprastasPaketuRasytojas;
import com.menotyou.JC.NIOBiblioteka.Skaitytojai.PaprastasPaketuSkaitytojas;

// TODO: Auto-generated Javadoc
/**
 * The Class Vartotojas.
 */
public class Vartotojas implements SasajosStebetojas {
	
	/** The Constant PRISIJUNGIMO_LAIKAS. */
	private final static long PRISIJUNGIMO_LAIKAS = 5 * 1000;
	
	/** The Constant MAX_NEVEIKSNUMO_LAIKAS. */
	private final static long MAX_NEVEIKSNUMO_LAIKAS = 20 * 60 * 1000;
	
	/** The Constant ANTRASTES_DYDIS. */
	private final static int ANTRASTES_DYDIS = 2;
	
	/** The Constant BIG_ENDIAN. */
	private final static boolean BIG_ENDIAN = true;
	
	/** The m_serveris. */
	private final JCServeris m_serveris;
	
	/** The m_sasaja. */
	private final NIOSasaja m_sasaja;
	
	/** The m_vardas. */
	private String m_vardas;
	
	/** The m_slaptazodis. */
	private String m_slaptazodis;
	
	/** The m_druska. */
	private String m_druska;
	
	/** The m_issukis. */
	private String m_issukis;
	
	/** The m_ id. */
	private int m_ID;
	
	/** The m_atsijungimo ivykis. */
	private UzdelstasIvykis m_atsijungimoIvykis;
	
	/** The m_prisijungimo laikas. */
	private Timestamp m_prisijungimoLaikas;
	
	/** The m_issiunte zinuciu. */
	private int m_issiunteZinuciu = 0;
	
	/** The m_prisijunge prie kambariu. */
	private int m_prisijungePrieKambariu = 0;
	
	/** The m_buvo ispirtas kartu. */
	private int m_buvoIspirtasKartu = 0;
	
	/** The m_ispyre kartu. */
	private int m_ispyreKartu = 0;

	/**
	 * Instantiates a new vartotojas.
	 *
	 * @param serveris the serveris
	 * @param sasaja the sasaja
	 */
	public Vartotojas(JCServeris serveris, NIOSasaja sasaja) {
		m_serveris = serveris;
		m_sasaja = sasaja;
		m_sasaja.nustatykPaketuRasytoja(new PaprastasPaketuRasytojas(ANTRASTES_DYDIS, BIG_ENDIAN));
		m_sasaja.nustatykPaketuSkaitytoja(new PaprastasPaketuSkaitytojas(ANTRASTES_DYDIS, BIG_ENDIAN));
		m_sasaja.stebek(this);
		m_vardas = null;
		m_ID = -1;
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.SasajosStebetojas#rysysUztvirtintas(com.menotyou.JC.NIOBiblioteka.NIOSasaja)
	 */
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return m_vardas != null ? m_vardas + "@" + m_sasaja.gaukIp() : "Anonimas@" + m_sasaja.gaukIp();
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
	 * Gauk id.
	 *
	 * @return the int
	 */
	public int gaukID() {
		return m_ID;
	}

	/**
	 * Gauk prisijungimo laika.
	 *
	 * @return the java.sql. timestamp
	 */
	public java.sql.Timestamp gaukPrisijungimoLaika() {
		return m_prisijungimoLaikas;
	}

	/**
	 * Gauk zinuciu sk.
	 *
	 * @return the int
	 */
	public int gaukZinuciuSK() {
		return m_issiunteZinuciu;
	}

	/**
	 * Gauk kambariu sk.
	 *
	 * @return the int
	 */
	public int gaukKambariuSK() {
		return m_prisijungePrieKambariu;
	}

	/**
	 * Gauk kiek kartu ispirtas.
	 *
	 * @return the int
	 */
	public int gaukKiekKartuIspirtas() {
		return m_buvoIspirtasKartu;
	}

	/**
	 * Gauk kiek ispyre.
	 *
	 * @return the int
	 */
	public int gaukKiekIspyre() {
		return m_ispyreKartu;
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.SasajosStebetojas#rysysNutrauktas(com.menotyou.JC.NIOBiblioteka.NIOSasaja, java.lang.Exception)
	 */
	public void rysysNutrauktas(NIOSasaja sasaja, Exception isimtis) {
		sasaja.uzdaryk();
		m_serveris.pasalinkKlienta(this);

	}

	/**
	 * Nustatyk auth duomenis.
	 *
	 * @param slaptazodis the slaptazodis
	 * @param druska the druska
	 * @param id the id
	 */
	public void nustatykAuthDuomenis(String slaptazodis, String druska, int id) {
		if (slaptazodis == null && druska == null && id == -1) {
			m_sasaja.rasyk("<ER>".getBytes());
			m_sasaja.uzsidarykPoRasymo();
			m_serveris.pasalinkKlienta(this);
		} else {
			m_slaptazodis = slaptazodis;
			m_druska = druska;
			m_ID = id;
			m_sasaja.rasyk(("<C2>" + m_druska).getBytes());
		}
	}

	/**
	 * Paruosk neveiksnumo ivyki.
	 */
	private void paruoskNeveiksnumoIvyki() {
		if (m_atsijungimoIvykis != null) m_atsijungimoIvykis.atsaukti();
		m_atsijungimoIvykis = m_serveris.gaukEventuValdikli().vykdytiVeliau(new Runnable() {
			public void run() {
				m_sasaja.rasyk("<S>Atjungta dėl neveiksnumo.".getBytes());
				m_sasaja.uzsidarykPoRasymo();
			}
		}, MAX_NEVEIKSNUMO_LAIKAS);
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.SasajosStebetojas#paketasGautas(com.menotyou.JC.NIOBiblioteka.NIOSasaja, byte[])
	 */
	public void paketasGautas(NIOSasaja sasaja, byte[] paketas) {
		System.out.println("Gautas paketas.");
		String zinute = new String(paketas).trim();
		if (zinute.length() == 0) return;
		System.out.println(zinute);
		paruoskNeveiksnumoIvyki();
		apdorokZinute(zinute);
	}

	/**
	 * Apdorok zinute.
	 *
	 * @param zinute the zinute
	 */
	public void apdorokZinute(String zinute) {
		if (m_vardas == null) {
			if (zinute.startsWith("<R1>")) {
				zinute = zinute.substring(4);
				if (!m_serveris.jauPrisijunges(zinute)) m_serveris.gaukDuomenis(this, zinute);
				else
					m_sasaja.rasyk("<EP>".getBytes());
			} else if (zinute.startsWith("<R2>")) {
				zinute = zinute.substring(4);
				String vardas = zinute.split("<P>")[0];
				String bandymas = zinute.substring(vardas.length() + 3);
				if (bandymas.isEmpty()) bandymas = "AAAAAAAAAAAAAAAAAAAAAA";
				try {
					String tikrasis = VartotojoAutentifikacija.gaukVAValdikli().UzkoduokSlaptazodi(m_slaptazodis, m_issukis, "SHA-512");
					if (tikrasis.equals(bandymas)) {
						m_vardas = vardas;
						m_sasaja.rasyk("<R+>".getBytes());
						m_prisijungimoLaikas = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
						m_serveris.atnaujinkInfo();
					} else {
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
			} else if (zinute.startsWith("<KS>")) {
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
						m_serveris.pridekKambari(k_pavadinimas, "", Vartotojas.this);
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
				Kambarys kambarys = m_serveris.gaukKambari(zinute.substring(4));
				if (kambarys == null) System.out.println("Operacija: <K-> Klaida: Kambario pavadinimu " + zinute.substring(4) + " nera");
				else
					kambarys.pasalinkKlienta(this);
			} else if (zinute.startsWith("<KK>")) {
				String k_pav = zinute.substring(4).split("<V>")[0];
				Kambarys kambarys = m_serveris.gaukKambari(k_pav);
				if (kambarys == null) System.out.println("Operacija: <KK> Klaida: Kambario pavadinimu " + k_pav + " nera");
				else
					kambarys.isspirkKlienta(zinute.split("<V>")[1], this);
			} else if (zinute.startsWith("<KP>")) {
				Kambarys kambarys = m_serveris.gaukKambari("Pagrindinis");
				if (kambarys == null) m_sasaja.rasyk("<EK+>".getBytes());
				else {
					kambarys.pridekKlienta(this);
					m_sasaja.rasyk("<KP>".getBytes());
				}
			} else if (zinute.startsWith("<Q>")) {
				m_serveris.pasalinkKlienta(this);
			}
		}
	}

	/**
	 * Generuok issuki.
	 *
	 * @return the byte[]
	 */
	private byte[] generuokIssuki() {
		final Random r = new SecureRandom();
		byte[] salt = new byte[22];
		r.nextBytes(salt);
		m_issukis = Hex.encodeHexString(salt);
		return ("<C1>" + m_issukis).getBytes();
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.SasajosStebetojas#paketasIssiustas(com.menotyou.JC.NIOBiblioteka.NIOSasaja, java.lang.Object)
	 */
	public void paketasIssiustas(NIOSasaja sasaja, Object zyme) {

	}

	/**
	 * Siusk zinute.
	 *
	 * @param kambarys the kambarys
	 * @param zinute the zinute
	 */
	public void siuskZinute(Kambarys kambarys, String zinute) {
		zinute = "<K>" + kambarys.gaukPavadinima() + zinute;
		siuskZinute(zinute);
	}

	/**
	 * Siusk zinute.
	 *
	 * @param zinute the zinute
	 */
	public void siuskZinute(String zinute) {
		siuskZinute(zinute.getBytes());
	}

	/**
	 * Siusk zinute.
	 *
	 * @param zinuteBaitais the zinute baitais
	 */
	public void siuskZinute(byte[] zinuteBaitais) {
		if (m_vardas != null) {
			m_sasaja.rasyk(zinuteBaitais);
		}
	}

	/**
	 * Papildyk issiustas zinutes.
	 */
	public void papildykIssiustasZinutes() {
		m_issiunteZinuciu++;
	}

	/**
	 * Papildyk kambariu prisijungimus.
	 */
	public void papildykKambariuPrisijungimus() {
		m_prisijungePrieKambariu++;
	}

	/**
	 * Papildyk isspyrimo kartus.
	 */
	public void papildykIsspyrimoKartus() {
		m_buvoIspirtasKartu++;
	}

	/**
	 * Papilfyk isspyrimus.
	 */
	public void papilfykIsspyrimus() {
		m_ispyreKartu++;
	}

}
