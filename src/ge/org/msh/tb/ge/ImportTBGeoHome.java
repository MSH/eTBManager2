package org.msh.tb.ge;


import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.application.tasks.AsyncTask;
import org.msh.tb.application.tasks.TaskManager;

@Name("importGETBHome")
public class ImportTBGeoHome {

	@In EntityManager entityManager;
	@In(create=true) TaskManager taskManager;

	/**
	 * Call assyncronous component to import cases
	 */
	public void execute() {
		taskManager.runTask(ImportTBGeoTask.class, null);
		FacesMessages.instance().add("Import in progress ... You can keep browsing the system");
	}


	/**
	 * Check if task is running
	 * @return true if task is running, otherwise return false
	 */
	public boolean isTaskRunning() {
		return taskManager.findTaskByClass(ImportTBGeoTask.class) != null; 
	}
	
	/**
	 * Return the task
	 * @return
	 */
	public AsyncTask getTask() {
		return taskManager.findTaskByClass(ImportTBGeoTask.class);
	}
	
}
