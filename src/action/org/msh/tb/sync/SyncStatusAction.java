/**
 * 
 */
package org.msh.tb.sync;

import java.io.PrintWriter;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.Name;
import org.msh.tb.application.App;
import org.msh.tb.application.tasks.AsyncTask;
import org.msh.tb.application.tasks.TaskManager;
import org.msh.tb.application.tasks.TaskStatus;
import org.msh.tb.entities.ClientSyncResult;

/**
 * Generate response about the status of the processing of the
 * sync file sent by the client app
 * 
 * @author Ricardo Memoria
 *
 */
@Name("syncStatusAction")
public class SyncStatusAction extends StandardAction {

	private String userToken;
		
	/**
	 * Generate the response to the client
	 */
	@Override
	protected void generateResponse() {
		if (userToken == null)
			throw new RuntimeException("Token was not defined");

		PrintWriter out = getWriter();
		
		// the file is still being processed ?
		if (isOnGoingProcess()) {
			out.println("ONGOING");
			return;
		}
		
		// get information about the sync
		EntityManager em = App.getEntityManager();
		ClientSyncResult res = em.find(ClientSyncResult.class, userToken);
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
				if (syncTask.getToken().equals(userToken)) {
					if (syncTask.getStatus() == TaskStatus.RUNNING)
						return true;
					else return false;
				}
			}
		}
		return false;
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
