//http://inetjava.sourceforge.net/
//http://inetjava.sourceforge.net/lectures/part1_sockets/InetJava-1.9-Chat-Client-Server-Example.html
//http://codereview.stackexchange.com/questions/10365/socket-listener-for-chat-client
//http://stackoverflow.com/questions/3695082/java-server-to-handle-multiple-tcp-connections
//http://cs.lmu.edu/~ray/notes/javanetexamples/
//http://www.dreamincode.net/forums/topic/217076-multithreaded-server-multiple-clients/
//http://javadeveloperszone.wordpress.com/2013/04/20/java-tcp-chat-multiple-client/
//http://stackoverflow.com/questions/14771564/accepting-multiple-clients-in-java-tcp
//http://www.javaprogrammingforums.com/java-networking/11070-1-server-multiple-clients-program.html
//http://www.java2s.com/Code/Java/Tiny-Application/FontChooser.htm
//http://rdeshapriya.com/a-singleton-java-class-for-mysql-db-connection/
//http://www.java2s.com/Code/Java/Tiny-Application/Afontselectiondialog.htm

package com.menotyou.JC.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class Serveris {
	
	private final Charset UTF8_CHARSET = Charset.forName("UTF-8");
	
	private int port;
	private boolean running = false;
	private ServerSocket ServerioPrisijungimas = null;
	private ArrayList<Kambarys> kambariai = new ArrayList<Kambarys>();
	
	public Serveris(int port){
		this.port = port;
		try {
			ServerioPrisijungimas = new ServerSocket(port);
			System.out.println("Serveris paleistas per " + port + " porta");
			running = true;
		} catch (IOException e) {
			System.out.println("Nepavyko paleisti serverio per " + port + " portà");
			e.printStackTrace();
			return;
		}
		Kambarys PagrindinisKamb = new Kambarys();
		PagrindinisKamb.nustatykVarda("Pagrindinis");
		PagrindinisKamb.start();
		kambariai.add(PagrindinisKamb);

        while (running) {
           try {
        	   System.out.println("Laukiama vartotoju");
               Socket prisijungimoSasaja = ServerioPrisijungimas.accept();
               System.out.println("Prisijunge vartotojas is " + prisijungimoSasaja.getInetAddress() + ":" + prisijungimoSasaja.getPort());
               KlientoDuomenys kD = new KlientoDuomenys();
               kD.nustatykPrieiga(prisijungimoSasaja);
               KlientoGaviklis kG = new KlientoGaviklis(kD, this);
               KlientoSiuntejas kS = new KlientoSiuntejas(kD, this);
               kD.klientoGaviklis = kG;
               kD.klientoSiuntejas = kS;
               kG.start();
               kS.start();
               PagrindinisKamb.pridekKlienta(kD);
           } catch (IOException ioe) {
               ioe.printStackTrace();
           }
        }
	}
	public synchronized void sukurkKambari(String vardas, KlientoDuomenys kd){
		Kambarys naujasKambarys = new Kambarys();
		naujasKambarys.nustatykVarda(vardas);
		naujasKambarys.pridekKlienta(kd);
		naujasKambarys.start();
		kambariai.add(naujasKambarys);
	}
	public synchronized void pasalinkKlienta(KlientoDuomenys kd){
		for(int i = 0; i < kambariai.size(); i++)
			kambariai.get(i).pasalinkKlienta(kd, false);
	}
	public synchronized Kambarys gaukKambari(String kambarys){
		for(int i = 0; i < kambariai.size(); i++)
			if(kambariai.get(i).gaukVarda().matches(kambarys))
				return kambariai.get(i);
		return null;
	}
}
