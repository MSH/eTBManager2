/**
 * 
 */
package org.msh.tb.sync;

import java.io.PrintWriter;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Standard abstract action class to make it easier to return some 
 * useful resources when constructing the answer by yourself
 * @author Ricardo Memoria
 *
 */
public abstract class StandardAction {

	/**
	 * Execute the action
	 */
	public void execute() {
		generateResponse();
		completeResponse();
	}
	
	protected abstract void generateResponse();
	
	/**
	 * Return the request of the action
	 * @return {@link HttpServletRequest} instance
	 */
	public HttpServletRequest getRequest() {
		FacesContext fc = FacesContext.getCurrentInstance();
		return (HttpServletRequest)fc.getExternalContext().getRequest();
	}
	
	
	/**
	 * Return the response object of the action
	 * @return instance of {@link HttpServletResponse} class
	 */
	public HttpServletResponse getResponse() {
		FacesContext fc = FacesContext.getCurrentInstance();
		return (HttpServletResponse)fc.getExternalContext().getResponse();
	}
	
	/**
	 * Return a write object for the given response
	 * @return
	 */
	public PrintWriter getWriter() {
		PrintWriter out;
		try {
			out = getResponse().getWriter();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return out;
	}
	
	/**
	 * Inform JSF that the response was already done
	 */
	public void completeResponse() {
		FacesContext.getCurrentInstance().responseComplete();
	}
}
