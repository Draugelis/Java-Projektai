package com.menotyou.JC.Serveris;

import java.util.ArrayList;
import java.util.Iterator;

import com.menotyou.JC.NIOBiblioteka.EventuValdiklis.UzdelstasIvykis;

// TODO: Auto-generated Javadoc
/**
 * The Class Kambarys.
 */
public class Kambarys {

	/** The Constant LAIKAS_IKI_KAMBARIO_UZDARYMO. */
	private final static long LAIKAS_IKI_KAMBARIO_UZDARYMO = 10*60*1000;
	
	/** The m_uzdarymo ivykis. */
	private UzdelstasIvykis m_uzdarymoIvykis;
	
	/** The m_kambario vartotojai. */
	private ArrayList<Vartotojas> m_kambarioVartotojai;
	
	/** The m_juodasis sarasas. */
	private ArrayList<Integer> m_juodasisSarasas;
	
	/** The m_kambario pavadinimas. */
	private String m_kambarioPavadinimas;
	
	/** The m_kambario pradine zinute. */
	private String m_kambarioPradineZinute;
	
	/** The sinchronizuotas. */
	private boolean sinchronizuotas;
	
	/** The m_savininko id. */
	private int m_savininkoID;
	
	/** The m_kambario id. */
	private int m_kambarioID;
	
	/** The m_uzdarytas. */
	private boolean m_uzdarytas;
	
	/**
	 * Instantiates a new kambarys.
	 *
	 * @param kambarioPradineZinute the kambario pradine zinute
	 * @param kambarioPavadinimas the kambario pavadinimas
	 * @param savininkas the savininkas
	 * @param kambarioID the kambario id
	 */
	public Kambarys(String kambarioPradineZinute, String kambarioPavadinimas, Vartotojas savininkas, int kambarioID){
		this(kambarioPradineZinute, kambarioPavadinimas, savininkas.gaukID(), kambarioID);
		pridekKlienta(savininkas);
	}

	/**
	 * Instantiates a new kambarys.
	 *
	 * @param kambarioPradineZinute the kambario pradine zinute
	 * @param kambarioPavadinimas the kambario pavadinimas
	 * @param savininkoID the savininko id
	 * @param kambarioID the kambario id
	 */
	public Kambarys(String kambarioPradineZinute, String kambarioPavadinimas, int savininkoID, int kambarioID) {
		System.out.println("Pridedamas naujas kambarys:");
		System.out.println("->>> \' "+ kambarioPradineZinute + "\'");
		System.out.println("->>>"+ kambarioPavadinimas);
		System.out.println("->>>"+ savininkoID);
		System.out.println("->>>"+ kambarioID);
		
		m_kambarioPradineZinute = kambarioPradineZinute;
		m_kambarioPavadinimas = kambarioPavadinimas;
		m_savininkoID = savininkoID;
		m_uzdarytas = false;
		m_kambarioVartotojai = new ArrayList<Vartotojas>();
		m_juodasisSarasas = new ArrayList<Integer>();
		nustatykKambarioID(kambarioID);
	}

	/**
	 * Pridek klienta.
	 *
	 * @param vartotojas the vartotojas
	 */
	public void pridekKlienta(Vartotojas vartotojas) {
		if (vartotojas == null) return;
		if (m_kambarioVartotojai.contains(vartotojas)) {
			vartotojas.siuskZinute("<EKP>");
			return;
		}
		if(yraJuodajameSarase(vartotojas.gaukID())){
			System.out.println("Vartotojas " + vartotojas + " yra juodajame saraše. Užklausa atmesta");
			vartotojas.siuskZinute("<EKK>");
			return;
		}
		m_kambarioVartotojai.add(vartotojas);
		vartotojas.papildykKambariuPrisijungimus();
		System.out.println("Klientas: " + vartotojas + " pridėtas <K+>" + m_kambarioPavadinimas);
		if(vartotojas.gaukID() == m_savininkoID)
			vartotojas.siuskZinute("<K++>" + m_kambarioPavadinimas);
		else 
			vartotojas.siuskZinute("<K+>" + m_kambarioPavadinimas);
		siuskVartotojuSarasa(vartotojas);
		siuskVisiems("<V+>" + vartotojas.gaukVarda(), vartotojas);
		vartotojas.siuskZinute(this, "<I>" + m_kambarioPradineZinute);
	}

	/**
	 * Siusk vartotoju sarasa.
	 *
	 * @param vartotojas the vartotojas
	 */
	private void siuskVartotojuSarasa(Vartotojas vartotojas) {
		StringBuffer sb = new StringBuffer("<VS>");
		Iterator<Vartotojas> itr = m_kambarioVartotojai.iterator();
		while(itr.hasNext()){
			sb.append(itr.next().gaukVarda());
			if (itr.hasNext())
				sb.append("<T>");
		}
		sb.append("<END>");
		vartotojas.siuskZinute(this, sb.toString());
	}
	
	/**
	 * Ar sinchorinzuotas.
	 *
	 * @return true, if successful
	 */
	public boolean arSinchorinzuotas(){
		return sinchronizuotas;
	}
	
	/**
	 * Pazymek kad sinchronizuotas.
	 */
	public void pazymekKadSinchronizuotas() {
		sinchronizuotas = true;
	}

	/**
	 * Gauk pavadinima.
	 *
	 * @return the string
	 */
	public String gaukPavadinima() {
		return m_kambarioPavadinimas;
	}
	
