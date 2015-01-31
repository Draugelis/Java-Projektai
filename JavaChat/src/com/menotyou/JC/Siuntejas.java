package com.menotyou.JC;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;

public class Siuntejas extends Thread {

	private Vector<String> siunciamuZinuciuEile = new Vector<String>();
	
	
	private PrintWriter rasymas;
	private Klientas klientas;
	
	public Siuntejas(Klientas klientas)
	throws IOException{
		this.klientas = klientas;
		Socket prieiga = klientas.gaukKlientoPrieiga();
		rasymas = new PrintWriter(new OutputStreamWriter(prieiga.getOutputStream()));
	}
	public synchronized void siuskZinute(String zinute){
		siunciamuZinuciuEile.add(zinute);
		notify();
	}
	private synchronized String kitaZinute()
	throws InterruptedException{
		while(siunciamuZinuciuEile.size() == 0)
			wait();
		String zinute = (String) siunciamuZinuciuEile.get(0);
		siunciamuZinuciuEile.remove(0);
		return zinute;
	}
	private void siuskServeriui(String zinute){
		rasymas.println(zinute);
		rasymas.flush();
	}
	public void run()
    {
        try {
           while (!isInterrupted()) {
               String message = kitaZinute();
               siuskServeriui(message);
           }
        } catch (Exception e) {
        	
        }
    }
}
