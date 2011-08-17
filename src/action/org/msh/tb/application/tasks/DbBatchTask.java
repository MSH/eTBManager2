package org.msh.tb.application.tasks;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.transaction.UserTransaction;


public abstract class DbBatchTask extends AsyncTaskImpl {

	/**
	 * Interface that must be implemented to have batch callback execution
	 * @author Ricardo Memoria
	 *
	 */
	public interface BatchIterator {
		boolean processRecord();
	}
	
	private int recordCount;
	private int recordIndex;
	private int commitCounter = 10;
	private boolean automaticProgress = true;
	
	
	/**
	 * Process the record being executed
	 * @return true to continue with the importing, otherwise return false
	 */
	protected boolean processBatchRecord() {
		return false;
	}



	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.AsyncTaskImpl#execute()
	 */
	@Override
	protected void execute() {
		executeBatch(new BatchIterator() {
			public boolean processRecord() {
				return processBatchRecord();
			}
		});
	}

	
	/**
	 * @param it
	 */
	protected void executeBatch(BatchIterator it) {
		recordIndex = 0;
		
		if (automaticProgress)
			setProgress(0);
		
		UserTransaction tx = getTransaction();
		try {
			
			tx.begin();
			
			try {
				EntityManager em = getEntityManager();
				em.joinTransaction();
				while (it.processRecord()) {
					recordIndex++;
					
					if ((automaticProgress) && (recordCount > 0)) 
						setProgress( Math.round(recordIndex / recordCount) );
					
					if (recordIndex % commitCounter == 0) {
						em.flush();
						tx.commit();
						
						em.clear();
						tx.begin();
					}
				}
				tx.commit();
			}
			
			catch (Exception e) {
				tx.rollback();
				throw e;
			}

		} catch (Exception e) {
			exceptionHandler(e);
		}
	}


	/**
	 * Return the transaction in use by the task
	 * @return
	 */
	protected UserTransaction getTransaction() {
		return (UserTransaction)Component.getInstance("org.jboss.seam.transaction.transaction");
	}

	/**
	 * Return the {@link EntityManager} instance in use
	 * @return
	 */
	public EntityManager getEntityManager() {
		return (EntityManager)Component.getInstance("entityManager");
	}


	@Override
	protected void finishing() {
		// do nothing by now
	}

	public void setCommitCounter(int commitCounter) {
		this.commitCounter = commitCounter;
	}

	public int getCommitCounter() {
		return commitCounter;
	}

	/**
	 * @return the recordIndex
	 */
	public int getRecordIndex() {
		return recordIndex;
	}

	/**
	 * @param recordIndex the recordIndex to set
	 */
	public void setRecordIndex(int recordIndex) {
		this.recordIndex = recordIndex;
	}

	/**
	 * @return the recordCount
	 */
	public int getRecordCount() {
		return recordCount;
	}



	/**
	 * @return the automaticProgress
	 */
	public boolean isAutomaticProgress() {
		return automaticProgress;
	}



	/**
	 * @param automaticProgress the automaticProgress to set
	 */
	public void setAutomaticProgress(boolean automaticProgress) {
		this.automaticProgress = automaticProgress;
	}



	/**
	 * @param recordCount the recordCount to set
	 */
	public void setRecordCount(int recordCount) {
		this.recordCount = recordCount;
	}

}
