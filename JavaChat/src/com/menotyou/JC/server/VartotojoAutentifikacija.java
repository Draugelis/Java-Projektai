package com.menotyou.JC.server;

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

public final class VartotojoAutentifikacija {
	
	private static VartotojoAutentifikacija VA;
	private final static int ITERACIJU_SKAICIUS = 1000;
	private final static int MAX_VARDO_ILGIS = 30;
	 
	private VartotojoAutentifikacija(){
		
	}
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
/*	public boolean vardasUzimtas(Connection con, String vardas){
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
	}*/
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

	public static synchronized VartotojoAutentifikacija gaukVAValdikli(){
		if(VA == null)
			VA = new VartotojoAutentifikacija();
		return VA;
	}
	public String Base64ToString(byte[] B) {
	    return StringUtils.newStringUtf8(Base64.decodeBase64(B));
	}
	public byte[] StringToBase64(String s) {
	    return Base64.encodeBase64(StringUtils.getBytesUtf8(s));
	}
	 public void close(Statement ps) {
	       if (ps!=null){
	           try {
	               ps.close();
	           } catch (SQLException ignore) {
	           }
	       }
	   }
	 
	 public void close(ResultSet rs) {
	     if (rs!=null){
	         try {
	             rs.close();
	           } catch (SQLException ignore) {
	           }
	       }
	   }
}
