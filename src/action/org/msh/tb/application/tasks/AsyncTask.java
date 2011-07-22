package org.msh.tb.application.tasks;

import java.util.Date;

import org.msh.tb.entities.User;

/**
 * Interface that all task must implement
 * @author Ricardo Memoria
 *
 */
public interface AsyncTask {

	/**
	 * Start the execution of the task 
	 */
	public void start();


	/**
	 * Cancel the execution of the task
	 */
	void cancel();


	/**
	 * Return the current status of the task
	 * @return
	 */
	TaskStatus getStatus();


	/**
	 * Return the display name of the task to be read by the user  
	 * @return
	 */
	public String getDisplayName();


	/**
	 * Return the execution progress in terms of percentage (0 to 100%)
	 * @return
	 */
	public int getProgress();


	/**
	 * Add a listener to a task in order to receive information about its execution
	 * @param task
	 */
	public void addListener(TaskListener taskListener);


	/**
	 * Remove a listener from a task
	 * @param taskListener
	 */
	public void removeListener(TaskListener taskListener);

	
	/**
	 * Return the unique ID given by the task manager to this task
	 * @return
	 */
	Integer getId();


	/**
	 * Change the ID of the task
	 * @param id
	 */
	void setId(Integer id);
	
	/**
	 * Return the user that executed the task. If the task was executed by the system, this method returns null
	 * @return instance of the {@link User} class representing the user that executed the task. 
	 */
	User getUser();
	
	/**
	 * Set the user that executed the task
	 * @param user
	 */
	void setUser(User user);
	
	/**
	 * Indicates if the task is unique in the execution list or if several instances of the same task
	 * can run simultaneously
	 * @return true if only one instance can run or false if several instances can run simultaneously 
	 */
	boolean isUnique();
	
	/**
	 * Return the date and time that the task started execution
	 * @return
	 */
	Date getExecutionTimestamp();
}
