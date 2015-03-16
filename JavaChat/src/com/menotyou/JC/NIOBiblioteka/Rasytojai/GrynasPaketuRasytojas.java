package com.menotyou.JC.NIOBiblioteka.Rasytojai;

import java.nio.ByteBuffer;

public class GrynasPaketuRasytojas implements PaketuRasytojas {
	
	public static GrynasPaketuRasytojas NUMATYTASIS = new GrynasPaketuRasytojas();
	
	public GrynasPaketuRasytojas(){}

	public ByteBuffer[] rasyk(ByteBuffer[] buferis) {
		return buferis;
	}

}
