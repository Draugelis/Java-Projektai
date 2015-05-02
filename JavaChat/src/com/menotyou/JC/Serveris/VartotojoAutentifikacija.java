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

// TODO: Auto-generated Javadoc
/**
 * The Class VartotojoAutentifikacija.
 */
public final class VartotojoAutentifikacija {
	
	/** The va. */
	private static VartotojoAutentifikacija VA;
	
	/** The Constant ITERACIJU_SKAICIUS. */
	private final static int ITERACIJU_SKAICIUS = 1000;
	 
	/**
	 * Instantiates a new vartotojo autentifikacija.
	 */
	private VartotojoAutentifikacija(){
		
	}
	
	/**
	 * Uzkoduok slaptazodi.
	 *
	 * @param slaptazodis the slaptazodis
	 * @param druska the druska
	 * @param algoritmas the algoritmas
	 * @return the string
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws DecoderException the decoder exception
	 */
	public synchronized String UzkoduokSlaptazodi(String slaptazodis, String druska, String algoritmas)
			throws NoSuchAlgorithmException, DecoderException{
		return UzkoduokSlaptazodi(slaptazodis,druska, algoritmas, ITERACIJU_SKAICIUS);
	}
	
	/**
	 * Uzkoduok slaptazodi.
	 *
	 * @param slaptazodis the slaptazodis
	 * @param druska the druska
	 * @param algoritmas the algoritmas
	 * @param It_skaicius the it_skaicius
	 * @return the string
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws DecoderException the decoder exception
	 */
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

	/**
	 * Gauk va valdikli.
	 *
	 * @return the vartotojo autentifikacija
	 */
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
