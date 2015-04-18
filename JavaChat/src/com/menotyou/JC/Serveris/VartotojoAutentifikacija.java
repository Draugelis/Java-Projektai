package com.menotyou.JC.Serveris;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;

public final class VartotojoAutentifikacija {
	
	private static VartotojoAutentifikacija VA;
	private final static int ITERACIJU_SKAICIUS = 1000;
	 
	private VartotojoAutentifikacija(){
		
	}
	public synchronized String UzkoduokSlaptazodi(String slaptazodis, String druska, String algoritmas)
			throws NoSuchAlgorithmException, DecoderException{
		return UzkoduokSlaptazodi(slaptazodis,druska, algoritmas, ITERACIJU_SKAICIUS);
	}
	public synchronized String UzkoduokSlaptazodi(String slaptazodis, String druska, String algoritmas, int It_skaicius)
			throws NoSuchAlgorithmException, DecoderException{
		MessageDigest digest = MessageDigest.getInstance(algoritmas);
		digest.reset();
		//digest.update(Hex.decodeHex(druska.toCharArray()));
		byte[] ivestis = null;
		try {
			 ivestis = digest.digest((slaptazodis+druska).getBytes("UTF-8"));
			 for(int i = 0; i < It_skaicius; i++){
				 digest.reset();
				 ivestis = digest.digest(ivestis);
			 }
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return Hex.encodeHexString(ivestis);
	}

	public static synchronized VartotojoAutentifikacija gaukVAValdikli(){
		if(VA == null)
			VA = new VartotojoAutentifikacija();
		return VA;
	}
	/*public String Base64ToString(byte[] B) {
	    return StringUtils.newStringUtf8(Base64.encodeBase64(B, false));
	}
	public byte[] StringToBase64(String s) {
	    return Base64.encodeBase64(s.getBytes());
	}*/
}
