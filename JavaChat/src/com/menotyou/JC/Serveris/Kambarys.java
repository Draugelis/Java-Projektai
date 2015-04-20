package com.menotyou.JC.Serveris;

import java.util.ArrayList;

public class Kambarys {

	private ArrayList<Vartotojas> m_kambarioVartotojai;
	private String m_kambarioPavadinimas;
	private String m_kambarioPradineZinute;
	private Vartotojas m_kambarioSavininkas;

	public Kambarys(String kambarioPradineZinute, String kambarioPavadinimas) {
		m_kambarioPradineZinute = kambarioPradineZinute;
		m_kambarioPavadinimas = kambarioPavadinimas;
		m_kambarioSavininkas = null;
		m_kambarioVartotojai = new ArrayList<Vartotojas>();

	}

	public Kambarys(String kambarioPradineZinute, String kambarioPavadinimas,
			Vartotojas savininkas) {
		m_kambarioPradineZinute = kambarioPradineZinute;
		m_kambarioPavadinimas = kambarioPavadinimas;
		m_kambarioSavininkas = savininkas;
		m_kambarioVartotojai = new ArrayList<Vartotojas>();
		pridekKlienta(savininkas);
	}

	public void pridekKlienta(Vartotojas vartotojas) {
		if (vartotojas == null) return;
		if (m_kambarioVartotojai.contains(vartotojas)) {
			vartotojas.siuskZinute("<EKP>");
			return;
		}
		m_kambarioVartotojai.add(vartotojas);
		System.out.println("Klientas: " + vartotojas + " pridėtas <K+>"
				+ m_kambarioPavadinimas);
		vartotojas.siuskZinute("<K+>" + m_kambarioPavadinimas);
		siuskVartotojuSarasa(vartotojas);
		for (Vartotojas v : m_kambarioVartotojai)
			if (v != vartotojas)
				v.siuskZinute(this, "<V+>" + vartotojas.gaukVarda());
		vartotojas.siuskZinute(this, "<I>" + m_kambarioPradineZinute);
	}

	private void siuskVartotojuSarasa(Vartotojas vartotojas) {
		StringBuffer sb = new StringBuffer("<VS>");
		for (int i = 0; i < m_kambarioVartotojai.size() - 1; i++)
			sb.append(m_kambarioVartotojai.get(i).gaukVarda() + "<T>");
		sb.append(m_kambarioVartotojai.get(m_kambarioVartotojai.size() - 1)
				.gaukVarda() + "<END>");
		vartotojas.siuskZinute(this, sb.toString());
	}

	public String gaukPavadinima() {
		return m_kambarioPavadinimas;
	}

	public void pasalinkKlienta(Vartotojas vartotojas) {
		if (m_kambarioVartotojai.remove(vartotojas)) {
			System.out.println("Klientas: " + vartotojas + " pašalintas <K-> "+ m_kambarioPavadinimas);
			for (Vartotojas v : m_kambarioVartotojai)
				if (v != vartotojas)
					v.siuskZinute(this, "<V->" + vartotojas.gaukVarda());
		} else {
			System.out.println("Nepavyko pasalinti vartotojo " + vartotojas);
		}
	}

	public void apdorokZinute(Vartotojas vartotojas, String zinute) {
		System.out.println("Kambarys: " + m_kambarioPavadinimas+ " Siuntejas: " + vartotojas + " Žinutė: " + zinute);
		zinute = "<V>" + vartotojas.gaukVarda() + "<Z>" + zinute;
		for (Vartotojas v : m_kambarioVartotojai)
			v.siuskZinute(this, zinute);
	}
}
