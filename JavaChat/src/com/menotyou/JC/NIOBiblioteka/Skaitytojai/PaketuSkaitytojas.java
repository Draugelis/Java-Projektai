package com.menotyou.JC.NIOBiblioteka.Skaitytojai;

import java.nio.ByteBuffer;

import com.menotyou.JC.NIOBiblioteka.ProtokoloPazeidimoIsimtis;

// TODO: Auto-generated Javadoc
/**
 * The Interface PaketuSkaitytojas.
 */
public interface PaketuSkaitytojas {
	
	/** The praleisk paketa. */
	public static byte[] PRALEISK_PAKETA = new byte[0];
	
	/**
	 * Kitas paketas.
	 *
	 * @param byteBuffer the byte buffer
	 * @return the byte[]
	 * @throws ProtokoloPazeidimoIsimtis the protokolo pazeidimo isimtis
	 */
	byte[] kitasPaketas(ByteBuffer byteBuffer) throws ProtokoloPazeidimoIsimtis;
}
