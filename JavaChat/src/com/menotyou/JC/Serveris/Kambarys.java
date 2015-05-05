package com.menotyou.JC.Serveris;

import java.util.ArrayList;
import java.util.Iterator;

import com.menotyou.JC.NIOBiblioteka.EventuValdiklis.UzdelstasIvykis;

/**
 * Klasė atsakinga už vieno kambario vidaus operacijas serverio pusėje.
 */
public class Kambarys {

    /** Laikas iki kammbario bus naudojamas tada kai vartotojas ištrins kambarį, o jame
     * vis dar šnekės žmonės. */
    private final static long LAIKAS_IKI_KAMBARIO_UZDARYMO = 10 * 60 * 1000;

    /** Įvykis kuris bus iškviestas kai klientas ištrins kambarį. */
    private UzdelstasIvykis m_uzdarymoIvykis;

    /** Kambario vartotojų sąrašas. */
    private ArrayList<Vartotojas> m_kambarioVartotojai;

    /** Kambario juodasis sąrašas jame talpinami
     * varotojų, kurie buvo išspirti, id. */
    private ArrayList<Integer> m_juodasisSarasas;
    private String m_kambarioPavadinimas;
    private String m_kambarioPradineZinute;
    private int m_savininkoID;
    private int m_kambarioID;

    /** Kintamasis nusakantis ar kambarys yra uždarytas. */
    private boolean m_uzdarytas;

    /**
     * Tai du Kambarys objekto konstruktoriai.
     *  1. Skiratas kurti kambarį realiu laiku.
     *	2. Skirtas kurti kambarį iš duomenų bazės.
     * Pirmasis kontruktorius kartu prideda jį sukūrusį vartotoją į savo vartotojų sarašą.
     *
     * @param kambarioPradineZinute -> kambario pradinė žinutė.
     * @param kambarioPavadinimas -> kambario pavadinimas
     * @param savininkas -> Vartotojas objektas kuris kuria kambarį.
     * @param kambarioID -> kambario id duomenų bazėje.
     */
    public Kambarys(String kambarioPradineZinute, String kambarioPavadinimas, Vartotojas savininkas, int kambarioID) {
        this(kambarioPradineZinute, kambarioPavadinimas, savininkas.gaukID(), kambarioID);
        pridekKlienta(savininkas);
    }

    public Kambarys(String kambarioPradineZinute, String kambarioPavadinimas, int savininkoID, int kambarioID) {
        System.out.println("Pridedamas naujas kambarys:");
        System.out.println("->>> \' " + kambarioPradineZinute + "\'");
        System.out.println("->>>" + kambarioPavadinimas);
        System.out.println("->>>" + savininkoID);
        System.out.println("->>>" + kambarioID);

        m_kambarioPradineZinute = kambarioPradineZinute;
        m_kambarioPavadinimas = kambarioPavadinimas;
        m_savininkoID = savininkoID;
        m_uzdarytas = false;
        m_kambarioVartotojai = new ArrayList<Vartotojas>();
        m_juodasisSarasas = new ArrayList<Integer>();
        nustatykKambarioID(kambarioID);
    }

    /**
     * Metodas prideda vartotoją į kambario vartotojų sąrašą.
     * Jei Vartotojas objektas == null, užklausa ignoruojama.
     * Užklausa taip pat atmetama jei vartotojas jau yra šiame kambaryje
     * arba juodajame sąraše.
     * Jei prisijungiantis klientas yra kambario savininkas,
     * tai pranešama klientinei programai kodu <K++>.
     * Kitu atvėju - paprastas klientas <K+>.
     *
     * @param vartotojas -> Vartotojas objektas kurį norima pridėti.
     */
    public void pridekKlienta(Vartotojas vartotojas) {
        if (vartotojas == null) return;
        if (m_kambarioVartotojai.contains(vartotojas)) {
            vartotojas.siuskZinute("<EKP>");
            return;
        }
        if (yraJuodajameSarase(vartotojas.gaukID())) {
            System.out.println("Vartotojas " + vartotojas + " yra juodajame saraše. Užklausa atmesta");
            vartotojas.siuskZinute("<EKK>");
            return;
        }
        m_kambarioVartotojai.add(vartotojas);
        vartotojas.papildykKambariuPrisijungimus();
        System.out.println("Klientas: " + vartotojas + " pridėtas <K+>" + m_kambarioPavadinimas);
        if (vartotojas.gaukID() == m_savininkoID) vartotojas.siuskZinute("<K++>" + m_kambarioPavadinimas);
        else
            vartotojas.siuskZinute("<K+>" + m_kambarioPavadinimas);
        siuskVartotojuSarasa(vartotojas);
        siuskVisiems("<V+>" + vartotojas.gaukVarda(), vartotojas);
        vartotojas.siuskZinute(this, "<I>" + m_kambarioPradineZinute);
    }

