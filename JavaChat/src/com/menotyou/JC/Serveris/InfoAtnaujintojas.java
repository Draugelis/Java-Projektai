package com.menotyou.JC.Serveris;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;

// TODO: Auto-generated Javadoc
/**
 * The Class InfoAtnaujintojas.
 */
public class InfoAtnaujintojas {
	
	/** The Constant JUODUJU_SARASU_PAPILDYMAS. */
	private final static String JUODUJU_SARASU_PAPILDYMAS = "INSERT INTO Juodieji_Sarasai (Kambario_ID, Vartotojo_ID) VALUES (?, ?)";
	
	/** The Constant KAMBARIU_PAPILDYMAS. */
	private final static String KAMBARIU_PAPILDYMAS = "INSERT INTO Kambariai (Pavadinimas, Zinute, Vartotojo_ID, Istrintas) VALUES (?, ?, ?, 0)";
	
	/** The Constant SESIJU_REGISTRO_PAPILDYMAS. */
	private final static String SESIJU_REGISTRO_PAPILDYMAS = "INSERT INTO Sesijos (Vartotojo_ID, Prisijungimo_laikas, Atsijungimo_laikas, Zinuciu_sk, Kambariu_sk, Isspirtas_kartu, Isspyre) VALUES (?, ?, ?, ?, ?, ?, ?)";
	
	/** The Constant MAX_IRASU. */
	private final static int MAX_IRASU = 100;
	
	/** The m_info antaujintojas. */
	private static InfoAtnaujintojas m_infoAntaujintojas = null;

	/** The juoduju sar papildymas. */
	private PreparedStatement juodujuSarPapildymas = null;
	
	/** The kambariu papildymas. */
	private PreparedStatement kambariuPapildymas = null;
	
	/** The sesiju reg papildymas. */
	private PreparedStatement sesijuRegPapildymas = null;

	/** The sukaupta kambariu. */
	private int sukauptaKambariu = 0;
	
	/** The sukaupta js irasu. */
	private int sukauptaJSIrasu = 0;
	
	/** The sukaupta sesiju. */
	private int sukauptaSesiju = 0;

	/** The m_con. */
	private Connection m_con = null;
	
	/** The m_serveris. */
	private JCServeris m_serveris;

	/**
	 * Instantiates a new info atnaujintojas.
	 */
	private InfoAtnaujintojas() {

	}

	/**
	 * Gauk info atnaujintoja.
	 *
	 * @return the info atnaujintojas
	 */
	public static InfoAtnaujintojas gaukInfoAtnaujintoja() {
		if (m_infoAntaujintojas == null) m_infoAntaujintojas = new InfoAtnaujintojas();
		return m_infoAntaujintojas;
	}

	/**
	 * Nustatyk db.
	 *
	 * @param con the con
	 */
	public void nustatykDB(final Connection con) {
		m_con = con;
	}

	/**
	 * Nustatyk serveri.
	 *
	 * @param serveris the serveris
	 */
	public void nustatykServeri(final JCServeris serveris) {
		m_serveris = serveris;
	}

	/**
	 * Gauk serveri.
	 *
	 * @return the JC serveris
	 */
	public JCServeris gaukServeri() {
		return m_serveris;
	}

