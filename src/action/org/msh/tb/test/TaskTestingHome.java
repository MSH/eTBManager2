package org.msh.tb.test;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.application.tasks.TaskManager;
import org.msh.tb.md.SymetaImportTask;

/**
 * Test class made just for testing task execution 
 * @author Ricardo Memoria
 *
 */
@Name("taskTestingHome")
public class TaskTestingHome {

	@In TaskManager taskManager;
	
	public void runTask() {
		taskManager.runTask(TestingTask.class);
	}

	public void runMoldovaInt() {
		taskManager.runTask(SymetaImportTask.class);
	}
}
