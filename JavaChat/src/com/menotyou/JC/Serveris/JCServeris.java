package com.menotyou.JC.Serveris;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import com.menotyou.JC.NIOBiblioteka.NIOSasaja;
import com.menotyou.JC.NIOBiblioteka.ServerioSasajosStebetojas;
import com.menotyou.JC.NIOBiblioteka.EventuValdiklis.EventuValdiklis;

public class JCServeris implements ServerioSasajosStebetojas{
	
	public final Charset KODAVIMO_FORMATAS = Charset.forName("UTF-8");
	private final static String NUMATYTA_KAMBARIO_ZINUTE = "Sveiki prisijungę! Visą reikalingą informaciją rasite skyriuje Pagalba!";
	private final EventuValdiklis m_eventuValdiklis;
	private final ArrayList<Vartotojas> m_vartotojai;
	private final HashMap<String, Kambarys> m_kambariai;
	public JCServeris(EventuValdiklis eventuValdiklis){
		m_eventuValdiklis = eventuValdiklis;
		m_vartotojai = new ArrayList<Vartotojas>();
		m_kambariai = new HashMap<String, Kambarys>();
		pridekKambari("Pagrindinis", NUMATYTA_KAMBARIO_ZINUTE, null);
	}

	public void priemimasNepavyko(IOException isimtis) {
		System.out.println("Nepavyko priimti prisijungimo: " + isimtis);
	}

	public void serverioSasajaMire(Exception isimtis) {
        System.out.println("Serverio sąsaja mire.");
        System.exit(-1);
	}
	public boolean arYraKambarys(String pavadinimas){
		return m_kambariai.containsKey(pavadinimas);
	}
	public void pridekKambari(String pavadinimas, String pradineZinute, Vartotojas vartotojas){
		m_kambariai.put(pavadinimas, new Kambarys(pavadinimas, pradineZinute, vartotojas));
		
	}
	public void perduokKambariui(String zinute, Vartotojas siuntejas){
		String kambarioPavadinimas = zinute.split("<V>")[0];
		zinute = zinute.substring(kambarioPavadinimas.length());
		Kambarys kambarys = m_kambariai.get(kambarioPavadinimas);
		System.out.println("Perduodama kambariui: " + kambarioPavadinimas + "Siuntejas: " + siuntejas);
		if(kambarys == null) return;
		kambarys.apdorokZinute(siuntejas, zinute);
	}

	public void naujasSujungimas(NIOSasaja sasaja) {
		System.out.println("Prisijunge naujas vartotojas iš" + sasaja.gaukIp() + ".");
		m_vartotojai.add(new Vartotojas(this, sasaja));
	}
	public EventuValdiklis gaukEventuValdikli(){
		return m_eventuValdiklis;
	}
	
	public void siuskVisiemsVartotojams(String zinute){
		byte[] zinuteBaitais = zinute.getBytes();
		for(Vartotojas vartotojas : m_vartotojai){
			vartotojas.siuskZinute(zinuteBaitais);
		}
	}
	public void pasalinkKlienta(Vartotojas vartotojas){
		m_vartotojai.remove(vartotojas);
	}

	public int gaukVartotojuSkaiciu() {
		return m_vartotojai.size();
	}
	
	

}
