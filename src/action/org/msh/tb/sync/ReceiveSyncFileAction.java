/**
 * 
 */
package org.msh.tb.sync;

import java.io.IOException;
import java.io.PrintWriter;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.jboss.seam.annotations.Name;

/**
 * Prepare the answer to the client app that sent the synchronization file. The file is read
 * in the filter {@link SyncFileUploadFilter} (because of SEAM limitations), and
 * this class just prepare the answer with the token ID to be used to follow up
 * the file answer  
 * @author Ricardo Memoria
 *
 */
@Name("receiveSyncFileAction")
public class ReceiveSyncFileAction {

	private boolean isMultipart;
	private String userToken;

	/**
	 * Execute the request of the page
	 * @throws IOException
	 */
	public void execute() throws IOException {
		FacesContext fc = FacesContext.getCurrentInstance();
		
		HttpServletRequest req = (HttpServletRequest)fc.getExternalContext().getRequest(); 
		HttpServletResponse resp = (HttpServletResponse)fc.getExternalContext().getResponse();
		
		receiveFile(req, resp);
	}

	/**
	 * Prepare answer to the client about the file received
	 * @param req
	 * @param resp
	 * @throws IOException
	 */
	protected void receiveFile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		isMultipart = ServletFileUpload.isMultipartContent(req);
		PrintWriter out = resp.getWriter();

		if (!isMultipart) {
			out.print("No file informed");
			return;
		}
		
		resp.setContentType("text/plain");
			// send the token to the client
		out.write("token=" + userToken);
		FacesContext.getCurrentInstance().responseComplete();
	}

	/**
	 * @return the userToken
	 */
	public String getUserToken() {
		return userToken;
	}

	/**
	 * @param userToken the userToken to set
	 */
	public void setUserToken(String userToken) {
		this.userToken = userToken;
	}
}
