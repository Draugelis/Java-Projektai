package com.menotyou.JC;

import java.net.Socket;

public class Klientas{
	private boolean prisijunges = false;
	private String vardas = "";
	private String kambarioPavadinimas = "";
	private String[] prisijungusiuVartotojuSarasas;
	private KlientoLangas klientoLangas;
	private Socket klientoPrieiga = null;
	
	
   	public Gavejas gavejas = null;
   	public Siuntejas siuntejas = null;

	public Klientas(Socket s) {
		klientoPrieiga = s;
	}
	
	public void susiekSuKlientu(KlientoLangas kl){
		klientoLangas = kl;
	}

	public Socket gaukKlientoPrieiga() {
		return klientoPrieiga;
	}
	public String[] gaukVartotojuSarasa(){
		return prisijungusiuVartotojuSarasas;
	}
		
	public void priimkZinute(String zinute){
		if(zinute.startsWith("/Z/") && prisijunges){
			zinute = zinute.substring(3);
			zinute = zinute.split("/p/")[0];
			klientoLangas.papildykIstorija(zinute);
		} else if(zinute.startsWith("/VS/")){
			System.out.println("Gautas vartotoju sarasas");
			if(zinute.startsWith("/VS//p/")){
				prisijungusiuVartotojuSarasas = null;
			} else {
				prisijungusiuVartotojuSarasas = zinute.split("/VS/|/k/|/p/");
			}
		} else if(zinute.startsWith("/PS/")){
			System.out.println("This it team leader! Contact established! Get ready to engage!");
			vardas = zinute.split("/PS/|/p/")[1];
			prisijunges = true;
		}
	}

	public void atsijunk() {
		try{
			gavejas.interrupt();
			siuntejas.interrupt();
			klientoPrieiga.close();
		} catch(Exception e){}
	}
}