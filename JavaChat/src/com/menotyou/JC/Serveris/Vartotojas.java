package com.menotyou.JC.Serveris;

import java.nio.charset.Charset;
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

/**
 * NIOKlientas klases atitikmuo serverio pusėje. Ši klasė yra tarpininkas tarp
 * klientinės programos ir serverio, bet veikia serverio pusėje.
 */
public class Vartotojas implements SasajosStebetojas {

    public final static Charset utf8 = Charset.forName("UTF-8");
    /** Laikas skirtas vartotojo autentifikacijai, laikas matuojamas milisekundėmis.
     * Laikas yra 5 sekundės */
    private final static long PRISIJUNGIMO_LAIKAS = 5 * 1000;

    /** Laikas kurį vartotojas gali praleisti nieko neveikdamas laikas (milisekundėmis).
     *  */
    private final static long MAX_NEVEIKSNUMO_LAIKAS = 20 * 60 * 1000;

    /** Skaitomų paketų antraštės dydis. */
    private final static int ANTRASTES_DYDIS = 2;

    /** Konstanta žyminti ar sistema yra tipo BigEndian ar ne.
     *  Daugiau informacijos galima rasti NIOBiblioteka/NIOIrankiai.java*/
    private final static boolean BIG_ENDIAN = true;

    /** JCServerio objektas su kurio bus susietas varotojas. */
    private final JCServeris m_serveris;

    /** NIOSasajos objektas. */
    private final NIOSasaja m_sasaja;
    /**Autentifikacijos kintamieji, daugiau apie autentifikaciją žiūrėti priede Autentifikacija. */
    private String m_vardas;
    private String m_slaptazodis;
    private String m_druska;
    private String m_issukis;

    /** Varotojo id duomenų bazėje.*/
    private int m_ID;

    /** Kintamasis saugantis atsijungimo įvykį. */
    private UzdelstasIvykis m_atsijungimoIvykis;

    /** Laikas momento kai vartotojas prisijungė. */
    private Timestamp m_prisijungimoLaikas;

    /** Išsiustų žinučių skaičius.
     * Skaičiuojamos tik paties vartotojo siunčiamos žinutės.
     **/
    private int m_issiunteZinuciu = 0;

    /** Skaičius kambarių į kurius vartotojas prisijungė per vieną sesiją.
     *  Jei vartotojas palieka kambarį ir vėl į jį grįžta, tai irgi fiksuojama
     *  kaip prisijungimas*/
    private int m_prisijungePrieKambariu = 0;

    /** Skaičius nurodantis kiek kartų varotojas buvo išspirtas sesijos metu.*/
    private int m_buvoIspirtasKartu = 0;
    /** Skaičius nurodantis kiek kitų klientų vartotojas išspyrė sesijos metu.*/
    private int m_ispyreKartu = 0;

    /**
     * Sukuriamas naujas vartotojas.
     *
     * @param serveris -> Serveris su kuriuo bus susietas vartotojas.
     * @param sasaja -> NIOSasaja per kurią Vartotjas bendraus su NIOKlientas objektu.
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

    /** 
     * Tai SasajosStebetojo funkcija.
     * Ją iškviečia SasajosStebetojoValdiklis kai
     * užtvirtinamas ryšys su klientu.
     * Užtvirtinus ryšį, iš karto nustatomas atsijungimo įvykis. Tokiu būdu neprisijungę vartotojai bus pašalinami
     * ir neužims vietos serverio atmintyje.
     * Priskyrus atsijungimo įvykį, klientui išsiunčiamas iššūkis. Daugiau apie tai priede Autentifikacija.
     */
    public void rysysUztvirtintas(NIOSasaja sasaja) {
        m_atsijungimoIvykis = m_serveris.gaukEventuValdikli().vykdytiVeliau(new Runnable() {
            public void run() {
                m_sasaja.rasyk("Prisijungimo laikas baigėsi!".getBytes());
                m_sasaja.uzsidarykPoRasymo();
            }
        }, PRISIJUNGIMO_LAIKAS);
        m_sasaja.rasyk(generuokIssuki());
    }

    /** 
     * Funkcija kuri perrašo objekto toString() funkciją. Ši funkcija naudojama stebėti vartotojus serverio konsolėje.
     */
    public String toString() {
        return m_vardas != null ? m_vardas + "@" + m_sasaja.gaukIp() : "Anonimas@" + m_sasaja.gaukIp();
    }

    public String gaukVarda() {
        return m_vardas;
    }

    public int gaukID() {
        return m_ID;
    }

    public java.sql.Timestamp gaukPrisijungimoLaika() {
        return m_prisijungimoLaikas;
    }

    public int gaukZinuciuSK() {
        return m_issiunteZinuciu;
    }

    public int gaukKambariuSK() {
        return m_prisijungePrieKambariu;
    }

    public int gaukKiekKartuIspirtas() {
        return m_buvoIspirtasKartu;
    }

