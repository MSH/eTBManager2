package org.msh.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Passwords {

	/**
	 * Aplica hash MD5 na senha informada
	 * @param password
	 * @return
	 */
	public static final String hashPassword(String password) {
		MessageDigest md;
		try {
	            md = MessageDigest.getInstance("MD5");
	            md.update(password.getBytes());
	            byte[] hashGerado = md.digest();

	            StringBuffer ret = new StringBuffer(hashGerado.length);
	            for (int i = 0; i < hashGerado.length; i++) {
	            String hex = Integer.toHexString(0x0100 + (hashGerado[i] & 0x00FF)).substring(1);
	            ret.append((hex.length() < 2 ? "0" : "") + hex);
	        }
	        return ret.toString();
	        
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Gera uma nova senha
	 * @return
	 */
	public static final String generateNewPassword() {
		String sen = "";
		String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
		
		Random gen = new Random();
		for (int i = 0; i < 6; i++) {
			int index = gen.nextInt(chars.length());
			sen = sen.concat(chars.substring(index, index+1));
		}
		
		return sen;
	}
}
