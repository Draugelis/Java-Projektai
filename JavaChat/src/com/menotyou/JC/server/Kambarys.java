package com.menotyou.JC.server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class Kambarys extends Thread{
	
	DateFormat datosForma = new SimpleDateFormat("HH:mm:ss");
	
	private Vector<String> zinuciuEile = new Vector<String>();
	private Vector<KlientoDuomenys> klientai = new Vector<KlientoDuomenys>();
	private String kambarioVardas;

	public synchronized void pridekKlienta(KlientoDuomenys kd){
		klientai.add(kd);
		siuskKlientuSarasa();
	}
	public synchronized void nustatykVarda(String vardas){
		this.kambarioVardas = vardas;
	}
	public synchronized String gaukVarda(){
		return kambarioVardas;
	}
	
	public synchronized void pasalinkKlienta(KlientoDuomenys kd, boolean isspirtas){
		int klientoIndeksas = klientai.indexOf(kd);
		if(klientoIndeksas != -1){
			String klientoVardas = kd.gaukVarda();
			klientai.removeElementAt(klientoIndeksas);
			if(!klientoVardas.isEmpty()){
				if(isspirtas)
					siuskZinuteVisiems(suformuokZinute(klientoVardas + " buvo pasalintas is pokalbio.", ""));
				else
					siuskZinuteVisiems(suformuokZinute(klientoVardas + " paliko pokalbá.", ""));
			}
			siuskKlientuSarasa();
		}
		
	}
	public synchronized void apdorokZinute(KlientoDuomenys kd, String zinute){
		if(zinute.startsWith("/PS/")){ //Prisijunge svecias.
			String vardas = zinute.substring(4);
			System.out.println("Prijungiamas vartotojas " + vardas +  ", jam bus iðsiøstas patvirtinimas");
			kd.nustatykVarda(vardas);
			String atsakymas = "/PS/" + vardas;
			kd.klientoSiuntejas.siuskZinute(atsakymas);
			siuskZinuteVisiems(suformuokZinute(vardas + " prisijungë", ""));
		} else {
			zinute = suformuokZinute(zinute, kd.gaukVarda());
			zinuciuEile.add(zinute);
			notify();
		}
	}
	private synchronized String gaukSekanciaZinute()
	throws InterruptedException
	{
		while(zinuciuEile.size() == 0)
			wait();
		String zinute = (String) zinuciuEile.get(0);
		zinuciuEile.removeElementAt(0);
		return zinute;
	}
	private synchronized void siuskKlientuSarasa(){
		String klientuSarasas = "/VS/";
		String klientoVardas;
		if(klientai.size() > 0){
			for (int i = 0; i < klientai.size() - 1; i++){
				klientoVardas = klientai.elementAt(i).gaukVarda();
				if(!klientoVardas.isEmpty()){
					klientuSarasas += (klientoVardas + "/k/");
				}
			}
			klientoVardas = klientai.elementAt(klientai.size()-1).gaukVarda();
			if(!klientoVardas.isEmpty()){
				klientuSarasas += klientoVardas;
			}
			klientuSarasas += "/p/";
			zinuciuEile.add(klientuSarasas);
			notify();
		}
	}
	private synchronized void siuskZinuteVisiems(String zinute)
    {
        for (int i=0; i<klientai.size(); i++) {
           KlientoDuomenys kd = (KlientoDuomenys) klientai.get(i);
           kd.klientoSiuntejas.siuskZinute(zinute);
        }
    }
	private String suformuokZinute(String tekstas, String vardas){
		Date data = new Date();
		String klientoInfo = "";
		if(!vardas.isEmpty())
			klientoInfo = vardas + ": ";
		String zinute = "/V/" + this.kambarioVardas + "/Z/[" + datosForma.format(data) + "] " + klientoInfo + tekstas;
		return zinute;
	}
	
	public void run(){
		try{
			while(true) {
				String zinute = gaukSekanciaZinute();
				siuskZinuteVisiems(zinute);
			}
		} catch (InterruptedException ie){
			
		}
	}
}
