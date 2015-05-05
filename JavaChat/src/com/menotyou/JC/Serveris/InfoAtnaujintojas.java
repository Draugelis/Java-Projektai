package com.menotyou.JC.Serveris;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Klasė atsakinga už duomenų apsikeitimą tarp serverio ir duomenų bazės.
 */
public class InfoAtnaujintojas {

    /** SQL komanda įterpti naują juodųjų sąrašų įrašą. */
    private final static String JUODUJU_SARASU_PAPILDYMAS = "INSERT INTO Juodieji_Sarasai (Kambario_ID, Vartotojo_ID) VALUES (?, ?)";

    /** SQL komanda įterpti naują kambarį. */
    private final static String KAMBARIU_PAPILDYMAS = "INSERT INTO Kambariai (Pavadinimas, Zinute, Vartotojo_ID, Istrintas) VALUES (?, ?, ?, 0)";

    /** SQL komanda įterpti naują Sesijos įrašą. */
    private final static String SESIJU_REGISTRO_PAPILDYMAS = "INSERT INTO Sesijos (Vartotojo_ID, Prisijungimo_laikas, Atsijungimo_laikas, Zinuciu_sk, Kambariu_sk, Isspirtas_kartu, Isspyre) VALUES (?, ?, ?, ?, ?, ?, ?)";

    /** Konstanta nurodanti iki kokio įrašų skaičiaus kaupti viename SQL batch'e. */
    private final static int MAX_IRASU = 100;

    /** Nuoroda į patį save. */
    private static InfoAtnaujintojas m_infoAntaujintojas = null;

    /** Paruoštos SQL komandos. */
    private PreparedStatement juodujuSarPapildymas = null;
    private PreparedStatement kambariuPapildymas = null;
    private PreparedStatement sesijuRegPapildymas = null;

    /** Kintamieji žymintys kiek įrašų jau yra sukaupta. */
    private int sukauptaKambariu = 0;
    private int sukauptaJSIrasu = 0;
    private int sukauptaSesiju = 0;

    /** Kintamasisi saugantis sąsają su duomenų baze. */
    private Connection m_con = null;

    /** Serverio objektas kurio duomenis ketinama atnaujinti. */
    private JCServeris m_serveris;

    /**
     * Tuščias konstruktorius. Klasė skirta vykdyti funkcijas ir duomenys jai nurodomi
     * per tam skirtus metodus.
     */
    private InfoAtnaujintojas() {

    }

    /**
     * Grąžina InfoAtnaujintojas objektą, jei tokio nėra - jį sukuria.
     *
     * @return InfoAtnaujintojas objektas.
     */
    public static InfoAtnaujintojas gaukInfoAtnaujintoja() {
        if (m_infoAntaujintojas == null) m_infoAntaujintojas = new InfoAtnaujintojas();
        return m_infoAntaujintojas;
    }

    public void nustatykDB(final Connection con) {
        m_con = con;
    }

    public void nustatykServeri(final JCServeris serveris) {
        m_serveris = serveris;
    }

    public JCServeris gaukServeri() {
        return m_serveris;
    }

    /**
     * Metodas atnaujina serverio kambarių informaciją.
     * Svarbios kelios sąlygos:
     * 	1.Jei serveris neturi duomenų bazėje esančio kambario ir jis nėra ištrintas
     * 		-> Sukuriamas naujas kambarys.
     *  2.Jei serveris turi duomenų bazėje esantį kambarį:
     *  	2.1 Jei kambarys neištrintas:
     *  		->Atnaujinama pradinė žinutė.
     *  		->Priskiriamas id.
     *  	2.2 Jei kambarys ištrintas:
     *  		->Serveris paskelbia kambario uždarymą.
     *
     * @param serverioKambariai -> Map<String, Kambarys> tipo komponentas saugantis
     * serverio kambarius.
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
     * Metodas atnaujina serverio juoduosius kambarius.
     * Siekiant taupyti serverio resursus t.y. laiką:
     * 	1. Duomenų bazėje surikiuojami kambariai pagal id.
     *  2. Serveryje iš eilės einama per gautus rezultatus ir paimamas kambarys su tam tikru id.
     *  3. Jo juodasis sarašas ištrinamas
     *  4. Papildomas duotu įrašu. 
     *  5. Jei užklausos rezultate sutinkamas kitas id, grįžtama į 3., jei ne vykdomas 4.
     *
     * @param serverioKambariai -> Map<String, Kambarys> tipo komponentas saugantis
     * serverio kambarius
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
                    k.gaukJuodajiSarasa().clear();
                }
                if (k == null) continue;
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
     * Visi trys metodai paleidžia jau anksčiau paruoštas ir pildytas SQL užklausas.
     * Jei užklausa == null, veiksmas nevykdomas.
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
     * Metodas į jau paruošta SQL užklausą prideda nurodytus duomenis.
     * Jei užklausa == null, metodas sukuria naują.
     * Naudojamasi JDBC (Java Database Connector driver) funkcija addBatch();
     * taip effektyviau išnaudojamos SQL užklausos, nes tai leidžia vienu kartu pridėti
     * pvz. 100 įrašų, o tai vykdoma daug greičiau nei tie patys 100
     * įrašų po vieną.
     *
     * @param kambarys -> Kambarys objektas kurio duomenys reikia pridėti.
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
     * Metodas į jau paruošta SQL užklausą prideda nurodytus duomenis.
     * Jei užklausa == null, metodas sukuria naują.
     * Naudojamasi JDBC (Java Database Connector driver) funkcija addBatch();
     * taip effektyviau išnaudojamos SQL užklausos, nes tai leidžia vienu kartu pridėti
     * pvz. 100 įrašų, o tai vykdoma daug greičiau nei tie patys 100
     * įrašų po vieną.
     *
     * @param vartotojas -> Vartotojas objektas su kurio id bus susietas įrašas.
     * @param kambarys -> Kambarys objektas su kurio id bus susietas įrašas.
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
     * Metodas į jau paruošta SQL užklausą prideda nurodytus duomenis.
     * Jei užklausa == null, metodas sukuria naują.
     * Naudojamasi JDBC (Java Database Connector driver) funkcija addBatch();
     * taip effektyviau išnaudojamos SQL užklausos, nes tai leidžia vienu kartu pridėti
     * pvz. 100 įrašų, o tai vykdoma daug greičiau nei tie patys 100
     * įrašų po vieną.
     * 
     * Metodas duomenų bazėje išsaugo duomenis kurie buvo surinkit vartotojui
     * esant prisijungus prie programos.
     *
     * @param vartotojas -> vartotojas kurio duomenis bus įrašyti į duomenų bazę.
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
     * Pagalbiniai metodai ResultSet ir PreparedStatement uždarymui.
     * Jei neišeina uždaryti, išimtis ignoruojama
     * @param rs the rs
     */
    private void uzdaryk(ResultSet rs) {
        try {
            rs.close();
        } catch (SQLException e) {
        }
    }

    private void uzdaryk(PreparedStatement ps) {
        try {
            ps.close();
        } catch (SQLException e) {
        }
        ps = null;
    }
}
