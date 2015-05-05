package com.menotyou.JC;

import java.awt.Font;
import java.io.IOException;
import java.nio.charset.Charset;
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

/**
 * Klasė NIOKlientas, ji naudojasi SasajosStebetojas valdikliu(interface).
 * Tai pagrindinis ir vienintelis kliento programos objektas galintis bendrauti su serveriu.
 */
public class NIOKlientas implements SasajosStebetojas {

    public final static Charset utf8 = Charset.forName("UTF-8");
    /** Datos formatas*/
    private final static DateFormat DATOS_FORMA = new SimpleDateFormat("HH:mm:ss");

    /** Skaitomų paketų antraštės dydis. */
    private final static int ANTRASTES_DYDIS = 2;

    /** Konstanta žyminti ar sistema yra tipo BigEndian ar ne.
     *  Daugiau informacijos galima rasti NIOBiblioteka/NIOIrankiai.java*/
    private final static boolean BIG_ENDIAN = true;

    /** EventuValdiklis objektas. Daugiau informacijos žiūrėti EventuValdiklis.java*/
    private final EventuValdiklis m_eventuValdiklis;

    /** NIOSasajos objektas. Žiūrėti NIOSasaja.java */
    private final NIOSasaja m_sasaja;

    private Boolean autentifikuotas;
    private String m_vardas;
    private String m_slaptazodis;
    private String issukis;
    private KlientoLangas m_klientoLangas;
    private SvecioPrisijungimas m_svecioPrisijungimas;

    /** Kliento atidarytų kambarių sarašas. */
    private final HashMap<String, KambarioInterfeisas> m_kambariai;

    /**
     * Sukuriamas naujas klientas ir jam priskiriami kliento lango ir prisijungimo lango objektai.
     *
     * @param kl -> Kliento lango objektas.
     * @param sp -> Svečio prisijungimo lango objektas.
     * @throws IOException Įspėja kad įvyko klaida kuriant NIOSasają ar vykdant kitą IO operaciją.
     */
    public NIOKlientas(KlientoLangas kl, SvecioPrisijungimas sp) throws IOException {
        m_eventuValdiklis = new EventuValdiklis();
        m_sasaja = m_eventuValdiklis.gaukNIOAptarnavima().sukurkSasaja("shared.fln.lt", 8192);
        m_svecioPrisijungimas = sp;
        m_klientoLangas = kl;
        autentifikuotas = false;
        m_kambariai = new HashMap<String, KambarioInterfeisas>();
        m_sasaja.nustatykPaketuRasytoja(new PaprastasPaketuRasytojas(ANTRASTES_DYDIS, BIG_ENDIAN));
        m_sasaja.nustatykPaketuSkaitytoja(new PaprastasPaketuSkaitytojas(ANTRASTES_DYDIS, BIG_ENDIAN));
        m_sasaja.stebek(this);
    }

    public void start() {
        m_eventuValdiklis.start();
    }

    /**
     * Nustatomi visų atidarytų kambarių šriftai.
     *
     * @param sriftas -> nurodytas šriftas.
     */
    public void nustatykSriftus(Font sriftas) {
        Collection<KambarioInterfeisas> c = m_kambariai.values();
        Iterator<KambarioInterfeisas> itr = c.iterator();
        while (itr.hasNext())
            itr.next().nustatykIstorijosSrifta(sriftas);
    }

    public String gaukVarda() {
        return m_vardas;
    }

    public boolean jauAtidarytasKambarys(String kambarys) {
        return m_kambariai.containsKey(kambarys);
    }

    /**
     * Pradedama autentifikacija. Apie autentifikacijos procesą daugiau informacijos galima rasti
     * priede Autentifikacija.
     *
     * @param vardas -> Vartotojo vardas nurodytas Svečio prisijungimo lange.
     * @param slaptazodis -> Vartotojo slaptažodis nurodytas Svečio prisijungimo lange.
     */
    public void pradekAutentifikacija(String vardas, String slaptazodis) {
        m_vardas = vardas;
        m_slaptazodis = slaptazodis;
        siuskZinute("<R1>" + vardas);
    }

    public void siuskZinute(String zinute) {
        byte[] zinuteBaitais = zinute.getBytes(utf8);
        m_sasaja.rasyk(zinuteBaitais);
    }

