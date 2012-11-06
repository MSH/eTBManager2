package org.msh.tb.webservices;

/**
 * Standard response for web services exposed by eTB Manager
 * @author Ricardo Memoria
 *
 */
public class Response {

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
