package org.msh.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.regex.Pattern;

public class UserUtils {

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    // keep it compiled
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

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


    /**
     * Check if e-mail address is valid
     * @param email the e-mail address to be validated
     * @return true if e-mail is valid
     */
    public static final boolean isValidEmail(String email) {
        return pattern.matcher(email).matches();
    }
}
