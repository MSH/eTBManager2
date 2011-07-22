package org.msh.tb.application.tasks;

/**
 * Interface that an object must implement in order to receiving information about a task execution
 * @author Ricardo Memoria
 *
 */
public interface TaskListener {

	/**
	 * Called everytime the status of a task changes
	 * @param task
	 */
	void taskStatusChangeHandler(AsyncTask task);
}
