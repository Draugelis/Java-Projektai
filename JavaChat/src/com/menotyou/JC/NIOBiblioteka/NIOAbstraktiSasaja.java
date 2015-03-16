package com.menotyou.JC.NIOBiblioteka;
import java.net.InetSocketAddress;


public interface NIOAbstraktiSasaja {


	void uzdaryk();
	
	InetSocketAddress gaukAdresa();
	
	boolean atidarytas();
	
	String gaukIp();
	
	int gaukPorta();
	
	Object gaukZyme();
	
	void nustatykZyme(Object zyme);
}
