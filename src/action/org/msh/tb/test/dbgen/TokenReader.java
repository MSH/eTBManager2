package org.msh.tb.test.dbgen;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Reads the input stream in token data 
 * @author Ricardo Memoria
 *
 */
public class TokenReader {

	private InputStream textFile;
	private int lineNumber;
	private int colNumber;
	private String textLine;
	private TokenType tokenType;
	private String token;
	private Scanner scanner;

	
	/**
	 * Initialize the reader to start reading a new document
	 */
	public void initialize() {
		scanner = new Scanner(textFile);
		lineNumber = 0;
		textLine = null;
		tokenType = null;
	}


	/**
	 * Moves to the next token
	 */
	public boolean nextToken() {
		if (tokenType != TokenType.EOF) {
			tokenType = null;
			token = null;
			return true;
		}
		return false;
	}


	/**
	 * Returns the current token type (or reads the first one if no token was read)
	 * @return - Token type
	 */
	protected TokenType getTokenType() {
		if (tokenType == null)
			readToken();
		return tokenType;
	}


	/**
	 * Return the current token (or read the first one if no token was read)
	 * @return - Token
	 */
	protected String getToken() {
		if (token == null)
			readToken();
		return token;
	}

	
	/**
	 * Reads a token from the input stream
	 */
	protected void readToken() {
		if (textLine == null)
			nextLine();

		token = "";

		// se não tem próxima linha é porque chegou ao final do arquivo
		if (textLine == null) {
			tokenType = TokenType.EOF;
			return;
		}
		
		if (colNumber >= textLine.length()) {
			if (tokenType != TokenType.EOL) {
				tokenType = TokenType.EOL;
				return;
			}
			nextLine();
		}
		
		if (tokenType == TokenType.EOF)
			return;
		
		byte[] bytes = textLine.getBytes();
		
		// skips white spaces and control chars
		while ((colNumber < textLine.length()) && (bytes[colNumber] <= 32))
			colNumber++;
		
		if (colNumber >= textLine.length()) {
			tokenType = TokenType.EOL;
			return;
		}
		
		char c = textLine.charAt(colNumber);
		
		// is digit?
		if (Character.isDigit(c)) {
			int ini = colNumber;
			while ((colNumber < textLine.length()) && (Character.isDigit(textLine.charAt(colNumber))))
				colNumber++;
			token = textLine.substring(ini, colNumber).trim();
			tokenType = TokenType.NUMBER;
			return;
		}
		
		// is string?
		if (isCharString(c, true)) {
			int ini = colNumber;
			colNumber++;
			if (colNumber < textLine.length()) {
				c = textLine.charAt(colNumber);
				boolean b = false;
				while ((colNumber < textLine.length()) && (isCharString(textLine.charAt(colNumber), b))) {
					if (textLine.charAt(colNumber) <= 32)
						b = true;
					else b = false;
					colNumber++;
				}
			}
			tokenType = TokenType.STRING;
			token = textLine.substring(ini, colNumber).trim();
			return;
		}
		
		// it is an operator
		int ini = colNumber;
		if ((colNumber < textLine.length()) && (bytes[colNumber] == (byte)'\'')) {
			colNumber++;
		}
		else
		while ((colNumber < textLine.length()) && (bytes[colNumber] > 32) && 
				(!Character.isLetter(textLine.charAt(colNumber))) &&
				(!Character.isDigit(textLine.charAt(colNumber))))
			colNumber++;

		tokenType = TokenType.OPERATOR;
		token = textLine.substring(ini, colNumber).trim();		
	}


	/**
	 * Skip chars in the same line until find the string s
	 * @param s
	 * @return true if it was found, otherwise false if it wasn't found in the line
	 */
/*	protected boolean skipTilStringInLine(String s) {
		int len = s.length();
		
		while (!textLine.substring(colNumber, len).equals(s)) {
			colNumber++;
			if (colNumber >= textLine.length()) {
				token = "";
				tokenType = TokenType.EOL;
				return false;
			}
		}		
		return true;
	}
*/	
	
	
	/**
	 * Checks if is a char string
	 * @param c - char to check
	 * @param firstChar - true if it's the first char in the string or false if it's a char in the body of string 
	 * @return - true if it's a char string
	 */
	protected boolean isCharString(char c, boolean firstChar) {
		return (firstChar? Character.isLetter(c) :
				(Character.isLetterOrDigit(c) || (c == '_') || ((byte)c <= 32)));
	}
	
