package com.menotyou.JC.NIOBiblioteka;

import java.net.Socket;
import java.nio.channels.SocketChannel;

import com.menotyou.JC.NIOBiblioteka.Rasytojai.PaketuRasytojas;
import com.menotyou.JC.NIOBiblioteka.Skaitytojai.PaketuSkaitytojas;


public interface NIOSasaja extends NIOAbstraktiSasaja {
	
	boolean rasyk(byte[] paketas);
	
	
	boolean rasyk(byte[] paketas, Object zyme);
	
	void pridekIEile(Runnable r);
	
	long gaukNuskaitytuBaituSkaiciu();
	
	long gaukParasytuBaituSkaiciu();
	
	long gaukLaikaNuoSujungimo();
	
	long gaukRasymoEilesDydi();
	
	int gaukMaxEilesIlgi();
	
	void nustatykMaxEilesIlgi(int maxEilesIlgis);
	
	void nustatykPaketuSkaitytoja(PaketuSkaitytojas paketuSkaitytojas);
	
	void nustatykPaketuRasytoja(PaketuRasytojas paketuRasytojas);
	
	void stebek(SasajosStebetojas stebetojas);
	
	void uzsidarykPoRasymo();
	
	Socket gaukSasaja();
	
}
