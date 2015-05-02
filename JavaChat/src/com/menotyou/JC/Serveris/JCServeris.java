package com.menotyou.JC.Serveris;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import com.menotyou.JC.NIOBiblioteka.NIOSasaja;
import com.menotyou.JC.NIOBiblioteka.ServerioSasajosStebetojas;
import com.menotyou.JC.NIOBiblioteka.EventuValdiklis.EventuValdiklis;
import com.menotyou.JC.NIOBiblioteka.EventuValdiklis.UzdelstasVeiksmas;

// TODO: Auto-generated Javadoc
/**
 * The Class JCServeris.
 */
public class JCServeris implements ServerioSasajosStebetojas {

	/** The kodavimo formatas. */
	public final Charset KODAVIMO_FORMATAS = Charset.forName("UTF-8");
	
	/** The Constant NUMATYTA_KAMBARIO_ZINUTE. */
	public final static String NUMATYTA_KAMBARIO_ZINUTE = "Sveiki prisijungę! Meniu Kambariai galite prisijungti prie jau esamo kambario arba sukurti savo.\n"
														 + "Jei norite pakeisti teksto išvaizda, nustatymus rasite meniu Nustatymai.\n"
														 + "Informaciją apie programą rasite meniu Apie";
	
	/** The Constant LAIKAS_IKI_ATNAUJINIMO. */
	public final static long LAIKAS_IKI_ATNAUJINIMO = 1*60*1000;
	
	/** The m_eventu valdiklis. */
	private final EventuValdiklis m_eventuValdiklis;
	
	/** The m_vartotojai. */
	private final ArrayList<Vartotojas> m_vartotojai;
	
	/** The m_kambariai. */
	private final HashMap<String, Kambarys> m_kambariai;
	
	/** The m_info atnaujintojas. */
	private InfoAtnaujintojas m_infoAtnaujintojas;
	
	/** The m_atnaujinimas. */
	private UzdelstasVeiksmas m_atnaujinimas = null;
	
	/** The db. */
	private Connection db;