	/**
	 * Reads the next line of text
	 */
	protected void nextLine() {
		if (scanner.hasNextLine()) {
			textLine = scanner.nextLine();
			tokenType = null;
			token = null;
			lineNumber++;
			colNumber = 0;
		}
		else {
			textLine = null;
			token = null;
			tokenType = TokenType.EOF;
		}
	}
	
	/**
	 * @return the textFile
	 */
	public InputStream getTextFile() {
		return textFile;
	}

	/**
	 * @param textFile the textFile to set
	 */
	public void setTextFile(InputStream textFile) {
		this.textFile = textFile;
	}

	/**
	 * @return the line number
	 */
	public int getLineNumber() {
		return lineNumber;
	}


	/**
	 * Expects a specific token of a specific type. If the token and type is wrong, raise a TokenReaderException
	 * @param token
	 * @param type
	 * @throws TokenReaderException 
	 */
	public void nextExpected(String token, TokenType type) throws TokenReaderException {
		nextToken();
		if (type != TokenType.EOL)
			while (getTokenType() == TokenType.EOL)
				nextToken();

		if ((type != tokenType) || (this.token == null) || (!this.token.equals(token)))
			throw new TokenReaderException("Expected " + token + " of type " + type);
	}

	
	/**
	 * Expects a specific token. If the token is not the expects, raises a TokenReaderException
	 * @param token - The expected token
	 * @throws TokenReaderException - if next token is different
	 */
	public void nextExpected(String token) throws TokenReaderException {
		nextToken();
		while (getTokenType() == TokenType.EOL)
			nextToken();
		
		if ((this.token == null) || (!this.token.equals(token)))
			throw new TokenReaderException("Expected " + token);
	}

	
	public void expectTokenType(TokenType type) throws TokenReaderException {
		if (getTokenType() != type)
			throwExpectedException(type.toString());
	}

	/**
	 * Expects a string token. If the token is not a string token, throws an exception
	 * @return - String token
	 * @throws TokenReaderException
	 */
	public String expectString() throws TokenReaderException {
		if (getTokenType() != TokenType.STRING)
			throwTokenReaderException(getToken() + " not expected. String expected");
		String s = getToken();
		nextToken();
		return s;
	}
	
	public void expectToken(String token) throws TokenReaderException {
		if ((getToken() == null) || (!getToken().equals(token)))
			throwTokenReaderException(getToken() + " not expected. " + token + " expected");
		nextToken();
	} 
	
	public void throwTokenReaderException(String msg) throws TokenReaderException {
		throw new TokenReaderException("Line " + lineNumber + ": " + msg);
	}
	
	public void throwExpectedException(String token) throws TokenReaderException {
		throwTokenReaderException(token + " expected");
	}


	/**
	 * Reads a string with support for ' as delimiters
	 * @return - String or null if is an invalid string
	 */
	public String readString(String stringDelimiter) {
		String token = getToken();
		String s;
		if (token.equals(stringDelimiter)) {
			int posini = colNumber;

			int pos = textLine.indexOf(stringDelimiter, colNumber);
			if (pos < colNumber)
				return null;
			
			s = textLine.substring(posini, pos);
			colNumber = pos + 1;
			nextToken();

		}
		else {
			if (getTokenType() != TokenType.STRING)
				return null;
			s = getToken();
			nextToken();
		}
		
		return s;
	}
	
	/**
	 * Checks if the current token is equals to the token argument
	 * @param token - Token to be compared with the current token
	 * @return true - if token is the same, false - otherwise
	 */
	public boolean isToken(String token) {
		return ((getToken() != null) && (getToken().equals(token)));
	}
}
