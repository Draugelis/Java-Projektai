package com.menotyou.JC.server;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;

public class KlientoSiuntejas extends Thread {

	private Vector<String> siunciamuZinuciuEile = new Vector<String>();
	
	private Serveris serveris;
	private KlientoDuomenys kD;
	private PrintWriter rasymas;
	
	public KlientoSiuntejas(KlientoDuomenys klientoDuomenys, Serveris serveris)
	throws IOException{
		kD = klientoDuomenys;
		this.serveris = serveris;
		Socket prieiga = kD.gaukKlientoPrieiga();
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
	private void siuskKlientui(String zinute){
		rasymas.println(zinute);
		rasymas.flush();
	}
	public void run()
    {
        try {
           while (!isInterrupted()) {
               String message = kitaZinute();
               siuskKlientui(message);
           }
        } catch (Exception e) {
        }
        kD.klientoGaviklis.interrupt();
    }
}
