package org.msh.tb.br;

import org.msh.tb.application.tasks.DbBatchTask;

public class ImportTBMRTask extends DbBatchTask {

	private String uf;

	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.AsyncTaskImpl#starting()
	 */
	@Override
	protected void starting() {
		setCommitCounter(1);
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.DbBatchTask#processBatchRecord()
	 */
	@Override
	protected boolean processBatchRecord() {
		return false;
	}
}
