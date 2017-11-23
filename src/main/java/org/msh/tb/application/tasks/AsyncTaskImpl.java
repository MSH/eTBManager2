package org.msh.tb.application.tasks;

import org.jboss.seam.Component;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;
import org.msh.tb.entities.User;
import org.msh.tb.entities.Workspace;

import java.util.*;

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
	private Workspace workspace;
	private Map<String, Object> parameters;
	private StringBuffer logMessage;

	protected abstract void starting();
	protected abstract void execute();
	protected abstract void finishing();


	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.AsyncTask#start()
	 */
	public void start() {
		progress = 0;
		executionTimestamp = new Date();
		
		if (getWorkspace() != null)
			Contexts.getEventContext().set("defaultWorkspace", getWorkspace());

		Object userLogin = getParameter("userLogin");
		if (userLogin != null)
			Contexts.getEventContext().set("userLogin", userLogin);
		
		Object userWorkspace = getParameter("userWorkspace");
		if (userWorkspace != null)
			Contexts.getEventContext().set("userWorkspace", userWorkspace);
		
		try {
			changeStatus(TaskStatus.STARTING);
			callStarting();

			changeStatus(TaskStatus.RUNNING);
			callExecute();

			changeStatus(TaskStatus.FINISHING);
			callFinishing();
			changeStatus(TaskStatus.FINISHED);

		} catch (Exception e) {
			exceptionHandler(e);
			if (getStatus() != TaskStatus.FINISHING) {
				finishing();
			}
			changeStatus(TaskStatus.ERROR);
		}
		finally {
			if (isCanceling())
				changeStatus(TaskStatus.CANCELED);
		}
	}


	/**
	 * Wrapper method to call the finishing() method
	 */
	protected void callFinishing() {
		finishing();
	}

	/**
	 * Wrapper method to call the execute() method
	 */
	protected void callExecute() {
		execute();
	}

	/**
	 * Wrapper method to call the starting() method
	 */
	protected void callStarting() {
		starting();
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
		changeStatus(TaskStatus.CANCELING);
		e.printStackTrace();
//		throw new RuntimeException(e);
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
	
	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.AsyncTask#addParameter(java.lang.String, java.lang.Object)
	 */
	public void addParameter(String param, Object value) {
		if (parameters == null)
			parameters = new HashMap<String, Object>();
		
		parameters.put(param, value);
	}
	
	
	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.AsyncTask#getParameter(java.lang.String)
	 */
	public Object getParameter(String param) {
		return (parameters == null? null: parameters.get(param));
	}


	/**
	 * Return a parameter as a string value
	 * @param param
	 * @return
	 */
	public String getStringParam(String param) {
		return (String)getParameter(param);
	}


	public User getUser() {
		return user;
	}

	
	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.AsyncTask#getLogMessage()
	 */
	public String getLogMessage() {
		return (logMessage != null? logMessage.toString(): null);
	}


	public void addLogMessage(String msg) {
		if (logMessage == null)
			logMessage = new StringBuffer();
		
		logMessage.append(msg);
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
	/**
	 * @return the workspace
	 */
	public Workspace getWorkspace() {
		return workspace;
	}
	/**
	 * @param workspace the workspace to set
	 */
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}
}
