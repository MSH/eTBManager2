package org.msh.tb.application.tasks;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.async.Asynchronous;

@Name("asyncTaskRunner")
public class AsyncTaskRunner {

	@Asynchronous
	public void runTask(AsyncTask task) {
		task.start();
	}
}
