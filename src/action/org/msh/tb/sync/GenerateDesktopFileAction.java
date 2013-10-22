/**
 * 
 */
package org.msh.tb.sync;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.ValidationException;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.web.Session;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.webservices.RemoteActionHandler;

/**
 * Home class that get information from the UI and send the initialization file
 * of the desktop client app 
 * @author Ricardo Memoria
 *
 */
@Name("generateDesktopFileAction")
public class GenerateDesktopFileAction {
	/**
	 * Buffer size used when generating file to the stream
	 */
	private static final int BUFFER_SIZE = 65535;

	private Integer unitId;

	/**
	 * Used when a client request the initialization file without authenticating using the login page
	 */
	private String userToken;
	
	@In EntityManager entityManager;

	/**
	 * Generates the initialization file of the unit pointed by the property {@link #unitId} and 
	 * send it to the response servlet as a downloadable file
	 * @throws IOException
	 */
	public void generate() throws IOException {
		// is there any unit selected ?
		if (unitId == null)
			return;

		// get unit to generate file
		Tbunit unit = entityManager.find(Tbunit.class, unitId);
		if (unit == null)
			throw new IllegalAccessError("Unit id not valid: " + unitId);

		File file = File.createTempFile("etbm", ".pkg");
		BufferedOutputStream fout = new BufferedOutputStream( new FileOutputStream(file) );
		try {
			// generate the file
			DesktopIniGeneratorService srv = (DesktopIniGeneratorService)Component.getInstance("desktopIniGeneratorService");
			srv.generateFile(unit, fout);
		} finally {
			fout.close();
		}
		
		// create file name for the unit
		String filename = unit.getName().getName1();
    	filename = filename.replaceAll("[^a-zA-Z0-9.]", "_");
    	if (filename.length() > 10)
    		filename.substring(0, 9);
    	filename += ".etbm.pkg";

		sendFile(file, filename);
		
		// generate the file
//		DesktopIniGeneratorService srv = (DesktopIniGeneratorService)Component.getInstance("desktopIniGeneratorService");
//		srv.generateFile(unit, response.getOutputStream());
		
	}

	
	/**
	 * Send the file to the output response
	 * @param file
	 * @param filename
	 * @throws IOException
	 */
	private void sendFile(File file, String filename) throws IOException {
    	// initialize the servlet response
		FacesContext fc = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Length", String.valueOf(file.length()));  
		response.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");  

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
	}
	
	
	/**
	 * Called from the REST API to generate the download file as a request from a client
	 */
	public void download() {
		if (userToken == null)
			throw new RuntimeException("No user token informed");
		
		RemoteActionHandler handler = new RemoteActionHandler(userToken) {
			@Override
			protected Object execute(Object data) throws ValidationException {
				UserWorkspace uw = (UserWorkspace)Component.getInstance("userWorkspace");
				unitId = uw.getTbunit().getId();
				try {
					generate();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				return null;
			}
		};
		
		handler.setTransactional(false);
		handler.run();
		Session.instance().invalidate();
	}
	
	/**
	 * @return the unitId
	 */
	public Integer getUnitId() {
		return unitId;
	}

	/**
	 * @param unitId the unitId to set
	 */
	public void setUnitId(Integer unitId) {
		this.unitId = unitId;
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