    public int gaukKiekIspyre() {
        return m_ispyreKartu;
    }

    /** Tai SasajosStebetojo funkcija.
     * 	Ją iškviečia SasajosStebetojoValdiklis kai ryšys su klientu nutraukiamas.
     */
    public void rysysNutrauktas(NIOSasaja sasaja, Exception isimtis) {
        sasaja.uzdaryk();
        m_serveris.pasalinkKlienta(this);

    }

    /**
     * Metodas nusatato varotojo autentifikacijos duomenis, kurie gaunami iš duomenų
     * bazės.
     * Jei visi trys parametrai yra null ar -1, tai suprantama kaip klaida ir
     * vartotojui nusiunčiamas klaidos kodas.
     * @param slaptazodis -> vartotojo slaptažodis iš duomenų bazės.
     * @param druska -> vartotojo druska iš duomenų bazės.
     * @param id -> vartotojo id iš duomenų bazės.
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
     * Metodas paruošia neveiksnumo įvyki. Jis skirtas tam, kad būtų pašalinti  vartotojai, kurie
     * per klaidą liko prisijungę, ar ilgai nieko nerašė.
     */
    private void paruoskNeveiksnumoIvyki() {
        if (m_atsijungimoIvykis != null) m_atsijungimoIvykis.atsaukti();
        m_atsijungimoIvykis = m_serveris.gaukEventuValdikli().vykdytiVeliau(new Runnable() {
            public void run() {
                m_sasaja.rasyk("<S>Jūs buvote atjungtas dėl neveiksnumo.".getBytes());
                m_sasaja.uzsidarykPoRasymo();
            }
        }, MAX_NEVEIKSNUMO_LAIKAS);
    }

    /**
     * Tai SasajosStebetojo funkcija.
     * Ją iškviečia SasajosStebetojoValdiklis kai iš kliento gaunama žinutė.
     * Žinutės kurių ilgis lygus 0 ignoruojamos.
     * Gavus žinutė, atnaujinamas neveiksnumo įvykis.
     */
    public void paketasGautas(NIOSasaja sasaja, byte[] paketas) {
        String zinute = new String(paketas, utf8).trim();
        if (zinute.length() == 0) return;
        paruoskNeveiksnumoIvyki();
        apdorokZinute(zinute);
    }

    /**
     * Metodas analogiškas NIOKlientas metodui apdorokZinute(String zinute);
     * Metodas išrenka žinutes pagal numatytus žinučiu formatus. 
     * Žiūrėti priedą žinučių formatai.
     *
     * @param zinute -> gauta žinutė.
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
                naujoKambarioUzklausa(zinute);
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
     * Motadas apdoroja naujo kambario užklausą.
     * 
     * @param zinute
     */
    public void naujoKambarioUzklausa(String zinute) {
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
    }

    /**
     * Generuojama random bitų seka iš 22 baitų.
     * Ši seka paverčiama šešioliktaine išraiška ir
     * iššūkis perduodamas iškvietusiai funkcijai.
     * 
     * @return išūkis ir <C> priedėlis
     * 
     * Daugiau apie tai priede Autentifikacija.
     */
    private byte[] generuokIssuki() {
        final Random r = new SecureRandom();
        byte[] salt = new byte[22];
        r.nextBytes(salt);
        m_issukis = Hex.encodeHexString(salt);
        return ("<C1>" + m_issukis).getBytes();
    }

    /**
     * Metodas pagrinde skirtas testavimui.Tai SasajosStebetojo funkcija.
     * Ją iškviečia SasajosStebetojoValdiklis kai
     * paketas sėkmingai išsiunčiamas.
     */
    public void paketasIssiustas(NIOSasaja sasaja, Object zyme) {

    }

    /**
     * Tai trys to paties metodo variacijos. Iš esmės visi metodai sueina į galutinį
     * siuskZinute(byte[] zinuteBaitais);
     * Taip galima patogiau išviesti vartotojo siuskZinute() funkciją, nerpisirišant
     * prie vienos išraiškos.
     *
     * @param kambarys -> kambarys iš kurio siunčiama žinutė.
     * @param zinute -> siunčiama žinutė.
     */
    public void siuskZinute(Kambarys kambarys, String zinute) {
        zinute = "<K>" + kambarys.gaukPavadinima() + zinute;
        siuskZinute(zinute);
    }

    public void siuskZinute(String zinute) {
        siuskZinute(zinute.getBytes(utf8));
    }

    public void siuskZinute(byte[] zinuteBaitais) {
        if (m_vardas != null) {
            m_sasaja.rasyk(zinuteBaitais);
        }
    }

    public void papildykIssiustasZinutes() {
        m_issiunteZinuciu++;
    }

    public void papildykKambariuPrisijungimus() {
        m_prisijungePrieKambariu++;
    }

    public void papildykIsspyrimoKartus() {
        m_buvoIspirtasKartu++;
    }

    public void papilfykIsspyrimus() {
        m_ispyreKartu++;
    }

}
