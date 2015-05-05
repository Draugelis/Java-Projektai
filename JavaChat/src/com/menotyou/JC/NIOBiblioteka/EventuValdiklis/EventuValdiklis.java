package com.menotyou.JC.NIOBiblioteka.EventuValdiklis;

import java.io.IOException;
import java.sql.Date;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import com.menotyou.JC.NIOBiblioteka.IsimciuStebetojas;
import com.menotyou.JC.NIOBiblioteka.NIOAptarnavimas;

/**
 * Klasė aplgėbia visą žinučių pernešimo internetu mechanizmą ir jį valdo bei tvarko.
 * Jai priklauso išorinė įvykiu eilė kurioje išrikiuojami įvykiai,
 * kurie turėtų būti vykdomi asinchroniškai.
 * NIO yra Non-Bloking-Input-Output kitaip sakant IO, kuris neblokuoja
 * proceso ir yra vykdomas tik tam tikrą laiko tarpą. Atsakymas į šį procesą dažnai
 * priimamas kito ciklo apsisukimo metu arba net dar vėliau.
 */
public class EventuValdiklis {

    /** NIOAptarnavimas objektas kuris talpina didžiają dalį.*/
    private final NIOAptarnavimas m_aptarnavimas;

    /** Įvykių eilė. Joje įvykiai yra rikiuojami pagal laiką iki įvykio.*/
    private final Queue<UzdelstasVeiksmas> m_eile;

    /** Procesas kuris vykdo visą NIO. */
    private Thread m_procesas;

    /**
     * Sukuriamas naujas Eventų valdiklis, kuris savo ruožtu sukuria
     * jam priklausantį NIOAptarnavimas objektą ir įvykių eilę.
     *
     * @throws Išmetama IOException tipo išimtis jei kyla klaida kuriant NIOAptarnavimą.
     */
    public EventuValdiklis() throws IOException {
        m_aptarnavimas = new NIOAptarnavimas();
        m_eile = new PriorityBlockingQueue<UzdelstasVeiksmas>();
        m_procesas = null;
    }

    /**
     * Asinchroniskas paleidimas, jis leidžia į įvykių eilę įterpti
     * operacijas kurios turi būti padarytos nedelsiant.
     *
     * @param r -> Runnable objektas, kuris talpina operaciją.
     */
    public void asinchroniskasPaleidimas(Runnable r) {
        vykdytiVeliau(r, 0);
    }

    /**
     * Funkcija skirta vykdyti veiksmą praėjus tam tikram laiko tarpui.
     *
     * @param r ->  Runnable objektas, kuris talpina operaciją.
     * @param uzdelsimasMs -> Laikas iki įvykio paleidimo.
     * @return UždelstasVeiksmas objektas kurį bus galima atšaukti.
     */
    public UzdelstasVeiksmas vykdytiVeliau(Runnable r, long uzdelsimasMs) {
        return eilesVeiksmas(r, uzdelsimasMs + System.currentTimeMillis());
    }

    /**
     * Funkcija grąžianti UždelstasVeiksmas objektą, prieš tai dar jis
     * įterpiamas į įvykių eilę.
     *
     * @param r ->  Runnable objektas, kuris talpina operaciją.
     * @param laikas -> Laikas iki įvykio paleidimo.
     * @return UždelstasVeiksmas objektas kurį bus galima atšaukti.
     */
    private UzdelstasVeiksmas eilesVeiksmas(Runnable r, long laikas) {
        UzdelstasVeiksmas veiksmas = new UzdelstasVeiksmas(r, laikas);
        m_eile.add(veiksmas);
        m_aptarnavimas.pabusk();
        return veiksmas;
    }

    /**
     * Funkcija, kuri nurodo kad įvykis turėtų būti vykdomas nurodytu laiku.
     *
     * @param r ->  Runnable objektas, kuris talpina operaciją.
     * @param data -> Data kada veiksmas turėtų būti vykdomas.
     * @return UždelstasVeiksmas objektas kurį bus galima atšaukti.
     */
    public UzdelstasVeiksmas vykdytiNurodytuLaiku(Runnable r, Date data) {
        return eilesVeiksmas(r, data.getTime());
    }