    /**
     * Metodas pagrinde skirtas testavimui.Tai SasajosStebetojo funkcija.
     * Ją iškviečia SasajosStebetojoValdiklis kai
     * užtvirtinamas ryšys su serveriu.
     */
    public void rysysUztvirtintas(NIOSasaja sasaja) {
    }

    /** 
     * Metodas pagrinde skirtas testavimui.Tai SasajosStebetojo funkcija.
     * Ją iškviečia SasajosStebetojoValdiklis kai
     * nutraukiamas ryšys su serveriu.
     */
    public void rysysNutrauktas(NIOSasaja sasaja, Exception isimtis) {

    }

    /** Tai SasajosStebetojo funkcija.
     *  Ji iškviečiama kai gaunamas naujas paketas.
     */
    public void paketasGautas(NIOSasaja sasaja, byte[] paketas) {
        String zinute = new String(paketas, utf8);
        apdorokZinute(zinute);
    }

    /**
     * Metodas skirtas apdoroti gautą žinutę.
     * Pagrinde visur naudojamos startsWith(), substring() ir split() funkcijos.
     * Taip išskiriama programai aktuali informacija ir kategorizuojamos žinutės.
     * Daugiau apie žinučių formatus galima rasti priede Žinučių formatai.
     * @param zinute -> gauta žinutė.
     */
    public void apdorokZinute(String zinute) {
        if (!autentifikuotas) {
            autentifikavimoZinutes(zinute);
        } else {
            if (zinute.startsWith("<K>")) {
                perduokKambariui(zinute.substring(3));
            } else if (zinute.startsWith("<K+>") || zinute.startsWith("<K++>")) {
                System.out.println("Pridedamas naujas kambarys");
                if (zinute.startsWith("<K+>")) m_klientoLangas.sukurkKambarioInterfeisa(zinute.substring(4), false);
                else
                    m_klientoLangas.sukurkKambarioInterfeisa(zinute.substring(5), true);
                pasalinkKambariuLangus();
            } else if (zinute.startsWith("<KP>")) {
                m_svecioPrisijungimas.KeistiKrovimoTeksta("Užbaigiama...", 99);
                m_svecioPrisijungimas.PrisijungimoUzbaigimas(m_vardas);
            } else if (zinute.startsWith("<KS>")) {
                m_klientoLangas.nustatykKambariuSarasa(zinute.substring(4));
            } else if (zinute.startsWith("<EKP>")) {
                m_klientoLangas.gaukPPk().klaida("Jau prisijungta prie šio kambario!");
            } else if (zinute.startsWith("<EKK>")) {
                m_klientoLangas.gaukPPk().klaida("Jus negalie sugrįžti į kambarį iš kurio buvote išspirtas!");
            } else if (zinute.startsWith("<EK+>")) {
                m_klientoLangas.gaukPPk().klaida("Toks kambarys nebegzistuoja!");
            } else if (zinute.startsWith("<NEK>")) {
                m_klientoLangas.gaukKK().klaida("Toks kambarys jau egzistuoja!");
            } else if (zinute.startsWith("<S>")) {
                m_klientoLangas.pranesimas(zinute.substring(3));
            }
        }
    }

    /**
     * Metodas skirtas vien autentifikavimo žinutėms.
     * @param zinute -> gauta žinutė.
     */
    public void autentifikavimoZinutes(String zinute) {
        if (zinute.startsWith("<C1>")) {
            issukis = zinute.substring(4);
        } else if (zinute.startsWith("<C2>")) {
            VartotojoAutentifikacija VA = VartotojoAutentifikacija.gaukVAValdikli();
            zinute = zinute.substring(4);
            try {
                String bandymas = VA.UzkoduokSlaptazodi(m_slaptazodis, zinute, "SHA-256", 0);
                String galutinisSlaptazodis = VA.UzkoduokSlaptazodi(bandymas, issukis, "SHA-512");
                m_sasaja.rasyk(("<R2>" + m_vardas + "<P>" + galutinisSlaptazodis).getBytes());
            } catch (NoSuchAlgorithmException e) {
                m_eventuValdiklis.gaukNIOAptarnavima().ispekApieIsimti(e);
            } catch (DecoderException e) {
                m_eventuValdiklis.gaukNIOAptarnavima().ispekApieIsimti(e);
            }
        } else if (zinute.startsWith("<R+>")) {
            m_svecioPrisijungimas.KeistiKrovimoTeksta("Prijungiama...", 62);
            autentifikuotas = true;
            m_sasaja.rasyk("<KS>".getBytes());
            m_sasaja.rasyk("<KP>".getBytes());
        } else if (zinute.startsWith("<ER>")) {
            m_klientoLangas.atjunkKlienta();
            m_svecioPrisijungimas.klaida("Netinkamas vardas arba slaptazodis!");
        } else if (zinute.startsWith("<EP>")) {
            m_klientoLangas.atjunkKlienta();
            m_svecioPrisijungimas.klaida("Vartotojas jau yra prisijungęs!");
        }
    }

