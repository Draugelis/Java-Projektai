package com.menotyou.JC;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;
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
	
	/** The Constant MAX_VARDO_ILGIS. */
	private final static int MAX_VARDO_ILGIS = 30;
	 
	/**
	 * Instantiates a new vartotojo autentifikacija.
	 */
	private VartotojoAutentifikacija(){
		
	}
	
	/**
	 * Autentifikuok vartotoja.
	 *
	 * @param con the con
	 * @param slaptazodis the slaptazodis
	 * @param vardas the vardas
	 * @return true, if successful
	 * @throws SQLException the SQL exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public boolean autentifikuokVartotoja(Connection con, String slaptazodis, String vardas)
			throws SQLException, NoSuchAlgorithmException, IOException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			boolean VartotojasEgzistuoja = true;
			if(vardas == null || slaptazodis == null){
				VartotojasEgzistuoja = false;
				vardas = "";
				slaptazodis= "";
			}
			ps = con.prepareStatement("SELECT slaptazodis, druska FROM Prisijungimai WHERE vardas = ?");
			ps.setString(1, vardas);
			rs = ps.executeQuery();
			String druska, uzzsifruotasSlaptazodis;
			if(rs.next()){
				uzzsifruotasSlaptazodis = rs.getString("slaptazodis");
				druska = rs.getString("druska");
				if(uzzsifruotasSlaptazodis == null || druska == null){
					throw new SQLException("Duomenu bazeje truksta duomenu, nerasta druska ir slaptazodis");
				}
				if(rs.next()){
					throw new SQLException("Duomenu bazej rasti du vartotojai tokiais paciais vardais");
				}
			}else{
				uzzsifruotasSlaptazodis = "000000000000000000000000000=";
	            druska = "00000000000=";
	            VartotojasEgzistuoja= false;
			}
			byte[] SlaptazodisBitais =  StringToBase64(uzzsifruotasSlaptazodis);
			byte[] druskaBitais = StringToBase64(druska);
			
			byte[] pasiulytasSlaptazodisBitais = UzkoduokSlaptazodi(slaptazodis, druskaBitais);
			
			return Arrays.equals(SlaptazodisBitais, pasiulytasSlaptazodisBitais) && VartotojasEgzistuoja;
		}finally{
			close(rs);
			close(ps);
		}
	}
	
	/**
	 * Vardas uzimtas.
	 *
	 * @param con the con
	 * @param vardas the vardas
	 * @return true, if successful
	 */
	public boolean vardasUzimtas(Connection con, String vardas){
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = con.prepareStatement("SELECT Vartotojo_ID FROM Prisijungimai WHERE Vardas = ?");
			ps.setString(1, vardas);
			rs = ps.executeQuery();
			if(rs.next())
				return false;
		} catch(SQLException e){
			System.out.println("Nepavyko patikrinti vartotojo vardo validumo");
			e.printStackTrace();
		}finally{
			close(ps);
			close(rs);
		}
		return true;
	}
	
	/**
	 * Sukurk vartotoja.
	 *
	 * @param con the con
	 * @param vardas the vardas
	 * @param slaptazodis the slaptazodis
	 * @return true, if successful
	 * @throws SQLException the SQL exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 */
	public boolean sukurkVartotoja(Connection con, String vardas, String slaptazodis)
			throws SQLException, NoSuchAlgorithmException{
		PreparedStatement ps = null;
		try{
			if(vardas != null && slaptazodis != null && vardas.length() <= MAX_VARDO_ILGIS){
				SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
				
				byte [] bDruska = new byte[8];
				random.nextBytes(bDruska);
				byte[] bSlaptazodis = UzkoduokSlaptazodi(slaptazodis, bDruska);
				String sSlaptazodis = Base64ToString(bSlaptazodis);
				String sDruska = Base64ToString(bDruska);
				
				ps = con.prepareStatement("INSERT INTO Prisijungimai (Vardas, Slaptazodis, Druska) VALUES (?, ?, ?)");
				ps.setString(1, vardas);
				ps.setString(2, sSlaptazodis);
				ps.setString(3, sDruska);
				ps.executeUpdate();
				return true;
			}else{
				return false;
			}
		}finally{
			close(ps);
		}
	}
	
	/**
	 * Uzkoduok slaptazodi.
	 *
	 * @param slaptazodis the slaptazodis
	 * @param druska the druska
	 * @return the byte[]
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 */
	private synchronized byte[] UzkoduokSlaptazodi(String slaptazodis, byte[] druska)
			throws NoSuchAlgorithmException{
		MessageDigest digest = MessageDigest.getInstance("SHA-1");
		digest.reset();
		digest.update(druska);
		byte[] ivestis = null;
		try {
			 ivestis = digest.digest(slaptazodis.getBytes("UTF-8"));
			 for(int i = 0; i < ITERACIJU_SKAICIUS; i++){
				 digest.reset();
				 ivestis = digest.digest(ivestis);
			 }
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return ivestis;
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
	
	/**
	 * Base64 to string.
	 *
	 * @param B the b
	 * @return the string
	 */
	public String Base64ToString(byte[] B) {
	    return StringUtils.newStringUtf8(Base64.decodeBase64(B));
	}
	
	/**
	 * String to base64.
	 *
	 * @param s the s
	 * @return the byte[]
	 */
	public byte[] StringToBase64(String s) {
	    return Base64.encodeBase64(StringUtils.getBytesUtf8(s));
	}
	 
 	/**
 	 * Close.
 	 *
 	 * @param ps the ps
 	 */
 	public void close(Statement ps) {
	       if (ps!=null){
	           try {
	               ps.close();
	           } catch (SQLException ignore) {
	           }
	       }
	   }
	 
	 /**
 	 * Close.
 	 *
 	 * @param rs the rs
 	 */
 	public void close(ResultSet rs) {
	     if (rs!=null){
	         try {
	             rs.close();
	           } catch (SQLException ignore) {
	           }
	       }
	   }
}
