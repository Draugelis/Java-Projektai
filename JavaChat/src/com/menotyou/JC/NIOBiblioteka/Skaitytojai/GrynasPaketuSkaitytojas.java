package com.menotyou.JC.NIOBiblioteka.Skaitytojai;

import java.nio.ByteBuffer;

import com.menotyou.JC.NIOBiblioteka.ProtokoloPazeidimoIsimtis;

// TODO: Auto-generated Javadoc
/**
 * The Class GrynasPaketuSkaitytojas.
 */
public class GrynasPaketuSkaitytojas implements PaketuSkaitytojas {

	/** The Constant NUMATYTASIS. */
	public final static GrynasPaketuSkaitytojas NUMATYTASIS = new GrynasPaketuSkaitytojas();
	
	/**
	 * Instantiates a new grynas paketu skaitytojas.
	 */
	private GrynasPaketuSkaitytojas(){
		
	}
	
	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.Skaitytojai.PaketuSkaitytojas#kitasPaketas(java.nio.ByteBuffer)
	 */
	public byte[] kitasPaketas(ByteBuffer buferis) throws ProtokoloPazeidimoIsimtis {
		byte[] paketas = new byte[buferis.remaining()];
		buferis.get(paketas);
		return paketas;
	}

}
