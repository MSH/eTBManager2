package org.msh.tb.resistpattern;

import java.util.List;

import org.msh.tb.application.tasks.DbBatchTask;
import org.msh.tb.entities.ResistancePattern;

/**
 * Background execution task to update the resistance patterns of all cases.
 * This task is intended to be executed only once and it's objective is 
 * to update the content of the table <code>caseresistpattern</code> 
 * recently introduced in the system.<p/>
 * Prior to that, the resistance pattern had been (wrongly) extracted from 
 * the table examdst.
 *  
 * @author Ricardo Memoria
 *
 */
public class CaseResistancePatternTask extends DbBatchTask {

	private List<Integer> caseIds;
	private List<ResistancePattern> patterns;
	
	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.AsyncTaskImpl#starting()
	 */
	@Override
	protected void starting() {
		// return all the IDs to be updated
		caseIds = getEntityManager().createQuery("select id from TbCase").getResultList();

		// delete the content of the case resistance pattern in order to be updated again
		getEntityManager().createQuery("delete from CaseResistancePattern").executeUpdate();

		// load the resistance patterns into memory (to speed up the process)
		
		setRecordCount(caseIds.size());
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.DbBatchTask#processBatchRecord()
	 */
	@Override
	protected boolean processBatchRecord() throws Exception {
		return false;
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.AsyncTaskImpl#finishing()
	 */
	@Override
	protected void finishing() {
		// TODO Auto-generated method stub
		
	}
}
