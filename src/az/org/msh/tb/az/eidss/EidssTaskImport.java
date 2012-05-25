package org.msh.tb.az.eidss;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.application.tasks.AsyncTaskImpl;
import org.msh.tb.application.tasks.TaskManager;

@Name("eidssTaskImport")
public class EidssTaskImport extends AsyncTaskImpl {
	
	@In EidssIntHome eidssIntHome;
	@In TaskManager taskManager;

	
	@Override
	protected void starting() {
	
	}
	
	@Override
	public void execute() {
		try {

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.DbBatchTask#finishing()
	 */
	@Override
	protected void finishing() {
		
	}
}