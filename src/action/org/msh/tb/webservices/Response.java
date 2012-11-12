package org.msh.tb.webservices;

/**
 * Standard response for web services exposed by eTB Manager
 * @author Ricardo Memoria
 *
 */
public class Response {

	public static final int RESP_SUCCESS = 0;
	public static final int RESP_AUTHENTICATION_FAIL = 1;
	public static final int RESP_INVALID_SESSION = 2;
	public static final int RESP_UNEXPECTED_ERROR = 3;
	public static final int RESP_VALIDATION_ERROR = 4;
	
	private Object result;
	private int errorno;
	private String errormsg;

	/**
	 * @return the result
	 */
	public Object getResult() {
		return result;
	}
	/**
	 * @param result the result to set
	 */
	public void setResult(Object result) {
		this.result = result;
	}
	/**
	 * @return the errorno
	 */
	public int getErrorno() {
		return errorno;
	}
	/**
	 * @param errorno the errorno to set
	 */
	public void setErrorno(int errorno) {
		this.errorno = errorno;
	}
	/**
	 * @return the errormsg
	 */
	public String getErrormsg() {
		return errormsg;
	}
	/**
	 * @param errormsg the errormsg to set
	 */
	public void setErrormsg(String errormsg) {
		this.errormsg = errormsg;
	}
}