    /**
     * Metodas išrenka visus kambario vartotojų vardus ir nusiunčia juos vartotojui.
     * Žinutės formatas: <VS>vardas<T>vardas<T>vardas<END>.
     *
     * @param vartotojas -> Vartotojas objektas kuriam bus siunčiamas sąrašas.
     */
    private void siuskVartotojuSarasa(Vartotojas vartotojas) {
        StringBuffer sb = new StringBuffer("<VS>");
        Iterator<Vartotojas> itr = m_kambarioVartotojai.iterator();
        while (itr.hasNext()) {
            sb.append(itr.next().gaukVarda());
            if (itr.hasNext()) sb.append("<T>");
        }
        sb.append("<END>");
        vartotojas.siuskZinute(this, sb.toString());
    }

    public String gaukPavadinima() {
        return m_kambarioPavadinimas;
    }

    /**
     * Gražiną vartotoją susieta su nurodytu vardu,
     * jei jo vardas yra vartotojų sąraše.
     *
     * @param vardas -> vartotojo vardas.
     * @return Vartotojas objektas arba null.
     */
    public Vartotojas gaukVartotoja(String vardas) {
        for (int i = 0; i < m_kambarioVartotojai.size(); i++) {
            if (m_kambarioVartotojai.get(i).gaukVarda().equals(vardas)) return m_kambarioVartotojai.get(i);
        }
        return null;
    }

    /**
     * Išspiriamas klientas. Klientas išspiriamas tik tokiu atvėjui jei:
     *  1. Jį išspiriantis vartotojas yra kambario savininkas.
     *  2. Toks klietas yra vartotojų sąraše.
     *  3. Jis dar nebuvo išspirtas iš šio kambario.
     * Jei vartotojas sėkmingai pašalinamas(jis buvo sąraše), jam išsiunčiama
     * apie tai pranešanti žinutė. Pranešama ir kitiems kambario vartotojams.
     * @param vardas -> Išspiriamojo vardas.
     * @param prasytojas -> Vartotjas kuris prašo išspirti klientą.
     */
    public void isspirkKlienta(String vardas, Vartotojas prasytojas) {
        Vartotojas vartotojas = gaukVartotoja(vardas);
        if (prasytojas.gaukID() == m_savininkoID && vartotojas != null && !yraJuodajameSarase(vartotojas.gaukID())) {
            if (m_kambarioVartotojai.remove(vartotojas)) {
                suplanuokJSPapildyma(vartotojas, Kambarys.this);
                vartotojas.siuskZinute(this, "<I>Jūs buvote išspirtas iš pokalbio.");
                vartotojas.papildykIsspyrimoKartus();
                prasytojas.papilfykIsspyrimus();
                m_juodasisSarasas.add(vartotojas.gaukID());
                siuskVisiems("<VKK>" + vartotojas.gaukVarda(), vartotojas);
            }
        }
    }

    /**
     * Metodas suplanuoja buomenų bazės juodųjų sąrašų papildymą.
     * Šis metodas skirtas tam kad išvengti situacijos kai kambarys dar neturi savo id.
     * Metodas garantuoja, kad duomenų bazė bus papildyta šiuo įrašu tik po to kai
     * kambarys gaus savo id.
     *
     * @param vartotojas -> Vartotojas kurio id ketinama įrašyti į sąrašą.
     * @param kambarys -> Kambarys kurio juodajame saraše turėtų atsidurti vartotojas.
     */
    public void suplanuokJSPapildyma(final Vartotojas vartotojas, final Kambarys kambarys) {
        if (kambarys.gaukKambarioID() == -1) {
            InfoAtnaujintojas.gaukInfoAtnaujintoja().gaukServeri().gaukEventuValdikli().vykdytiVeliau(new Runnable() {
                public void run() {
                    InfoAtnaujintojas.gaukInfoAtnaujintoja().pridekJSIrasa(vartotojas, kambarys);
                }
            }, JCServeris.LAIKAS_IKI_ATNAUJINIMO);
        } else {
            InfoAtnaujintojas.gaukInfoAtnaujintoja().pridekJSIrasa(vartotojas, Kambarys.this);
        }
    }

