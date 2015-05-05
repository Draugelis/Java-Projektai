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

/**
 * Klasė, kuri atsakinga už serverio funcionalumą. Ji naudojasi ServerioSasajosStebetojas 
 * valdikliu(interface).
 */
public class JCServeris implements ServerioSasajosStebetojas {

    public final Charset KODAVIMO_FORMATAS = Charset.forName("UTF-8");
    public final static String NUMATYTA_KAMBARIO_ZINUTE = "Sveiki prisijungę!Visą reikiamą informaciją galite rasti Apie->D.U.K skiltyje.\n" + "Įspėjimas: jūs būsite atjungtas, jei išliksite neveiksnus ilgiau nei 20 min.";

    /** Nustatytas laikas po kuriam praėjus atnaujinama serverio ir duomenų bazės informacija. */
    public final static long LAIKAS_IKI_ATNAUJINIMO = 1 * 60 * 1000;
    private final EventuValdiklis m_eventuValdiklis;

    /** Visų serverio vartotojų sąrašas. */
    private final ArrayList<Vartotojas> m_vartotojai;

    /**Visus serverio kambaris talpinantis Map tipo kintamasis.*/
    private final HashMap<String, Kambarys> m_kambariai;

    /** Obejtas kuris atsakingas už informacijos atnaujinimą. */
    private InfoAtnaujintojas m_infoAtnaujintojas;

    /** Uždelstas veiksmas, kuriame suplanuojami informacijos atnaujinimai. */
    private UzdelstasVeiksmas m_atnaujinimas = null;

    /** Prisijungimas prie duomenų bazės. */
    private Connection db;

    /**
     * Sukuriamas naujas JCServerio objektas.
     * Prisijungiama prie duomenų bazės.
     * Prisijungimo doumenys saugomi .my.cnf faile todėl naudojant
     * Java Properties objektą patogu automatiškai gauti duomenų bazės prisijungimo duomenis.
     *
     * @param eventuValdiklis -> EventuValdiklis su kurio bus susietas šis serveris.
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
        pridekKambari("Pagrindinis", NUMATYTA_KAMBARIO_ZINUTE, -1, -1);
    }

    /** 
     * Metodas, kuris pagrinde naudojamas testuojant. 
     * Jis praneša kai prisijungimas nepavyksta.
     * @param isimtis -> Išimtis kuri buvo užfiksuota nepavykus priėmimui.
     */
    public void priemimasNepavyko(IOException isimtis) {
        System.out.println("Nepavyko priimti prisijungimo: " + isimtis);
    }

    /** 
     * Metodas, kuris pagrinde naudojamas testuojant.
     * Jei netikėtai nustoja veikti serverio sąsaja, programa sustabdoma.
     * Be sąsajos serveris  vis tiek nebegalėtu veikti tinkamai.
     * @param isimtis -> Išimtis kuri buvo užfiksuota sąsajai nustojus veikti.
     */
    public void serverioSasajaMire(Exception isimtis) {
        System.out.println("Serverio sąsaja mire.");
        System.exit(-1);
    }

    /**
     * Tai vienintelis metodas programoje kuris vyksta atskirame procese.
     * Taip daroma tam, kad išvengti galimų "spūsčių" jei pavyžiui vienu metu bandytų jungtis
     * virš 1000 klientų, o ryšys su duomenų baze būtų prastas.
     * Kiekviena tokia užklausa vyksta atskiame procese ir per 
     * EventuValdiklis funkciją asinchroniskasPaleidimas() įterpia į pagrindinį procesą
     * vartotojo duomenis jei tokie rasti.
     *
     * @param vartotojas -> Vartotojas objektas kuriam reikės perduoti duomenis.
     * @param vardas -> Vartojo vardas.
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
                            gaukEventuValdikli().gaukNIOAptarnavima().ispekApieIsimti(new SQLException("Duomenų bazėje trūksta duomenu, nerasta druska ar slaptažodis. Šalinamas vartotojas."));
                            dbSlaptazodis = null;
                            dbDruska = null;
                        }
                        if (rs.next()) {
                            gaukEventuValdikli().gaukNIOAptarnavima().ispekApieIsimti(new SQLException("Duomenų bazėje rasti keli vartotojai tokiu pačiu vardu. Šalinamas vartotojas."));
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
     * Tikrinama ar jau yra kambarys nurodytu pavadinimu.
     *
     * @param pavadinimas -> kambario pavadinimas.
     * @return true arba false.
     */
    public boolean arYraKambarys(String pavadinimas) {
        return m_kambariai.containsKey(pavadinimas);
    }

