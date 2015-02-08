package com.menotyou.JC.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Vector;

public class ServerioKlientas extends Thread {

	private Socket prieiga;
	private String vardas;
	private Serveris serveris;
	private PrintWriter rasymas;
	private BufferedReader skaitymas;
	private Vector<String> zinuciuEile = new Vector<String>();

	public ServerioKlientas(Socket prieiga, Serveris serveris) throws IOException {
		this.prieiga = prieiga;
		this.serveris = serveris;
		rasymas = new PrintWriter(new OutputStreamWriter(prieiga.getOutputStream()));
		skaitymas = new BufferedReader(new InputStreamReader(prieiga.getInputStream()));
	}

	public boolean Prisijungimas() {
		String eilute;
		try {
			eilute = skaitymas.readLine();
			String vardas = eilute.split("/V/|/S/|/p/")[0];
			String slaptazodis = eilute.split("/V/|/S/|/p/")[1];
			try {
				if (VartotojoAutentifikacija.gaukVAValdikli().autentifikuokVartotoja(serveris.gaukDBprieiga(), slaptazodis, vardas)) {
					
					rasymas.println("OK");
					this.vardas = vardas;
					return true;
				} else {
					rasymas.println("Netinkamas vardas ar slaptaþodis!");
				}
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void run() {
		try {
			while (!isInterrupted()) {
				if(!skaitymas.ready()){
					if(zinuciuEile.size() > 0){
						rasymas.println(zinuciuEile.get(0));
						zinuciuEile.remove(0);
					}
				} else{
					String zinute = skaitymas.readLine();
					pirminisApdorojimas(zinute);
				}
			}
		} catch (Exception e) {
		}
	}
	public synchronized void siuskZinute(String zinute){
		zinuciuEile.add(zinute);
	}
	public String gaukVarda(){
		return vardas;
	}
	
	public synchronized void atsijunk(){
		try {
			this.prieiga.close();
			this.interrupt();
		} catch (IOException e) {
		}
	}
	private void pirminisApdorojimas(String zinute){
		if(zinute.startsWith("/K/")){
			String kambarys = zinute.split("/K/|/Z/")[0];
			zinute = zinute.split("/Z/")[1];
			Kambarys k = serveris.gaukKambari(kambarys);
			if(k != null)
				k.apdorokZinute(this, zinute);
		}
	}

}
