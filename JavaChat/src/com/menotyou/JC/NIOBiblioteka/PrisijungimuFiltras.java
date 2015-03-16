package com.menotyou.JC.NIOBiblioteka;

import java.net.InetSocketAddress;


public interface PrisijungimuFiltras {
	
	PrisijungimuFiltras ATMESK_VISUS = new PrisijungimuFiltras(){
		public boolean priimkPrisijungima(InetSocketAddress adresas){
			return false;
		}
	};
	
	PrisijungimuFiltras LEISK_VISUS = new PrisijungimuFiltras(){
		public boolean priimkPrisijungima(InetSocketAddress adresas){
			return true;
		}
	};
	
	public boolean priimkPrisijungima(InetSocketAddress adresas);
}
