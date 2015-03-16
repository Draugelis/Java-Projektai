package com.menotyou.JC.Serveris;

import java.util.ArrayList;

public class Kambarys{
	
	private ArrayList<Vartotojas> m_kambarioVartotojai;
	private String m_kambarioPavadinimas;
	private String m_kambarioPradineZinute;
	private Vartotojas m_kambarioSavininkas;

	public Kambarys(String kambarioPradineZinute, String kambarioPavadinimas){
		m_kambarioPradineZinute = kambarioPradineZinute;
		m_kambarioPavadinimas = kambarioPavadinimas;
		m_kambarioSavininkas = null;
		
	}
	public Kambarys(String kambarioPradineZinute, String kambarioPavadinimas, Vartotojas savininkas){
		m_kambarioPradineZinute = kambarioPradineZinute;
		m_kambarioPavadinimas = kambarioPavadinimas;
		m_kambarioSavininkas = savininkas;
	}
	public synchronized void pridekKlienta(Vartotojas vartotojas){
		m_kambarioVartotojai.add(vartotojas);
		vartotojas.siuskZinute(m_kambarioPradineZinute);
	}
	public String gaukPavadinima(){
		return m_kambarioPavadinimas;
	}
	
	public void pasalinkKlienta(Vartotojas vartotojas){
		
		
	}
	public synchronized void apdorokZinute(Vartotojas sk, String zinute){
		
	}
	
	
}