    /**
     * Tai dvi pridėk kambari variacijos, visos turi savo paskirtį:
     * 	1.Skirta kurti kambariams iš duomenų bazės.
     * 	2.Skirta kurti kambarį kai tai daro kambario savininkas iš klientinės
     * programos.
     * Jei kambarys neturi savininko, saviniko id žymima -1.
     * Jei kambarys kuriamas realiu laiku, kambario id žymima -1 ir priskiriama tik
     * tuomet kai serveris atnaujina kambarius.
     * @param pavadinimas -> kambario pavadinimas.
     * @param pradineZinute -> pradinė kambario žinutė.
     * @param savininkoID -> kambario savininko id.
     * @param kambarioID -> kambario id duomenų bazėje.
     */
    public void pridekKambari(String pavadinimas, String pradineZinute, int savininkoID, int kambarioID) {
        m_kambariai.put(pavadinimas, new Kambarys(pradineZinute, pavadinimas, savininkoID, kambarioID));
    }

    public void pridekKambari(String pavadinimas, String pradineZinute, Vartotojas vartotojas) {
        Kambarys k = new Kambarys(pradineZinute, pavadinimas, vartotojas.gaukID(), -1);
        m_infoAtnaujintojas.pridekDBKambari(k);
        m_kambariai.put(pavadinimas, k);
        k.pridekKlienta(vartotojas);
    }

    /*public void pridekKambari(String pavadinimas, String pradineZinute, int savininkoID) {
    	Kambarys k = new Kambarys(pradineZinute, pavadinimas, savininkoID, -1);
    	m_infoAtnaujintojas.pridekDBKambari(k);
    	m_kambariai.put(pavadinimas, k);
    }*/

    /**
     * Metodui nurodomas siuntėjas ir žinutė. Tuomet metodas iš žinutės gauna kambario pavadinimą
     * ir jei toks kambarys yra, perduoda jam žinutę.
     *
     * @param siuntejas -> siuntėjo Vartotojas objektas.
     * @param zinute -> gauta žinutė.
     */
    public void perduokKambariui(Vartotojas siuntejas, String zinute) {
        String kambarioPavadinimas = zinute.split("<Z>")[0];
        Kambarys kambarys = m_kambariai.get(kambarioPavadinimas);
        System.out.println("Perduodama kambariui: " + kambarioPavadinimas + " Siuntejas: " + siuntejas);
        if (kambarys == null) return;
        kambarys.apdorokZinute(siuntejas, zinute.split("<Z>")[1]);
    }

    public Kambarys gaukKambari(String pavadinimas) {
        return m_kambariai.get(pavadinimas);
    }

    /**
     * Metodas per InfoAtnaujintojas klasė atnaujina pirma 
     * duomenų bazės duomenis, vėliau ir serverio. Taip užtikrinama kad pakeitimai įvykdyti
     * iš serverio pusės galės grįžti kartu su vėliau eisiančiu serverio duomenų atnaujinimu.
     * Pavyzdžiui tokiu būdu nustatomi kambarių id kurie buvo sukurti iš klientinės programos.
     * Metodas taip pat priskiria naują atnaujinimo įvykį, kuris savo ruožtu vėl priskirs naują.
     * Taip vykdomas atnaujinimas kas numatytą laiką.
     */
    public void atnaujinkInfo() {
        System.out.println("Vykdomas atnaujinimas");
        m_infoAtnaujintojas.atnaujinkDBJuoduosius_Sarasus();
        m_infoAtnaujintojas.atnaujinkDBKambarius();
        m_infoAtnaujintojas.atnaujinkDBSesijuRegistra();
        m_infoAtnaujintojas.atnaujinkServerioKamarius(m_kambariai);
        m_infoAtnaujintojas.atnaujikJuoduosiusSarasus(m_kambariai);
        if (m_atnaujinimas != null) m_atnaujinimas.atsaukti();
        m_atnaujinimas = gaukEventuValdikli().vykdytiVeliau(new Runnable() {
            public void run() {
                atnaujinkInfo();
            }
        }, LAIKAS_IKI_ATNAUJINIMO);

    }

