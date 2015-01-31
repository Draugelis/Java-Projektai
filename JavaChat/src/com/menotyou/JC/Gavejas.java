package com.menotyou.JC;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Gavejas extends Thread {
	private BufferedReader skaitymas;
	private Klientas klientas;
	
	public Gavejas(Klientas klientas)
	throws IOException{
		this.klientas = klientas;
		Socket prieiga = klientas.gaukKlientoPrieiga();
		skaitymas = new BufferedReader(new InputStreamReader(prieiga.getInputStream()));
	}
	public void run()
	{
		System.out.println("Paleidþiamas gaviklis");
		try{
			while(!isInterrupted()){
				String zinute = skaitymas.readLine();
				if(zinute == null)
					break;
				klientas.priimkZinute(zinute + "/p/"); // /p/ - pabaiga;
			}
		} catch(IOException ioex){
			
		}
	}
	
}
