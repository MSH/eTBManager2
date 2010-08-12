package org.msh.tb.importexport;

import java.io.InputStream;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.contexts.Contexts;
import org.msh.mdrtb.entities.UserLogin;
import org.msh.mdrtb.entities.Workspace;
import org.msh.tb.importexport.ImportDataHome.ImportTable;
import org.msh.tb.misc.DmSystemHome;

/**
 * Execute asynchronous importing of data. This class is used by {@link ImportDataHome} to implement
 * asynchronous importing of data file sent by user. This implementation releases the user interface
 * while importing happens in parallel
 * @author Ricardo Memoria
 *
 */
@Name("importAsyncExecution")
public class ImportAsyncExecution {

	@In(create=true) DmSystemHome dmsystem;
	
	private ImportTable table;
	private ImportBase importBase;
	private String error;
	
	@Asynchronous
	public void execute(ImportTable table, InputStream data, Workspace workspace, UserLogin userLogin) {
		error = null;
		this.table = table;
		
		importBase = null;
		try {
			importBase = (ImportBase)table.getImportClass().newInstance();
			
			if (importBase != null) {
				Contexts.getEventContext().set("userLogin", userLogin);
				importBase.setWorkspace(workspace);
				importBase.execute(data);
			}
		} catch (Exception e) {
			e.printStackTrace();
			error = e.getMessage();
		}
		sendNotificationMail();
	}

	
	/**
	 * Send mail message to the user notifying about the progress of the imported data
	 */
	protected void sendNotificationMail() {
		UserLogin userLogin = (UserLogin)Component.getInstance("userLogin");
		Contexts.getEventContext().set("user", userLogin.getUser());
		dmsystem.enviarEmail("importdata.xhtml");
	}

	
	public String getError() {
		return error;
	}

	
	public ImportTable getTable() {
		return table;
	}

	
	public Integer getLineNumber() {
		return (importBase != null? importBase.getLineNumber(): null);
	}
}
