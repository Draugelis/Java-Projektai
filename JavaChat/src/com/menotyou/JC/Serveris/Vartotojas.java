package com.menotyou.JC.Serveris;

import com.menotyou.JC.NIOBiblioteka.NIOSasaja;
import com.menotyou.JC.NIOBiblioteka.SasajosStebetojas;
import com.menotyou.JC.NIOBiblioteka.EventuValdiklis.UzdelstasIvykis;
import com.menotyou.JC.NIOBiblioteka.Rasytojai.PaprastasPaketuRasytojas;
import com.menotyou.JC.NIOBiblioteka.Skaitytojai.PaprastasPaketuSkaitytojas;

public class Vartotojas implements SasajosStebetojas {
	private final static long PRISIJUNGIMO_LAIKAS = 30 * 1000;
	private final static long MAX_NEVEIKSNUMO_LAIKAS = 20 * 60 * 1000;
	private final static int ANTRASTES_DYDIS = 2;
	private final static boolean BIG_ENDIAN = true;
	private final JCServeris m_serveris;
	private final NIOSasaja m_sasaja;
	private String m_vardas;
	private int m_ID;
	private UzdelstasIvykis m_atsijungimoIvykis;
	public Vartotojas(JCServeris serveris, NIOSasaja sasaja){
		m_serveris = serveris;
		m_sasaja = sasaja;
		m_sasaja.nustatykPaketuRasytoja(new PaprastasPaketuRasytojas(ANTRASTES_DYDIS, BIG_ENDIAN));
		m_sasaja.nustatykPaketuSkaitytoja(new PaprastasPaketuSkaitytojas(ANTRASTES_DYDIS, BIG_ENDIAN));
		m_sasaja.stebek(this);
		m_vardas = null;
		m_ID = -1;
	}

	public void rysysUztvirtintas(NIOSasaja sasaja) {
		m_atsijungimoIvykis = m_serveris.gaukEventuValdikli().vykdytiVeliau(new Runnable(){
			public void run(){
				m_sasaja.rasyk("Prisijungimo laikas baigėsi!".getBytes());
				m_sasaja.uzsidarykPoRasymo();
			}
		}, PRISIJUNGIMO_LAIKAS);
		m_sasaja.rasyk(generuokIssuki());
	}
	public String toString(){
		return m_vardas != null ? m_vardas +"@"+m_sasaja.gaukIp() : "Anonimas@" + m_sasaja.gaukIp();
	}

	public void rysysNutrauktas(NIOSasaja sasaja, Exception isimtis) {
		if(m_vardas != null){
			//Turėtų pranešti visiems..
		}
		sasaja.uzdaryk();
		m_serveris.pasalinkKlienta(this);

	}
	private void paruoskNeveiksnumoIvyki(){
		if(m_atsijungimoIvykis != null) m_atsijungimoIvykis.atsaukti();
		m_atsijungimoIvykis = m_serveris.gaukEventuValdikli().vykdytiVeliau(new Runnable(){
			public void run(){
				m_sasaja.rasyk("Atjungta dėl neveiksnumo.".getBytes());
				m_sasaja.uzsidarykPoRasymo();
			}
		}, MAX_NEVEIKSNUMO_LAIKAS);
	}
	public void paketasGautas(NIOSasaja sasaja, byte[] paketas) {
		System.out.println("Gautas paketas.");
		String zinute = new String(paketas).trim();
		if(zinute.length() == 0) return;
		System.out.println(zinute);
		paruoskNeveiksnumoIvyki();
		apdorokZinute(zinute);
	}
	public void apdorokZinute(String zinute){
		if(m_vardas == null){
			if(zinute.startsWith("<R>")){
				zinute = zinute.substring(3);
				String vardas = zinute.split("<P>")[0];
				String slaptazodis = zinute.split("<P>")[1];
				//TODO realus slaptazodzio patikrinimas
				if(slaptazodis.contentEquals("Challange13")){
					m_sasaja.rasyk(("<R+>" + vardas).getBytes());
				} else {
					m_sasaja.rasyk("<ER>".getBytes());
				}
			}
		} else {
			if(zinute.startsWith("<K>")){
				zinute = zinute.substring(3);
				m_serveris.perduokKambariui(zinute, this);
			} else if(zinute.startsWith("<NK>")){
				zinute = zinute.substring(4);
				String k_pavadinimas;
				Boolean suZinute = false;
				if(zinute.contains("<KZ>")){
					k_pavadinimas = zinute.split("<KZ>")[0];
					suZinute = true;
				} else {
					k_pavadinimas = zinute;
				}
				if(m_serveris.arYraKambarys(k_pavadinimas)){
					m_sasaja.rasyk("<EK+>".getBytes());
				} else {
					if(suZinute){
						m_serveris.pridekKambari(k_pavadinimas, zinute.split("<KZ>")[1], Vartotojas.this);
					} else {
						m_serveris.pridekKambari(k_pavadinimas, null, Vartotojas.this);
					}
				}
			} else if(zinute.startsWith("<K+>")){
				
			} else if(zinute.startsWith("<K->")){
				
			} else if(zinute.startsWith("<Q>")){
				
			}
		}
	}
	private byte[] generuokIssuki(){
		//TODO prideti random byte seka
		return "Challange13".getBytes(m_serveris.KODAVIMO_FORMATAS);
	}

	public void paketasIssiustas(NIOSasaja sasaja, Object zyme) {

	}
	public void siuskZinute(String zinute){
		siuskZinute(zinute.getBytes());
	}
	public void siuskZinute(byte[] zinuteBaitais){
		if(m_vardas != null){
			m_sasaja.rasyk(zinuteBaitais);
		}
	}

}