    /**
     * Nusatomas išimčiu stebėtojas šiam objektui.
     *
     * @param stebetojas ->norimas IsimciuStebetojas objektas.
     */
    public void nustatykPriziuretoja(IsimciuStebetojas stebetojas) {
        gaukNIOAptarnavima().nustatykIsimciuPriziuretoja(stebetojas);
    }

    /**
     * Funkcija grąžiną laiką iki kito įvykio esančio eilėje.
     *
     * @return laikas iki kito įvyko milisekundėmis arba Long.MAX_VALUE.
     */
    public long laikasIkiKitoVeiksmo() {
        UzdelstasVeiksmas veiksmas = m_eile.peek();
        return veiksmas == null ? Long.MAX_VALUE : veiksmas.gaukLaika();
    }

    /**
     * Metodas paleidžia pagrindinę įvykių eilę ir
     * EventuValdiklis pradeda veikti.
     */
    public synchronized void start() {
        if (m_procesas != null) throw new IllegalStateException("Procesas jau paleistas.");
        if (!m_aptarnavimas.atidarytas()) throw new IllegalStateException("Procesas buvo išjungtas.");
        m_procesas = new Thread() {
            public void run() {
                while (m_procesas == this) {
                    try {
                        pasirink();
                    } catch (Throwable e) {
                        if (m_procesas == this) gaukNIOAptarnavima().ispekApieIsimti(e);
                    }
                }
            }

        };
        m_procesas.start();
    }

    /**
     * Metodas sustabdo procesą. Jį galima vėl paleisti su start() metodu.
     */
    public synchronized void sustabdyk() {
        if (m_procesas == null) throw new IllegalStateException("Procesas nėra paleistas.");
        m_procesas = null;
        m_aptarnavimas.pabusk();
    }

    /**
     * Metodas galutinai išjungia EventuValdiklis procesą.
     */
    public synchronized void isjunk() {
        if (m_procesas == null) throw new IllegalStateException("Procesas nėra paleistas.");
        m_aptarnavimas.isjunk();
        sustabdyk();
    }

    /**
     * Pagrindinis metodas kuris vykdo įvykius įvykių eilėje.
     * Jei įvykių eilėje nenusimato jokio artimo įvykio, 
     * procesas užsiblokuoja, t.y. laukia įvesties iš kažkurio kliento.
     * Taip yra efektyviau nei tikrinti ar yra naujas įvykis kiekvieną
     * milisekundę.
     *
     * @throws Tai klaida kuria gali išmesti pasirinkBlokuodamas funkcija.
     */
    private void pasirink() throws Throwable {
        while (laikasIkiKitoVeiksmo() <= System.currentTimeMillis()) {
            try {
                paleiskKitaVeiksma();
            } catch (Throwable t) {
                gaukNIOAptarnavima().ispekApieIsimti(t);
            }
        }
        if (laikasIkiKitoVeiksmo() == Long.MAX_VALUE) {
            m_aptarnavimas.pasirinkBlokuodamas();
        } else {
            long uzdelsimas = laikasIkiKitoVeiksmo() - System.currentTimeMillis();
            m_aptarnavimas.pasirinkBlokuodamas(Math.max(1, uzdelsimas));
        }
    }

    /**
     * Paleidžai kitą veiksmą esantį eilės priekyje.
     */
    private void paleiskKitaVeiksma() {
        m_eile.poll().run();
    }

    public NIOAptarnavimas gaukNIOAptarnavima() {
        return m_aptarnavimas;
    }

    public Queue<UzdelstasVeiksmas> gaukEile() {
        return new PriorityQueue<UzdelstasVeiksmas>(m_eile);
    }

    public int gaukEilesIlgi() {
        return m_eile.size();
    }
}
