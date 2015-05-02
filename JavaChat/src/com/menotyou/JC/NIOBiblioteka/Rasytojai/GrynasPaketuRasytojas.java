package com.menotyou.JC.NIOBiblioteka.Rasytojai;

import java.nio.ByteBuffer;

// TODO: Auto-generated Javadoc
/**
 * The Class GrynasPaketuRasytojas.
 */
public class GrynasPaketuRasytojas implements PaketuRasytojas {
	
	/** The numatytasis. */
	public static GrynasPaketuRasytojas NUMATYTASIS = new GrynasPaketuRasytojas();
	
	/**
	 * Instantiates a new grynas paketu rasytojas.
	 */
	public GrynasPaketuRasytojas(){}

	/* (non-Javadoc)
	 * @see com.menotyou.JC.NIOBiblioteka.Rasytojai.PaketuRasytojas#rasyk(java.nio.ByteBuffer[])
	 */
	public ByteBuffer[] rasyk(ByteBuffer[] buferis) {
		return buferis;
	}

}
