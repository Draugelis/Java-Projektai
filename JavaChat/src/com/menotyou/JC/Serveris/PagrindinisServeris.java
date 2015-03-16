package com.menotyou.JC.Serveris;

import java.io.IOException;

import com.menotyou.JC.NIOBiblioteka.NIOServerioSasaja;
import com.menotyou.JC.NIOBiblioteka.PrisijungimuFiltras;
import com.menotyou.JC.NIOBiblioteka.EventuValdiklis.EventuValdiklis;

public class PagrindinisServeris {
	
	private final static int DEFAULT_PORT = 8192;

	public static void main(String[] args) {
		int port;
		if (args.length > 1) {
			System.out.println("Naudojimas: java -jar ChatServer.jar [port] [t/f]- (Command console enabled)");
			return;
		} else if (args.length == 1){
			port = Integer.parseInt(args[0]);
			paleiskServeri(port, false);
		} else {
			System.out.println("No port specified, the server will start on default port:" + DEFAULT_PORT);
			paleiskServeri(DEFAULT_PORT, false);
		}
	}
	
	private static void paleiskServeri(int portas, boolean konsoleIjungta){
		try
        {
			EventuValdiklis eventuValdiklis = new EventuValdiklis();
            NIOServerioSasaja sasaja = eventuValdiklis.gaukNIOAptarnavima().sukurkServerioSasaja(portas);
            sasaja.stebek(new JCServeris(eventuValdiklis));
            sasaja.nustatykPrisijungimuFiltra(PrisijungimuFiltras.LEISK_VISUS);
            eventuValdiklis.start();
        }
        catch (IOException e)
        {
                e.printStackTrace();
        }
	}
}
