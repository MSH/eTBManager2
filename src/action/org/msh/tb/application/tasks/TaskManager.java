package org.msh.tb.application.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.entities.UserLogin;
import org.msh.tb.entities.Workspace;

@Name("taskManager")
@Scope(ScopeType.APPLICATION)
@AutoCreate
public class TaskManager implements TaskListener {

	private List<AsyncTask> tasks = new ArrayList<AsyncTask>();
	private int idCounter;

	public void runTask(Class taskClazz) {
		runTask(taskClazz, null);
	}
	
	public void runTask(Class taskClazz, Map<String, Object> params) {
		AsyncTask task = findTaskByClass(taskClazz);
		if ((task != null) && (task.isUnique()))
			throw new RuntimeException("There is already a task running as " + task.getDisplayName());
		
		try {
			task = (AsyncTask)taskClazz.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error when trying to run task " + taskClazz.toString());
			throw new RuntimeException(e);
		}

		if (task == null)
			throw new RuntimeException("Interface " + AsyncTask.class.toString() + " in class " + taskClazz.toString());

		task.addListener(this);
		
		// feed parameters to the task
		if (params != null) {
			for (String param: params.keySet()) {
				task.addParameter(param, params.get(param));
			}
		}
		
		UserLogin userLogin = (UserLogin)Component.getInstance("userLogin");
		if (userLogin != null)
			task.setUser(userLogin.getUser());
		
		Workspace workspace = (Workspace)Component.getInstance("defaultWorkspace");
		if (workspace != null)
			task.setWorkspace(workspace);
		
		AsyncTaskRunner runner = (AsyncTaskRunner)Component.getInstance("asyncTaskRunner", true);
		runner.runTask(task);
	}


	/**
	 * Search for a task by its implementation class 
	 * @param clazz
	 * @return
	 */
	public AsyncTask findTaskByClass(Class clazz) {
		for (AsyncTask task: tasks) {
			if (task.getClass() == clazz)
				return task;
		}
		return null;
	}


	/**
	 * Return the list of tasks under execution
	 * @return
	 */
	public List<AsyncTask> getTasks() {
		return tasks;
	}

	
	/**
	 * Cancel a task under execution
	 * @param id
	 */
	public void cancelTask(Integer id) {
		AsyncTask task = findTaskById(id);
		if (task == null)
			return;
		
		task.cancel();
	}

	
	/**
	 * Find a task by its id
	 * @param id
	 * @return
	 */
	public AsyncTask findTaskById(Integer id) {
		for (AsyncTask task: tasks) {
			if (task.getId().equals(id)) {
				return task;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.TaskListener#notifyTaskStarting(org.msh.tb.application.tasks.AsyncTask)
	 */
	public void notifyTaskStarting(AsyncTask task) {
		idCounter++;
		task.setId(idCounter);
		tasks.add(task);
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.TaskListener#notifyTaskEnding(org.msh.tb.application.tasks.AsyncTask)
	 */
	public void notifyTaskFinished(AsyncTask task) {
		tasks.remove(task);
	}


	public void taskStatusChangeHandler(AsyncTask task) {
		switch (task.getStatus()) {
		case STARTING: 
			notifyTaskStarting(task);
		break;
		case FINISHED:
		case ERROR:
		case CANCELED:
			notifyTaskFinished(task);
		break;
		}
	}

}
