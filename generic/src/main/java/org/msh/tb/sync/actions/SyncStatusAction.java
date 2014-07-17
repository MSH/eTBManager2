/**
 * 
 */
package org.msh.tb.sync.actions;

import org.jboss.seam.annotations.Name;
import org.msh.tb.application.App;
import org.msh.tb.application.tasks.AsyncTask;
import org.msh.tb.application.tasks.TaskManager;
import org.msh.tb.entities.ClientSyncResult;
import org.msh.tb.sync.SyncFileTask;

import javax.persistence.EntityManager;
import java.io.PrintWriter;

/**
 * Generate response about the status of the processing of the
 * sync file sent by the client app
 * 
 * @author Ricardo Memoria
 *
 */
@Name("syncStatusAction")
public class SyncStatusAction extends StandardAction {

	private String fileToken;
		
	/**
	 * Generate the response to the client
	 */
	@Override
	protected void generateResponse() {
		if (fileToken == null)
			throw new RuntimeException("Token was not defined");

		PrintWriter out = getWriter();
		
		// the file is still being processed ?
		if (isOnGoingProcess()) {
			out.println("ONGOING");
			return;
		}
		
		// get information about the sync
		EntityManager em = App.getEntityManager();
		ClientSyncResult res = em.find(ClientSyncResult.class, fileToken);
		// an error happened ?
		if ((res == null) || (res.getErrorMessage() != null)) {
			String msg = "ERROR:";
			if (res != null)
				msg += res.getErrorMessage();
			else msg += "No information found for this synchronization";
			out.println(msg);
		}
		else out.println("SUCCESS");
	}
	
	
	/**
	 * Check if the file is still being processed
	 * @return true if so
	 */
	protected boolean isOnGoingProcess() {
		TaskManager tm = TaskManager.instance();
		for (AsyncTask task: tm.getTasks()) {
			if (task instanceof SyncFileTask) {
				SyncFileTask syncTask = (SyncFileTask)task;
				// is the task of the file being processed ?
				if (syncTask.getToken().equals(fileToken))
					return true;
			}
		}
		return false;
	}


	/**
	 * @return the fileToken
	 */
	public String getFileToken() {
		return fileToken;
	}


	/**
	 * @param fileToken the fileToken to set
	 */
	public void setFileToken(String fileToken) {
		this.fileToken = fileToken;
	}
}
