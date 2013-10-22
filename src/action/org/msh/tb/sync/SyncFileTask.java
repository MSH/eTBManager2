/**
 * 
 */
package org.msh.tb.sync;

import java.io.File;
import java.util.Date;

import org.jboss.seam.annotations.Name;
import org.msh.tb.application.App;
import org.msh.tb.application.tasks.AsyncTaskImpl;
import org.msh.tb.entities.ClientSyncResult;

/**
 * @author Ricardo Memoria
 *
 */
@Name("syncFileImporter")
public class SyncFileTask extends AsyncTaskImpl {

	private File file;
	private String token;
	private String errorMessage;
	
	/** {@inheritDoc}
	 */
	@Override
	protected void starting() {
		file = (File)getParameter("file");
		if (file == null)
			throw new RuntimeException("File not informed");
		token = (String)getParameter("token");
	}

	/** {@inheritDoc}
	 */
	@Override
	protected void execute() {
		try {
			for (int i = 1; i <= 10; i++) {
				System.out.println("Running step " + i + " of 10");
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		errorMessage = "This is just a test";

/*		SyncFileImporter importer = new SyncFileImporter();
		importer.start(file);

		errorMessage = importer.getErrorMessage();
		if (errorMessage != null)
			return;
*/
		// TODO generate answer file
	}

	/**
	 * 
	 */
	private void saveStatus() {
		ClientSyncResult resp = new ClientSyncResult();
		resp.setId(token);
		resp.setErrorMessage(errorMessage);
		resp.setSyncStart(getExecutionTimestamp());
		resp.setSyncEnd(new Date());
		App.getEntityManager().persist(resp);
	}

	/** {@inheritDoc}
	 */
	@Override
	protected void finishing() {
		saveStatus();
	}

	/** {@inheritDoc}
	 */
	@Override
	public String getDisplayName() {
		return "Importing " + file.getName();
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}
}
