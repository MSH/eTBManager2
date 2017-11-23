/**
 * 
 */
package org.msh.tb.sync.actions;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * Standard abstract action class to make it easier to return some 
 * useful resources when constructing the answer by yourself
 * @author Ricardo Memoria
 *
 */
public abstract class StandardAction {
	/**
	 * Buffer size used when generating file to the stream
	 */
	private static final int BUFFER_SIZE = 65535;

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

	
	/**
	 * Send the file to the output response. This is the response, and no other data
	 * must be sent
	 * @param file
	 * @param filename
	 * @throws IOException
	 */
	protected void sendFile(File file, String filename) {
    	// initialize the servlet response
		FacesContext fc = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Length", String.valueOf(file.length()));  
		response.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");  

		try {
			BufferedInputStream input = null;
			BufferedOutputStream output = null;
			try {
				input = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE);
				output = new BufferedOutputStream(response.getOutputStream(), BUFFER_SIZE);
	            byte[] buffer = new byte[BUFFER_SIZE];  
	            int length;  
	            while ((length = input.read(buffer)) > 0) {  
	                    output.write(buffer, 0, length);
	            }
	        } finally {
				input.close();
				output.close();
			}
			fc.responseComplete();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