    /**
     * Metodas kuris atsako už visas <K> simbiliu prasidedančias žinutes.
     * @param zinute -> gauta žinutė.
     */
    public void perduokKambariui(String zinute) {
        String kambarioPavadinimas = zinute.split("<")[0];
        KambarioInterfeisas kambarys = m_kambariai.get(kambarioPavadinimas);
        if (kambarys != null) {
            zinute = zinute.substring(kambarioPavadinimas.length());
            if (zinute.startsWith("<V>")) {
                zinute = zinute.substring(3);
                String siuntejas = zinute.split("<Z>")[0];
                siuntejas = siuntejas.contentEquals("NULL") ? null : siuntejas;
                zinute = zinute.split("<Z>")[1];
                kambarys.spausdinkZinute(zinute, siuntejas);
            } else if (zinute.startsWith("<I>")) {
                zinute = zinute.substring(3).trim();
                if (!zinute.isEmpty()) kambarys.spausdintiTeksta(zinute);
            } else if (zinute.startsWith("<V+>")) {
                kambarys.pridekVartotoja(zinute.substring(4));
            } else if (zinute.startsWith("<V->")) {
                kambarys.pasalinkVartotoja(zinute.substring(4), false);
            } else if (zinute.startsWith("<VKK>")) {
                kambarys.pasalinkVartotoja(zinute.substring(5), true);
            } else if (zinute.startsWith("<VS>")) {
                kambarys.nustatykPrisijungusiusVartotojus(zinute.substring(4));
            }
        }
    }

    /**
     * Metodas pašalina Kambario kūrimo ar Prisijungimo prie kambario langus,
     * jei jie yra atidaryti.
     */
    public void pasalinkKambariuLangus() {
        if (m_klientoLangas.gaukKK() != null && m_klientoLangas.gaukKK().isVisible()) {
            m_klientoLangas.gaukKK().pasalink();
        }
        if (m_klientoLangas.gaukPPk() != null && m_klientoLangas.gaukPPk().isVisible()) {
            m_klientoLangas.gaukPPk().pasalink();
        }
    }

    /** 
     * Metodas pagrinde skirtas testavimui.Tai SasajosStebetojo funkcija.
     * Ją iškviečia SasajosStebetojoValdiklis kai
     * paketas sėkmingai išsiunčiamas.
     */
    public void paketasIssiustas(NIOSasaja sasaja, Object zyme) {

    }

    /**
     * Metodas kuris atjungia kliento programos NIOKlientas objektą ir su juo susietą
     * EventuValdiklis objektą.
     */
    public void atsijunk() {
        m_eventuValdiklis.isjunk();
        m_sasaja.uzdaryk();
    }

    /**
     * Metodas į kliento kambarių Map<String, KambarioInterfeisas> kintamajį
     * prideda kambarį nurodytu pavadinimu.
     * Jei pridėti pavyksta, t.y. kambario tokiu pačiu pavadinimu nėra, gražinamas true;
     *
     * @param pavadinimas -> kambario pavadinimas.
     * @param k -> kambario GUI.
     * @return operacijos rezultatas true arba false;
     */
    public boolean pridekKambari(String pavadinimas, KambarioInterfeisas k) {
        if (m_kambariai.containsKey(pavadinimas)) return false;
        m_kambariai.put(pavadinimas, k);
        return true;
    }

    /**
     * Panaikinamas kambarys nurodytu pavadinimu.
     * Pranešama serveriui kad klientas paliko kambarį.
     *
     * @param pavadinimas -> kambario pavadinimas.
     */
    public void panaikinkKambari(String pavadinimas) {
        m_sasaja.rasyk(("<K->" + pavadinimas).getBytes());
        m_kambariai.remove(pavadinimas);
    }

    public static DateFormat gaukDatosForma() {
        return DATOS_FORMA;
    }
}