	/**
	 * Gauk vartotoja.
	 *
	 * @param vardas the vardas
	 * @return the vartotojas
	 */
	public Vartotojas gaukVartotoja(String vardas){
		for(int i = 0; i < m_kambarioVartotojai.size(); i++){
			if(m_kambarioVartotojai.get(i).gaukVarda().equals(vardas))
				return m_kambarioVartotojai.get(i);
		}
		return null;
	}
	
	/**
	 * Isspirk klienta.
	 *
	 * @param vardas the vardas
	 * @param prasytojas the prasytojas
	 */
	public void isspirkKlienta(String vardas, Vartotojas prasytojas){
		Vartotojas vartotojas = gaukVartotoja(vardas);
		if(prasytojas.gaukID() == m_savininkoID && vartotojas != null && !yraJuodajameSarase(vartotojas.gaukID())){
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
	 * Suplanuok js papildyma.
	 *
	 * @param vartotojas the vartotojas
	 * @param kambarys the kambarys
	 */
	public void suplanuokJSPapildyma(final Vartotojas vartotojas, final Kambarys kambarys){
		if(kambarys.gaukKambarioID() == -1){
			InfoAtnaujintojas.gaukInfoAtnaujintoja().gaukServeri().gaukEventuValdikli().vykdytiVeliau(new Runnable(){
				public void run(){
					InfoAtnaujintojas.gaukInfoAtnaujintoja().pridekJSIrasa(vartotojas, kambarys);
				}
			}, JCServeris.LAIKAS_IKI_ATNAUJINIMO);
		} else{
			InfoAtnaujintojas.gaukInfoAtnaujintoja().pridekJSIrasa(vartotojas, Kambarys.this);
		}
	}

	/**
	 * Pasalink klienta.
	 *
	 * @param vartotojas the vartotojas
	 */
	public void pasalinkKlienta(Vartotojas vartotojas) {
		if (m_kambarioVartotojai.remove(vartotojas)) {
			System.out.println("Klientas: " + vartotojas + " pašalintas <K-> "+ m_kambarioPavadinimas);
			siuskVisiems("<V->" + vartotojas.gaukVarda(), vartotojas);
		}
	}

	/**
	 * Apdorok zinute.
	 *
	 * @param siuntejas the siuntejas
	 * @param zinute the zinute
	 */
	public void apdorokZinute(Vartotojas siuntejas, String zinute) {
		if(m_uzdarytas) return;
		if(yraJuodajameSarase(siuntejas.gaukID())){
			return;
		} else {
			System.out.println("Kambarys: " + m_kambarioPavadinimas+ " Siuntejas: " + siuntejas + " Žinutė: " + zinute);
			zinute = "<V>" + siuntejas.gaukVarda() + "<Z>" + zinute;
			siuntejas.papildykIssiustasZinutes();
			siuskVisiems(zinute, null);
		}
	}
	
	/**
	 * Siusk visiems.
	 *
	 * @param zinute the zinute
	 * @param vartotojas the vartotojas
	 */
	public void siuskVisiems(String zinute, Vartotojas vartotojas){
		for (Vartotojas v : m_kambarioVartotojai)
			if (v != vartotojas)
				v.siuskZinute(this, zinute);
	}
	
	/**
	 * Atsauk uzdaryma.
	 */
	public void atsaukUzdaryma(){
		m_uzdarymoIvykis.atsaukti();
		siuskVisiems("<I>Kambario uždarymas buvo atšauktas", null);
	}
	
	/**
	 * Paskelbk uzdaryma.
	 *
	 * @param serveris the serveris
	 */
	public void paskelbkUzdaryma(JCServeris serveris){
		siuskVisiems("<I>Šis kambarys buvo pašalintas, ir po 10 min taps nebeatyviu.\n Jūs dar galite bendrauti tarpusavyje, bet po 10 min, žinutės nebebus perduodamos", null);
		m_uzdarymoIvykis = serveris.gaukEventuValdikli().vykdytiVeliau(new Runnable() {
		public void run() {
			m_uzdarytas = true;
		}
		}, LAIKAS_IKI_KAMBARIO_UZDARYMO);
	}
	
	/**
	 * Gauk juodaji sarasa.
	 *
	 * @return the array list
	 */
	public ArrayList<Integer> gaukJuodajiSarasa(){
		return m_juodasisSarasas;
	}
	
	/**
	 * Nustatyk savininko id.
	 *
	 * @param id the id
	 */
	public void nustatykSavininkoID(int id){
		nustatykKambarioID(id);
	}
	
	/**
	 * Yra juodajame sarase.
	 *
	 * @param id the id
	 * @return true, if successful
	 */
	public boolean yraJuodajameSarase(int id){
		return m_juodasisSarasas.contains(id) ? true:false;
	}
	
	/**
	 * Gauk kambario zinute.
	 *
	 * @return the string
	 */
	public String gaukKambarioZinute(){
		return m_kambarioPradineZinute;
	}
	
	/**
	 * Nustatyk kambario zinute.
	 *
	 * @param zinute the zinute
	 */
	public void nustatykKambarioZinute(String zinute){
		m_kambarioPradineZinute = zinute;
	}

	/**
	 * Gauk kambario savininko id.
	 *
	 * @return the int
	 */
	public int gaukKambarioSavininkoID(){
		return m_savininkoID;
	}
	
	/**
	 * Gauk kambario id.
	 *
	 * @return the int
	 */
	public int gaukKambarioID() {
		return m_kambarioID;
	}

	/**
	 * Nustatyk kambario id.
	 *
	 * @param kambarioID the kambario id
	 */
	public void nustatykKambarioID(int kambarioID) {
		m_kambarioID = kambarioID;
	}
}
