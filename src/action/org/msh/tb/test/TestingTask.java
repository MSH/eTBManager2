package org.msh.tb.test;

import org.msh.tb.application.tasks.AsyncTaskImpl;
import org.msh.tb.application.tasks.TaskManager;
import org.msh.tb.application.tasks.TaskStatus;

/**
 * Just a quick example of a task implementation. It must be run by {@link TaskManager}
 * @author Ricardo Memoria
 *
 */
public class TestingTask extends AsyncTaskImpl {

	@Override
	protected void starting() {
		sleep(5000);
	}

	@Override
	protected void execute() {
		for (int i = 0; i < 100; i++) {
			setProgress( getProgress() + 1 );
			sleep(500);
	
			if (getStatus().equals(TaskStatus.CANCELING))
				break;
		}
	}

	@Override
	protected void finishing() {
		sleep(5000);
	}

	protected void sleep(int miliseconds) {
		try {
			Thread.sleep(miliseconds);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.AsyncTaskImpl#isUnique()
	 */
	@Override
	public boolean isUnique() {
		return false;
	}
}