    /**
     * Metodas pašalinantis klientą reguliariu būdu, kai
     * šis palieką pokalbį ar atsijungia.
     * Tai pranešama kitiems kambario vartotojams.
     *
     * @param vartotojas -> Vartotojas kurį ketinama šalinti.
     */
    public void pasalinkKlienta(Vartotojas vartotojas) {
        if (m_kambarioVartotojai.remove(vartotojas)) {
            System.out.println("Klientas: " + vartotojas + " pašalintas <K-> " + m_kambarioPavadinimas);
            siuskVisiems("<V->" + vartotojas.gaukVarda(), vartotojas);
        }
    }

    /**
     * Metodas kuris apdoroją žinutė ir jei siuntėjas yra juodajame sąraše
     * ar kambarys yra uždarytas žinutės neišsiunčia.
     *
     * @param siuntejas -> Vartotojas iš kurio gauta žinutė.
     * @param zinute -> žinutės tekstas.
     */
    public void apdorokZinute(Vartotojas siuntejas, String zinute) {
        if (m_uzdarytas) return;
        if (yraJuodajameSarase(siuntejas.gaukID())) return;

        System.out.println("Kambarys: " + m_kambarioPavadinimas + " Siuntejas: " + siuntejas + " Žinutė: " + zinute);
        zinute = "<V>" + siuntejas.gaukVarda() + "<Z>" + zinute;
        siuntejas.papildykIssiustasZinutes();
        siuskVisiems(zinute, null);
    }

    /**
     * Metodas išsiunčia žintę visiems kambario vartotojams išskyrus nurodytajį.
     * Jei nurodytas vartotojas == null, žinutė išsiunčiama visiems kambario vartotojams.
     *
     * @param zinute -> siunčiama žinutė.
     * @param vartotojas -> vartotojas, kuriam nesiųsti žinutės.
     */
    public void siuskVisiems(String zinute, Vartotojas vartotojas) {
        for (Vartotojas v : m_kambarioVartotojai)
            if (v != vartotojas) v.siuskZinute(this, zinute);
    }

    /**
     * Metodas atšaukiantis kambario uždarymą.
     * Šiuo metu jis nenaudojamas, bet ateityje gali praverst.
     */
    public void atsaukUzdaryma() {
        m_uzdarymoIvykis.atsaukti();
        siuskVisiems("<I>Kambario uždarymas buvo atšauktas", null);
    }

    /**
     * Metodas paskelbia visiems kambario vartotojams kad kambarys uždaromas praėjus
     * numatytam laiko tarpui.
     *
     * @param serveris -> Serveris kurio kambarį ketinama šalinti.
     */
    public void paskelbkUzdaryma(JCServeris serveris) {
        siuskVisiems("<I>Šis kambarys buvo pašalintas. Po 10 min. jis taps nebeatyviu.\n Jūs dar galite bendrauti tarpusavyje, bet po 10 min, žinutės nebebus perduodamos", null);
        m_uzdarymoIvykis = serveris.gaukEventuValdikli().vykdytiVeliau(new Runnable() {
            public void run() {
                m_uzdarytas = true;
            }
        }, LAIKAS_IKI_KAMBARIO_UZDARYMO);
    }

    public ArrayList<Integer> gaukJuodajiSarasa() {
        return m_juodasisSarasas;
    }

    public void nustatykSavininkoID(int id) {
        nustatykKambarioID(id);
    }

    public boolean yraJuodajameSarase(int id) {
        return m_juodasisSarasas.contains(id) ? true : false;
    }

    public String gaukKambarioZinute() {
        return m_kambarioPradineZinute;
    }

    void nustatykKambarioZinute(String zinute) {
        m_kambarioPradineZinute = zinute;
    }

    public int gaukKambarioSavininkoID() {
        return m_savininkoID;
    }

    public int gaukKambarioID() {
        return m_kambarioID;
    }

    public void nustatykKambarioID(int kambarioID) {
        m_kambarioID = kambarioID;
    }
}