	/**
	 * Atnaujink serverio kamarius.
	 *
	 * @param serverioKambariai the serverio kambariai
	 */
	public void atnaujinkServerioKamarius(HashMap<String, Kambarys> serverioKambariai) {
		if (m_con == null) {
			System.out.println("Nėra ryšio su duomenų baze, atnaujinimas nutraukiamas");
			return;
		}
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = m_con.prepareStatement("SELECT * FROM Kambariai");
			rs = ps.executeQuery();
			while (rs.next()) {
				int kambarioID = rs.getInt(1);
				String pavadinimas = rs.getString(2);
				String zinute = rs.getString(3);
				if (zinute == null) zinute = "";
				int savininkoID = rs.getInt(4);
				boolean istrintas = rs.getBoolean(5);
				if (!serverioKambariai.containsKey(pavadinimas)) {
					if (!istrintas) {
						m_serveris.pridekKambari(pavadinimas, zinute, savininkoID, kambarioID);
					}
				} else if (serverioKambariai.containsKey(pavadinimas)) {
					Kambarys k = serverioKambariai.get(pavadinimas);
					if (!istrintas) {
						if (!k.gaukKambarioZinute().equals(zinute)) {
							k.nustatykKambarioZinute(zinute);
						}
						if (k.gaukKambarioID() == -1) {
							k.nustatykKambarioID(kambarioID);
						}
					} else {
						k.paskelbkUzdaryma(m_serveris);
					}
				}
			}
			System.out.println("Serverio kambariai atnaujinti");
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			uzdaryk(ps);
			uzdaryk(rs);
		}
	}

	/**
	 * Atnaujik juoduosius sarasus.
	 *
	 * @param serverioKambariai the serverio kambariai
	 */
	public void atnaujikJuoduosiusSarasus(HashMap<String, Kambarys> serverioKambariai) {
		if (m_con == null) {
			System.out.println("Nėra ryšio su duomenų baze, atnaujinimas nutraukiamas");
			return;
		}
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = m_con.prepareStatement("SELECT k.Kambario_ID, k.Pavadinimas, j.Vartotojo_ID FROM Juodieji_Sarasai j, Kambariai k WHERE j.Kambario_ID=k.Kambario_ID ORDER BY k.Kambario_ID");
			rs = ps.executeQuery();
			int paskutinioID = -1;
			Kambarys k = null;
			while (rs.next()) {
				int kambarioID = rs.getInt(1);
				String pavadinimas = rs.getString(2);
				int vartotojoID = rs.getInt(3);
				if (kambarioID != paskutinioID) {
					k = serverioKambariai.get(pavadinimas);
					paskutinioID = kambarioID;
					if (k == null) continue;
					System.out.println("Išvalomas kambario " + k.gaukPavadinima() + " juodasis sarašas");
					k.gaukJuodajiSarasa().clear();
				}
				if (k == null) continue;
				System.out.println("Į kambario " + k.gaukPavadinima() + " juodajį sarašą įrašomas id " + vartotojoID);
				k.gaukJuodajiSarasa().add(vartotojoID);
			}
			System.out.println("Serverio juodieji sarašai atnaujinti");
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			uzdaryk(rs);
			uzdaryk(ps);

		}
	}

	/**
	 * Atnaujink db juoduosius_ sarasus.
	 */
	public void atnaujinkDBJuoduosius_Sarasus() {
		if (juodujuSarPapildymas == null) return;
		try {
			juodujuSarPapildymas.executeBatch();
			sukauptaJSIrasu = 0;
			uzdaryk(juodujuSarPapildymas);
			System.out.println("DB kambariai papildyti");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Atnaujink db kambarius.
	 */
	public void atnaujinkDBKambarius() {
		if (kambariuPapildymas == null) return;
		try {
			kambariuPapildymas.executeBatch();
			sukauptaKambariu = 0;
			uzdaryk(kambariuPapildymas);
			System.out.println("DB juodieji sarašai papildyti");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Atnaujink db sesiju registra.
	 */
	public void atnaujinkDBSesijuRegistra() {
		if (sesijuRegPapildymas == null) return;
		try {
			sesijuRegPapildymas.executeBatch();
			sukauptaSesiju = 0;
			uzdaryk(sesijuRegPapildymas);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Pridek db kambari.
	 *
	 * @param kambarys the kambarys
	 */
	public void pridekDBKambari(Kambarys kambarys) {
		if (kambariuPapildymas == null) {
			try {
				kambariuPapildymas = m_con.prepareStatement(KAMBARIU_PAPILDYMAS);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		try {
			kambariuPapildymas.setString(1, kambarys.gaukPavadinima());
			kambariuPapildymas.setString(2, kambarys.gaukKambarioZinute());
			kambariuPapildymas.setInt(3, kambarys.gaukKambarioSavininkoID());
			kambariuPapildymas.addBatch();
			sukauptaKambariu++;
			if (sukauptaKambariu >= MAX_IRASU) {
				atnaujinkDBKambarius();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Pridek js irasa.
	 *
	 * @param vartotojas the vartotojas
	 * @param kambarys the kambarys
	 */
	public void pridekJSIrasa(Vartotojas vartotojas, Kambarys kambarys) {
		if (juodujuSarPapildymas == null) {
			try {
				juodujuSarPapildymas = m_con.prepareStatement(JUODUJU_SARASU_PAPILDYMAS);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		try {
			juodujuSarPapildymas.setInt(1, kambarys.gaukKambarioID());
			juodujuSarPapildymas.setInt(2, vartotojas.gaukID());
			juodujuSarPapildymas.addBatch();
			sukauptaJSIrasu++;
			if (sukauptaJSIrasu >= MAX_IRASU) {
				atnaujinkDBJuoduosius_Sarasus();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Pridek sesijos irasa.
	 *
	 * @param vartotojas the vartotojas
	 */
	public void pridekSesijosIrasa(Vartotojas vartotojas) {
		if (sesijuRegPapildymas == null) {
			try {
				sesijuRegPapildymas = m_con.prepareStatement(SESIJU_REGISTRO_PAPILDYMAS);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		try {
			sesijuRegPapildymas.setInt(1, vartotojas.gaukID());
			sesijuRegPapildymas.setTimestamp(2, vartotojas.gaukPrisijungimoLaika());
			sesijuRegPapildymas.setTimestamp(3, new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()));
			sesijuRegPapildymas.setInt(4, vartotojas.gaukZinuciuSK());
			sesijuRegPapildymas.setInt(5, vartotojas.gaukKambariuSK());
			sesijuRegPapildymas.setInt(6, vartotojas.gaukKiekKartuIspirtas());
			sesijuRegPapildymas.setInt(7, vartotojas.gaukKiekIspyre());
			sesijuRegPapildymas.addBatch();
			sukauptaSesiju++;
			if (sukauptaSesiju >= MAX_IRASU) {
				atnaujinkDBSesijuRegistra();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Uzdaryk.
	 *
	 * @param rs the rs
	 */
	private void uzdaryk(ResultSet rs) {
		try {
			rs.close();
		} catch (SQLException e) {
		}
	}

	/**
	 * Uzdaryk.
	 *
	 * @param ps the ps
	 */
	private void uzdaryk(PreparedStatement ps) {
		try {
			ps.close();
		} catch (SQLException e) {
		}
		ps = null;
	}
}
