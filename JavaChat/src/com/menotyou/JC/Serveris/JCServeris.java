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

public class JCServeris implements ServerioSasajosStebetojas {

	public final Charset KODAVIMO_FORMATAS = Charset.forName("UTF-8");
	private final static String NUMATYTA_KAMBARIO_ZINUTE = "Sveiki prisijungę! Visą reikalingą informaciją rasite skyriuje Pagalba!";
	private final EventuValdiklis m_eventuValdiklis;
	private final ArrayList<Vartotojas> m_vartotojai;
	private final HashMap<String, Kambarys> m_kambariai;
	private Connection db;

	public JCServeris(EventuValdiklis eventuValdiklis) {
		m_eventuValdiklis = eventuValdiklis;
		m_vartotojai = new ArrayList<Vartotojas>();
		m_kambariai = new HashMap<String, Kambarys>();
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

		pridekKambari("Pagrindinis", NUMATYTA_KAMBARIO_ZINUTE, null);
	}

	public void priemimasNepavyko(IOException isimtis) {
		System.out.println("Nepavyko priimti prisijungimo: " + isimtis);
	}

	public void serverioSasajaMire(Exception isimtis) {
		System.out.println("Serverio sąsaja mire.");
		System.exit(-1);
	}

	public void gaukDuomenis(final Vartotojas vartotojas, final String vardas) {
		new Thread() {
			public void run() {
				if (vardas.isEmpty() || vardas == null) {
					gaukEventuValdikli().asinchroniskasPaleidimas(new PrisijungimoDuomenuNustatymas(vartotojas, null, null));
				}
				PreparedStatement ps = null;
				ResultSet rs = null;
				try {
					ps = db.prepareStatement("SELECT * FROM Prisijungimai WHERE Vardas = ?");
					ps.setString(1, vardas);
					rs = ps.executeQuery();
					String dbSlaptazodis, dbDruska;
					if (rs.next()) {
						System.out.println("Rastas vartotojas vardu:" + rs.getString("Vardas"));
						System.out.println("Vartotojo_ID: " + rs.getString("Vartotojo_ID"));
						dbSlaptazodis = rs.getString("Slaptazodis");
						dbDruska = rs.getString("Salt");
						System.out.println("Slaptažodis:" + dbSlaptazodis);
						System.out.println("Druska: " + dbDruska);
						if (dbSlaptazodis == null || dbDruska == null) {
							gaukEventuValdikli().gaukNIOAptarnavima().ispekApieIsimti(new SQLException("Duomenu bazėje trūksta duomenu, nerasta druska ir slaptažodis. Šalinamas vartotojas."));
							dbSlaptazodis = null;
							dbDruska = null;
						}
						if (rs.next()) {
							System.out.println("Rastas dar vienas vartotojas vardu: " +  rs.getString("Vardas"));
							gaukEventuValdikli().gaukNIOAptarnavima().ispekApieIsimti(new SQLException("Duomenu bazėje rasti keli vartotjai tokiu pačiu vardu. Šalinamas vartotojas."));
							dbSlaptazodis = null;
							dbDruska = null;
						}
						gaukEventuValdikli().asinchroniskasPaleidimas(new PrisijungimoDuomenuNustatymas(vartotojas, dbSlaptazodis, dbDruska));
					} else {
						gaukEventuValdikli().asinchroniskasPaleidimas(new PrisijungimoDuomenuNustatymas(vartotojas, null, null));
					}
				} catch (SQLException e) {
					gaukEventuValdikli().gaukNIOAptarnavima().ispekApieIsimti(e);
				} finally {
					close(ps);
					close(rs);
				}
			}
		}.start();
	}

	public boolean arYraKambarys(String pavadinimas) {
		return m_kambariai.containsKey(pavadinimas);
	}

	public void pridekKambari(String pavadinimas, String pradineZinute, Vartotojas vartotojas) {
		m_kambariai.put(pavadinimas, new Kambarys(pradineZinute, pavadinimas, vartotojas));

	}

	public void perduokKambariui(Vartotojas siuntejas, String zinute) {
		String kambarioPavadinimas = zinute.split("<Z>")[0];
		zinute = zinute.split("<Z>")[1];
		Kambarys kambarys = m_kambariai.get(kambarioPavadinimas);
		System.out.println("Perduodama kambariui: " + kambarioPavadinimas + " Siuntejas: " + siuntejas);
		if (kambarys == null) return;
		kambarys.apdorokZinute(siuntejas, zinute);
	}

	public Kambarys gaukKambari(String pavadinimas) {
		return m_kambariai.get(pavadinimas);
	}

	public void naujasSujungimas(NIOSasaja sasaja) {
		System.out.println("Prisijunge naujas vartotojas iš" + sasaja.gaukIp() + ".");
		m_vartotojai.add(new Vartotojas(this, sasaja));
	}

	public EventuValdiklis gaukEventuValdikli() {
		return m_eventuValdiklis;
	}

	public void siuskVisiemsVartotojams(String zinute) {
		byte[] zinuteBaitais = zinute.getBytes();
		for (Vartotojas vartotojas : m_vartotojai) {
			vartotojas.siuskZinute(zinuteBaitais);
		}
	}

	public void pasalinkKlienta(Vartotojas vartotojas) {
		if (vartotojas.gaukVarda() != null) {
			Collection<Kambarys> c = m_kambariai.values();
			Iterator<Kambarys> itr = c.iterator();
			while (itr.hasNext())
				itr.next().pasalinkKlienta(vartotojas);
		}
		m_vartotojai.remove(vartotojas);
	}

	public int gaukVartotojuSkaiciu() {
		return m_vartotojai.size();
	}

	private class PrisijungimoDuomenuNustatymas implements Runnable {
		private Vartotojas m_vartotojas;
		private String m_slaptazodis, m_druska;
		public PrisijungimoDuomenuNustatymas(Vartotojas vartotojas, String slaptazodis, String druska) {
			m_vartotojas = vartotojas;
			m_slaptazodis = slaptazodis;
			m_druska = druska;
		}

		public void run() {
			System.out.println("Vartotjui perduodami duomenys: " + m_slaptazodis +" ir " + m_druska);
			m_vartotojas.nustatykAuthDuomenis(m_slaptazodis, m_druska);
		}
	}
	
	public void close(Statement ps) {
	       if (ps!=null){
	           try {
	               ps.close();
	           } catch (SQLException ignore) {
	           }
	       }
	   }
	 
	 public void close(ResultSet rs) {
	     if (rs!=null){
	         try {
	             rs.close();
	         } catch (SQLException ignore) {
	         }
	     }
	}

}
