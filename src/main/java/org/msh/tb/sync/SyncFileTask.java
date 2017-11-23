/**
 * 
 */
package org.msh.tb.sync;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.msh.tb.application.App;
import org.msh.tb.application.tasks.AsyncTaskImpl;
import org.msh.tb.entities.ClientSyncResult;
import org.msh.tb.entities.UserWorkspace;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

/**
 * @author Ricardo Memoria
 *
 */
@Name("syncFileImporter")
public class SyncFileTask extends AsyncTaskImpl {

	private File file;
	private String token;
	private String errorMessage;
	private UserWorkspace userWorkspace;
	
	/** {@inheritDoc}
	 */
	@Override
	protected void starting() {
		file = (File)getParameter("file");
		if (file == null)
			throw new RuntimeException("File not informed");
		token = (String)getParameter("token");
		
		userWorkspace = (UserWorkspace)getParameter("userWorkspace");
	}

	/** {@inheritDoc}
	 */
	@Override
	protected void execute() {
		try {
			doSync();
		} catch (Exception e) {
			e.printStackTrace();
			errorMessage = e.getMessage();
		}
	}

	
	/**
	 * Import the file sent by the client and generate the answer file
	 * @throws Exception
	 */
	protected void doSync() throws Exception {
		// import the file sent by the client
		SyncFileImporter importer = new SyncFileImporter();
		importer.start(file);

		// generate the answer file to be downloaded by the client
		DesktopAnswerFileGenerator answerGenerator = (DesktopAnswerFileGenerator)Component.getInstance("desktopAnswerFileGenerator");
		answerGenerator.setKeyList(importer.getEntityKeys());
		answerGenerator.setClientEntityVersions(importer.getEntityVersions());

		File outFile = DesktopAnswerFileGenerator.getAnswerFileName(token);
		System.out.println(outFile);
		FileOutputStream out = new FileOutputStream(outFile);

		userWorkspace = App.getEntityManager().find(UserWorkspace.class, userWorkspace.getId());
		answerGenerator.generateFile(userWorkspace.getTbunit(), out);
		out.close();
	}
	
	
	/**
	 * 
	 */
	private void saveStatus() {
		ClientSyncResult resp = new ClientSyncResult();
		resp.setId(token);
		if ((errorMessage != null) && (errorMessage.length() > 250)) {
			errorMessage = errorMessage.substring(0, 249);
		}
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