    /**
     * Tai ServerioSasajosStebetojo funkcija. 
     * Ji iškviečiama kai su serveriu pirmą kartą susisiekia NIOKlientas.
     * Šis ryšys užfiksuojamas kaip naujas vartotjas ir laukiama jo autentifikacijos
     * proceso pradžios.
     * 
     * @param sasaja -> sasaja iš kurios gautas naujas ryšys.
     */
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

    /**
     * Metodas pašalina klientą iš vartotojų sarašo. Jei Vartotojas objekto
     * vardas yra nustatytas, ir jis yra vartotojų sarašę(Objektas gali būti jau pašalintas
     * iš sarašo, bet dar programos atmintyje), vartotojas pašalinamas iš visų kambarių.
     * Kiekvienas kambarys turi savo šalinimo metodą, todėl jei kambaryje nėra tokio vartotojo,
     * kambarys ignoruoja šia užklausą.
     *
     * @param vartotojas -> Vartotojas objektas kurį ketinama šalinti.
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

    public int gaukVartotojuSkaiciu() {
        return m_vartotojai.size();
    }

    public void uzdaryk(Statement ps) {
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException ignore) {
            }
        }
    }

    public void uzdaryk(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignore) {
            }
        }
    }

    /**
     * Metodas tam tikram vartotojui išsiunčia visų galimų kambariu sąrašą.
     *
     * @param vartotojas -> vartotojas kuriam reikai siųsti sąrašą.
     */
    public void siuskKambariuSarasa(Vartotojas vartotojas) {
        if (vartotojas == null) System.out.println("Klaida: Nepavyko siųsti kambario sąrašo, nes nurodytas vartotojas neegzistuoja!");
        Collection<String> c = m_kambariai.keySet();
        Iterator<String> itr = c.iterator();
        StringBuffer sb = new StringBuffer("<KS>");
        while (itr.hasNext()) {
            sb.append(itr.next());
            if (itr.hasNext()) sb.append("<K>");
        }
        sb.append("<END>");
        vartotojas.siuskZinute(sb.toString());
    }

    /**
     * Tikrinama ar vartotoju sąraše jau yra tam tikras vartotojas.
     * @param vardas -> Vartotojo vardas.
     **/
    public boolean jauPrisijunges(String vardas) {
        for (Vartotojas v : m_vartotojai)
            if (v.gaukVarda() != null && v.gaukVarda().equals(vardas)) return true;
        return false;
    }

    /**
     * Privati klasė kuri naudojasi Runnable valdikliu(interface), paleista per
     * asinchronišką paleidimą, nustato vartotojo autentifikacijos duomenis.
     */
    private class PrisijungimoDuomenuNustatymas implements Runnable {

        private Vartotojas m_vartotojas;
        private String m_slaptazodis, m_druska;
        private int m_id;

        /**
         * @param vartotojas -> vartotojas, kurio duomenis reikia nustatyti.
         * @param slaptazodis -> vartotojo slaptažodis iš duomenų bazės.
         * @param druska -> vartotojo druska iš duomenų bazės.
         * @param id -> vartotojo id iš duomenų bazės.
         */
        public PrisijungimoDuomenuNustatymas(Vartotojas vartotojas, String slaptazodis, String druska, int id) {
            m_vartotojas = vartotojas;
            m_slaptazodis = slaptazodis;
            m_druska = druska;
            m_id = id;
        }

        public void run() {
            System.out.println("Vartotjui perduodami duomenys: " + m_slaptazodis + " ir " + m_druska);
            m_vartotojas.nustatykAuthDuomenis(m_slaptazodis, m_druska, m_id);
        }
    }
}
