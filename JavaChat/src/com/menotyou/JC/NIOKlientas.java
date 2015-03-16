package com.menotyou.JC;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import com.menotyou.JC.NIOBiblioteka.NIOSasaja;
import com.menotyou.JC.NIOBiblioteka.SasajosStebetojas;
import com.menotyou.JC.NIOBiblioteka.EventuValdiklis.EventuValdiklis;
import com.menotyou.JC.NIOBiblioteka.Rasytojai.PaprastasPaketuRasytojas;
import com.menotyou.JC.NIOBiblioteka.Skaitytojai.PaprastasPaketuSkaitytojas;

public class NIOKlientas implements SasajosStebetojas {
	private final static DateFormat DATOS_FORMA = new SimpleDateFormat(
			"HH:mm:ss");
	private final static int ANTRASTES_DYDIS = 2;
	private final static boolean BIG_ENDIAN = true;
	private final EventuValdiklis m_eventuValdiklis;
	private final NIOSasaja m_sasaja;
	private String m_vardas;
	private KlientoLangas m_klientoLangas;
	private SvecioPrisijungimas m_svecioPrisijungimas;
	private final HashMap<String, KambarioInterfeisas> kambariai;

	public NIOKlientas(KlientoLangas kl, SvecioPrisijungimas sp)
			throws IOException {
		m_eventuValdiklis = new EventuValdiklis();
		m_sasaja = m_eventuValdiklis.gaukNIOAptarnavima().sukurkSasaja(
				"localhost", 8192);
		m_svecioPrisijungimas = sp;
		m_klientoLangas = kl;
		kambariai = new HashMap<String, KambarioInterfeisas>();
		m_sasaja.nustatykPaketuRasytoja(new PaprastasPaketuRasytojas(
				ANTRASTES_DYDIS, BIG_ENDIAN));
		m_sasaja.nustatykPaketuSkaitytoja(new PaprastasPaketuSkaitytojas(
				ANTRASTES_DYDIS, BIG_ENDIAN));
		m_sasaja.stebek(this);
	}

	public void start() {
		m_eventuValdiklis.start();
	}

	public void siuskZinute(String zinute) {
		byte[] zinuteBaitais = zinute.getBytes();
		m_sasaja.rasyk(zinuteBaitais);
	}

	public void rysysUztvirtintas(NIOSasaja sasaja) {
		System.out.println("Rysys su serveriu uzmegstas");
	}

	public void rysysNutrauktas(NIOSasaja sasaja, Exception isimtis) {
		// TODO Auto-generated method stub
	}

	public void paketasGautas(NIOSasaja sasaja, byte[] paketas) {
		String zinute = new String(paketas);
		apdorokZinute(zinute);
	}

	public void apdorokZinute(String zinute) {
		if (m_vardas == null) {
			if(zinute.startsWith("<C>")){
				//TODO Challange priemimas
			} else if (zinute.startsWith("<R+>")) {
				m_vardas = zinute.substring(4);
				m_sasaja.rasyk("<K+>Pagrindinis".getBytes());
				// TODO Perduoti prisijungimo langui jog pavyko prisijungti
			} else if (zinute.startsWith("ER")) {
				// TODO Perduoti prisijungimo langui jog nepavyko prisijungti
			}
		} else {
			if(zinute.startsWith("<K>")){
				zinute = zinute.substring(3);
				String kambarioPavadinimas = zinute.split("<")[0];
				KambarioInterfeisas kambarys = kambariai.get(kambarioPavadinimas);
				if(kambarys == null){
					System.out.println("Kambarys pavadinimu " + kambarioPavadinimas + " neegzistuoja");
				} else{
					zinute = zinute.substring(kambarioPavadinimas.length());
					if(zinute.startsWith("<V>")){
						zinute = zinute.substring(3);
						String siuntejas = zinute.split("<Z>")[0];
						zinute = zinute.split("<Z>")[1];
						kambarys.spausdinkZinute(zinute, siuntejas);
					}
				}
			}
		}
	}

	public void paketasIssiustas(NIOSasaja sasaja, Object zyme) {
		System.out.println("Zinute buvo issiusta");
	}

	public void atsijunk() {
		m_eventuValdiklis.isjunk();
	}

	public boolean pridekKambari(String pavadinimas, KambarioInterfeisas k) {
		if (kambariai.containsKey(pavadinimas))
			return false;
		kambariai.put(pavadinimas, k);
		return true;
	}
}
