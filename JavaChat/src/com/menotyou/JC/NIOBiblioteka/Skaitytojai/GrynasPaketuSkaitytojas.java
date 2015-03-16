package com.menotyou.JC.NIOBiblioteka.Skaitytojai;

import java.nio.ByteBuffer;

import com.menotyou.JC.NIOBiblioteka.ProtokoloPazeidimoIsimtis;

public class GrynasPaketuSkaitytojas implements PaketuSkaitytojas {

	public final static GrynasPaketuSkaitytojas NUMATYTASIS = new GrynasPaketuSkaitytojas();
	
	private GrynasPaketuSkaitytojas(){
		
	}
	
	public byte[] kitasPaketas(ByteBuffer buferis) throws ProtokoloPazeidimoIsimtis {
		byte[] paketas = new byte[buferis.remaining()];
		buferis.get(paketas);
		return paketas;
	}

}
