package org.msh.tb.test;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.application.tasks.TaskManager;

import java.util.Random;

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
		try {
			Thread.sleep(500);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public void runMoldovaInt() {
//		taskManager.runTask(SymetaImportTask.class);
	}
	
	
	public void runIdle() {
		Random r = new Random();
		int val = r.nextInt();
		System.out.println("executing procId = " + val);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("finishing procId = " + val);
	}
	
	/**
	 * Generates an error (testing purposes)
	 */
	public void raiseError() {
		throw new RuntimeException("Error testing");
	}
}
