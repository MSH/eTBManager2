/**
 * 
 */
package org.msh.tb.sync;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Tbunit;

/**
 * Home class that get information from the UI and send the initialization file
 * of the desktop client app 
 * @author Ricardo Memoria
 *
 */
@Name("generateDesktopFileHome")
public class GenerateDesktopFileHome {

	private Integer unitId;
	
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
		
		// create file name for the unit
		String filename = unit.getName().getName1();
    	filename = filename.replaceAll("[^a-zA-Z0-9.]", "_");
    	if (filename.length() > 10)
    		filename.substring(0, 9);
    	filename += ".etbm.pkg";

    	// initialize the servlet response
		FacesContext fc = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");  

		// generate the file
		DesktopIniGeneratorService srv = (DesktopIniGeneratorService)Component.getInstance("desktopIniGeneratorService");
		srv.generateFile(unit, response.getOutputStream());
		
		fc.responseComplete();
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
}
