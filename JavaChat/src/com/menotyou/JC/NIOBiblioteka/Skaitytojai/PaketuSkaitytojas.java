package com.menotyou.JC.NIOBiblioteka.Skaitytojai;

import java.nio.ByteBuffer;

import com.menotyou.JC.NIOBiblioteka.ProtokoloPazeidimoIsimtis;

public interface PaketuSkaitytojas {
	public static byte[] PRALEISK_PAKETA = new byte[0];
	
	byte[] kitasPaketas(ByteBuffer byteBuffer) throws ProtokoloPazeidimoIsimtis;
}
