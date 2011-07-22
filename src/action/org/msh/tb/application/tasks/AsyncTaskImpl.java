package org.msh.tb.application.tasks;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.log.Log;
import org.msh.tb.entities.User;

/**
 * Base implementation for AsyncTask interface
 * @author Ricardo Memoria
 *
 */
public abstract class AsyncTaskImpl implements AsyncTask {

	private Integer id;
	private List<TaskListener> listeners;
	private int progress;
	private TaskStatus status;
	private Date executionTimestamp;
	private User user;

	protected abstract void starting();
	protected abstract void execute();
	protected abstract void finishing();


	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.AsyncTask#start()
	 */
	public void start() {
		progress = 0;
		executionTimestamp = new Date();

		try {
			changeStatus(TaskStatus.STARTING);
			starting();

			changeStatus(TaskStatus.RUNNING);
			execute();
			
			changeStatus(TaskStatus.FINISHING);
			finishing();
			changeStatus(TaskStatus.FINISHED);

		} catch (Exception e) {
			exceptionHandler(e);
			changeStatus(TaskStatus.FINISHED);
		}
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.AsyncTask#cancel()
	 */
	public void cancel() {
		changeStatus(TaskStatus.CANCELING);
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.AsyncTask#getTaskStatus()
	 */
	public TaskStatus getStatus() {
		return status;
	}


	/**
	 * Return the logger
	 * @return
	 */
	protected Log getLogger() {
		return (Log)Component.getInstance("logger", true);
	}

	/**
	 * Standard exception handler
	 * @param e
	 */
	protected void exceptionHandler(Exception e) {
		changeStatus(TaskStatus.FINISHED);
		throw new RuntimeException(e);
	}
	
	/**
	 * Change the current status of the task to a new status
	 * @param value
	 */
	protected void changeStatus(TaskStatus value) {
		if (value == status)
			return;
		status = value;
		notifyStatusChange();
	}

	
	/**
	 * Notify listeners about changes in the status
	 */
	protected void notifyStatusChange() {
		for (TaskListener listener: listeners) {
			listener.taskStatusChangeHandler(this);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.AsyncTask#getDisplayName()
	 */
	public String getDisplayName() {
		String s = getClass().getSimpleName();
		if (id != null)
			s += " (" + id.toString() + ")";
		return s;
	}

	public int getProgress() {
		return progress;
	}
	
	public void setProgress(int value) {
		progress = value;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.AsyncTask#addListener(org.msh.tb.application.tasks.TaskListener)
	 */
	public void addListener(TaskListener taskListener) {
		if (listeners == null)
			listeners = new ArrayList<TaskListener>();
		listeners.add(taskListener);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.AsyncTask#removeListener(org.msh.tb.application.tasks.TaskListener)
	 */
	public void removeListener(TaskListener taskListener) {
		if (listeners != null)
			listeners.remove(taskListener);
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.AsyncTask#getId()
	 */
	public Integer getId() {
		return id;
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.AsyncTask#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	
	/**
	 * Return true if task is being canceled
	 * @return
	 */
	public boolean isCanceling() {
		return status == TaskStatus.CANCELING;
	}
	

	public User getUser() {
		return user;
	}
	
	/**
	 * Set the user that executed the task
	 * @param user
	 */
	public void setUser(User user) {
		this.user = user;
	}
	
	/**
	 * Indicates if the task is unique in the execution list or if several instances of the same task
	 * can run simultaneously
	 * @return true if only one instance can run or false if several instances can run simultaneously 
	 */
	public boolean isUnique() {
		return true;
	}
	
	/**
	 * Return the date and time that the task started execution
	 * @return
	 */
	public Date getExecutionTimestamp() {
		return executionTimestamp;
	}
}