	/**
	 * Instantiates a new JC serveris.
	 *
	 * @param eventuValdiklis the eventu valdiklis
	 */
	public JCServeris(EventuValdiklis eventuValdiklis) {
		m_eventuValdiklis = eventuValdiklis;
		m_vartotojai = new ArrayList<Vartotojas>();
		m_kambariai = new HashMap<String, Kambarys>();
		m_infoAtnaujintojas = InfoAtnaujintojas.gaukInfoAtnaujintoja();
		Properties DBnustatymai = new Properties();
		try {
			FileInputStream infoFailas = new FileInputStream(new File(".my.cnf"));
			DBnustatymai.load(infoFailas);
			db = DriverManager.getConnection("jdbc:mysql://localhost/vtautvydas", DBnustatymai.getProperty("user"), DBnustatymai.getProperty("password"));
			infoFailas.close();
			System.out.println("Db connection: OK");
		} catch (SQLException e) {
			db = null;
			e.printStackTrace();
			System.out.println("Nepavyko prisijungti prie duomenų bazės.");
		} catch (IOException e) {
			db = null;
			System.out.println("Kilo klaida failo nuskaityme.");
			e.printStackTrace();
		}
		m_infoAtnaujintojas.nustatykDB(db);
		m_infoAtnaujintojas.nustatykServeri(this);
		atnaujinkInfo();
		pridekKambari("Pagrindinis", NUMATYTA_KAMBARIO_ZINUTE);
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.ServerioSasajosStebetojas#priemimasNepavyko(java.io.IOException)
	 */
	public void priemimasNepavyko(IOException isimtis) {
		System.out.println("Nepavyko priimti prisijungimo: " + isimtis);
	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.ServerioSasajosStebetojas#serverioSasajaMire(java.lang.Exception)
	 */
	public void serverioSasajaMire(Exception isimtis) {
		System.out.println("Serverio sąsaja mire.");
		System.exit(-1);
	}

	/**
	 * Gauk duomenis.
	 *
	 * @param vartotojas the vartotojas
	 * @param vardas the vardas
	 */
	public void gaukDuomenis(final Vartotojas vartotojas, final String vardas) {
		new Thread() {
			public void run() {
				if (vardas.isEmpty() || vardas == null) {
					gaukEventuValdikli().asinchroniskasPaleidimas(new PrisijungimoDuomenuNustatymas(vartotojas, null, null, -1));
				}
				PreparedStatement ps = null;
				ResultSet rs = null;
				try {
					ps = db.prepareStatement("SELECT * FROM Prisijungimai WHERE Vardas = ?");
					ps.setString(1, vardas);
					rs = ps.executeQuery();
					String dbSlaptazodis, dbDruska;
					int dbID;
					if (rs.next()) {
						dbSlaptazodis = rs.getString("Slaptazodis");
						dbDruska = rs.getString("Salt");
						dbID = Integer.parseInt(rs.getString("Vartotojo_ID"));
						if (dbSlaptazodis == null || dbDruska == null) {
							gaukEventuValdikli().gaukNIOAptarnavima().ispekApieIsimti(new SQLException("Duomenu bazėje trūksta duomenu, nerasta druska ir slaptažodis. Šalinamas vartotojas."));
							dbSlaptazodis = null;
							dbDruska = null;
						}
						if (rs.next()) {
							gaukEventuValdikli().gaukNIOAptarnavima().ispekApieIsimti(new SQLException("Duomenu bazėje rasti keli vartotjai tokiu pačiu vardu. Šalinamas vartotojas."));
							dbSlaptazodis = null;
							dbDruska = null;
						}
						gaukEventuValdikli().asinchroniskasPaleidimas(new PrisijungimoDuomenuNustatymas(vartotojas, dbSlaptazodis, dbDruska, dbID));
					} else {
						gaukEventuValdikli().asinchroniskasPaleidimas(new PrisijungimoDuomenuNustatymas(vartotojas, null, null, -1));
					}
				} catch (SQLException e) {
					gaukEventuValdikli().gaukNIOAptarnavima().ispekApieIsimti(e);
					gaukEventuValdikli().asinchroniskasPaleidimas(new PrisijungimoDuomenuNustatymas(vartotojas, null, null, -1));
				} finally {
					uzdaryk(ps);
					uzdaryk(rs);
				}
			}
		}.start();
	}

	/**
	 * Ar yra kambarys.
	 *
	 * @param pavadinimas the pavadinimas
	 * @return true, if successful
	 */
	public boolean arYraKambarys(String pavadinimas) {
		return m_kambariai.containsKey(pavadinimas);
	}

	/**
	 * Pridek kambari.
	 *
	 * @param pavadinimas the pavadinimas
	 * @param pradineZinute the pradine zinute
	 * @param vartotojas the vartotojas
	 */
	public void pridekKambari(String pavadinimas, String pradineZinute, Vartotojas vartotojas) {
		Kambarys k = new Kambarys(pradineZinute, pavadinimas, vartotojas.gaukID(), -1);
		m_infoAtnaujintojas.pridekDBKambari(k);
		m_kambariai.put(pavadinimas, k);
		k.pridekKlienta(vartotojas);
	}
	
	/**
	 * Pridek kambari.
	 *
	 * @param pavadinimas the pavadinimas
	 * @param pradineZinute the pradine zinute
	 * @param savininkoID the savininko id
	 */
	public void pridekKambari(String pavadinimas, String pradineZinute, int savininkoID) {
		Kambarys k = new Kambarys(pradineZinute, pavadinimas, savininkoID, -1);
		m_infoAtnaujintojas.pridekDBKambari(k);
		m_kambariai.put(pavadinimas, k);
	}
	
	/**
	 * Pridek kambari.
	 *
	 * @param pavadinimas the pavadinimas
	 * @param pradineZinute the pradine zinute
	 * @param savininkoID the savininko id
	 * @param kambarioID the kambario id
	 */
	public void pridekKambari(String pavadinimas, String pradineZinute, int savininkoID, int kambarioID) {
		m_kambariai.put(pavadinimas, new Kambarys(pradineZinute, pavadinimas, savininkoID, kambarioID));
	}
	
	/**
	 * Pridek kambari.
	 *
	 * @param pavadinimas the pavadinimas
	 * @param pradineZinute the pradine zinute
	 */
	public void pridekKambari(String pavadinimas, String pradineZinute) {
		m_kambariai.put(pavadinimas, new Kambarys(pradineZinute, pavadinimas, -1, -1));
	}

	/**
	 * Perduok kambariui.
	 *
	 * @param siuntejas the siuntejas
	 * @param zinute the zinute
	 */
	public void perduokKambariui(Vartotojas siuntejas, String zinute) {
		String kambarioPavadinimas = zinute.split("<Z>")[0];
		Kambarys kambarys = m_kambariai.get(kambarioPavadinimas);
		System.out.println("Perduodama kambariui: " + kambarioPavadinimas + " Siuntejas: " + siuntejas);
		if (kambarys == null) return;
		kambarys.apdorokZinute(siuntejas, zinute.split("<Z>")[1]);
	}

	/**
	 * Gauk kambari.
	 *
	 * @param pavadinimas the pavadinimas
	 * @return the kambarys
	 */
	public Kambarys gaukKambari(String pavadinimas) {
		return m_kambariai.get(pavadinimas);
	}
	
	/**
	 * Atnaujink info.
	 */
	public void atnaujinkInfo(){
			System.out.println("Vykdomas atnaujinimas");
		m_infoAtnaujintojas.atnaujinkServerioKamarius(m_kambariai);
		m_infoAtnaujintojas.atnaujikJuoduosiusSarasus(m_kambariai);
		m_infoAtnaujintojas.atnaujinkDBJuoduosius_Sarasus();
		m_infoAtnaujintojas.atnaujinkDBKambarius();
		m_infoAtnaujintojas.atnaujinkDBSesijuRegistra();
		if (m_atnaujinimas != null) m_atnaujinimas.atsaukti();
		m_atnaujinimas = gaukEventuValdikli().vykdytiVeliau(new Runnable() {
			public void run() {
				atnaujinkInfo();
			}
		}, LAIKAS_IKI_ATNAUJINIMO);

	}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.ServerioSasajosStebetojas#naujasSujungimas(com.menotyou.JC.NIOBiblioteka.NIOSasaja)
	 */
	public void naujasSujungimas(NIOSasaja sasaja) {
		System.out.println("Prisijunge naujas vartotojas iš" + sasaja.gaukIp() + ".");
		m_vartotojai.add(new Vartotojas(this, sasaja));
	}

	/**
	 * Gauk eventu valdikli.
	 *
	 * @return the eventu valdiklis
	 */
	public EventuValdiklis gaukEventuValdikli() {
		return m_eventuValdiklis;
	}

	/**
	 * Siusk visiems vartotojams.
	 *
	 * @param zinute the zinute
	 */
	public void siuskVisiemsVartotojams(String zinute) {
		byte[] zinuteBaitais = zinute.getBytes();
		for (Vartotojas vartotojas : m_vartotojai) {
			vartotojas.siuskZinute(zinuteBaitais);
		}
	}

	/**
	 * Pasalink klienta.
	 *
	 * @param vartotojas the vartotojas
	 */
	public void pasalinkKlienta(Vartotojas vartotojas) {
		if (vartotojas.gaukVarda() != null && m_vartotojai.contains(vartotojas)) {
			m_infoAtnaujintojas.pridekSesijosIrasa(vartotojas);
			Collection<Kambarys> c = m_kambariai.values();
			Iterator<Kambarys> itr = c.iterator();
			while (itr.hasNext())
				itr.next().pasalinkKlienta(vartotojas);
		}
		m_vartotojai.remove(vartotojas);
	}

	/**
	 * Gauk vartotoju skaiciu.
	 *
	 * @return the int
	 */
	public int gaukVartotojuSkaiciu() {
		return m_vartotojai.size();
	}
	
	/**
	 * Uzdaryk.
	 *
	 * @param ps the ps
	 */
	public void uzdaryk(Statement ps) {
	       if (ps!=null){
	           try {
	               ps.close();
	           } catch (SQLException ignore) {
	           }
	       }
	 }
	 
	 /**
 	 * Uzdaryk.
 	 *
 	 * @param rs the rs
 	 */
 	public void uzdaryk(ResultSet rs) {
	     if (rs!=null){
	         try {
	             rs.close();
	         } catch (SQLException ignore) {
	        	 System.out.println("Ignoruoti šią išimtį");
	         }
	     }
	}

	/**
	 * Siusk kambariu sarasa.
	 *
	 * @param vartotojas the vartotojas
	 */
	public void siuskKambariuSarasa(Vartotojas vartotojas) {
		if(vartotojas == null) System.out.println("Klaida: Nepavyko siųsti kambario sąrašo, nes nurodytas vartotojas neegzistuoja!");
		Collection<String> c = m_kambariai.keySet();
		Iterator<String> itr = c.iterator();
		StringBuffer sb = new StringBuffer("<KS>");
		while(itr.hasNext()){
			sb.append(itr.next());
			if (itr.hasNext())
				sb.append("<K>");
		}
		sb.append("<END>");
		vartotojas.siuskZinute(sb.toString());
	}

	/**
	 * Jau prisijunges.
	 *
	 * @param zinute the zinute
	 * @return true, if successful
	 */
	public boolean jauPrisijunges(String zinute) {
		for(Vartotojas v: m_vartotojai)
			if(v.gaukVarda() != null && v.gaukVarda().equals(zinute)) return true;
		return false;
	}
	
	/**
	 * The Class PrisijungimoDuomenuNustatymas.
	 */
	private class PrisijungimoDuomenuNustatymas implements Runnable {
		
		/** The m_vartotojas. */
		private Vartotojas m_vartotojas;
		
		/** The m_druska. */
		private String m_slaptazodis, m_druska;
		
		/** The m_id. */
		private int m_id;
		
		/**
		 * Instantiates a new prisijungimo duomenu nustatymas.
		 *
		 * @param vartotojas the vartotojas
		 * @param slaptazodis the slaptazodis
		 * @param druska the druska
		 * @param id the id
		 */
		public PrisijungimoDuomenuNustatymas(Vartotojas vartotojas, String slaptazodis, String druska, int id) {
			m_vartotojas = vartotojas;
			m_slaptazodis = slaptazodis;
			m_druska = druska;
			m_id = id;
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			System.out.println("Vartotjui perduodami duomenys: " + m_slaptazodis +" ir " + m_druska);
			m_vartotojas.nustatykAuthDuomenis(m_slaptazodis, m_druska, m_id);
		}
	}
}
