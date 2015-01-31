package com.menotyou.JC.server;

import java.awt.List;
import java.net.Socket;
import java.util.ArrayList;

public class KlientoDuomenys {
	
	private String vardas = "";
	private Socket klientoPrieiga = null;
    public KlientoGaviklis klientoGaviklis = null;
    public KlientoSiuntejas klientoSiuntejas = null;
    public ArrayList<Kambarys> kambariai = new ArrayList<Kambarys>();

    public String gaukVarda(){
    	return vardas;
    }
    public void nustatykVarda(String vardas){
    	this.vardas = vardas;
    }
    
    public Socket gaukKlientoPrieiga(){
    	return klientoPrieiga;
    }
    public void nustatykPrieiga(Socket s){
    	klientoPrieiga = s;
    }
}
