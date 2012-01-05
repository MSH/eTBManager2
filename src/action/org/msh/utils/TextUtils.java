package org.msh.utils;

import java.util.StringTokenizer;

public class TextUtils {

	public interface TokenInterpreter {
		String translate(String token);
	}
	
	/**
	 * Recognizes all occurrences of delimited el expressions #{token} and call {@link TokenInterpreter}
	 * to translate token
	 * @param text
	 * @param interp
	 * @return
	 */
	public static String eltokenFormat(String text, TokenInterpreter interp) {
		StringTokenizer tokens = new StringTokenizer(text, "#}", true);
		StringBuilder res = new StringBuilder(text.length());

		while (tokens.hasMoreTokens()) {
			String token = tokens.nextToken();
			if ("#".equals(token) && tokens.hasMoreTokens()) {
				String expressionToken = tokens.nextToken();
				String endToken = tokens.nextToken();
				
				if (!expressionToken.startsWith("{") || !endToken.equals("}")) {
					res.append(token).append(expressionToken + endToken);
				} else {
					String fieldName = expressionToken.substring(1);
					String s = interp.translate(fieldName);
					if (s == null) {
						res.append("#{" + fieldName + "}");
					}
					else {
						res.append(s);
					}
				}    
			} else {
				res.append(token);
			}
		}
		
		return res.toString();
	}
}
