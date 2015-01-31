package com.menotyou.JC.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;

public class KlientoGaviklis extends Thread {
	private KlientoDuomenys kD;
	private BufferedReader skaitymas;
	private Serveris serveris;
	
	public KlientoGaviklis(KlientoDuomenys klientoDuomenys, Serveris serveris)
	throws IOException{
		kD = klientoDuomenys;
		this.serveris = serveris;
		Socket prieiga = kD.gaukKlientoPrieiga();
		skaitymas = new BufferedReader(new InputStreamReader(prieiga.getInputStream()));
	}
	public void run()
	{
		try{
			while(!isInterrupted()){
				String zinute = skaitymas.readLine();
				if(zinute == null)
					break;
				pirminisApdorojimas(kD, zinute);
			}
		} catch(IOException ioex){
			
		}
		kD.klientoSiuntejas.interrupt();
		serveris.pasalinkKlienta(kD);
	}
	private void pirminisApdorojimas(KlientoDuomenys kD, String zinute){
		String kambarys = zinute.split("/K/|/Z/")[0];
		zinute = "/Z/" + zinute.split("/Z/")[1];
		serveris.gaukKambari(kambarys).apdorokZinute(kD, zinute);
	}
	
	
}
