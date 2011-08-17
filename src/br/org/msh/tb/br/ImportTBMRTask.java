package org.msh.tb.br;

import org.msh.tb.application.tasks.DbBatchTask;

public class ImportTBMRTask extends DbBatchTask {

	@Override
	protected void starting() {
		setCommitCounter(1);
	}

}
