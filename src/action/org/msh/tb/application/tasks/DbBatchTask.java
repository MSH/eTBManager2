package org.msh.tb.application.tasks;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.msh.tb.application.TransactionManager;


/**
 * Abstract task to implement an asynchronous background processing, specific for <b>database batch operations</b>, 
 * like batch importing or long executions in a set of records.
 * <p/>
 * A concrete class must inherit this class and override the method 
 * <p/><code>boolean processBatchRecord()</code>.</p>
 * 
 * When a DB batch task is executed, the method <code>starting()</code> is called to initialize any database operation.
 * After starting, the method <code>processBatchRecord()</code> will be called. 
 * This method is responsible for the execution of one loop in the batch processing (for example, importing of a single record
 * in a batch importing operation). If the method return value is <i>true</i>, it'll be called again to execute another
 * loop in the batch. If the method returns <i>false</i>, the batch execution will start the finishing process calling the 
 * <code>finishing()</code> method.
 * <p/>
 * <code>setRecordCount(int)</code> must be used when task is being started (method starting()) and is called to specify the total number of records to be
 * processed. In this case, the method <code>int getProgress()</code> will automatically return the progress of execution in a range of 0 to 100.
 * <p/>
 * If the concrete class wants to calculate its own progress, call the method <code>setAutomaticProgress(boolean)</code> passing
 * <b>false</b> as argument. In this case, the class will have to update the execution progress by calling <code>setProgress</code> 
 * on each execution of the method <code>processBatchRecord()</code>. 
 * <p/>  
 * <code>setCommitCounter(int)</code> is used to specify the number of records processed inside a transaction.
 * Each time the number of records processed is equals the number specified in commitCounter, the transaction will be commited
 * and the entity manager will be cleared (calling {@link EntityManager} clear() method).
 * <p/> 
 * @author Ricardo Memoria
 *
 */
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
	// manage the transaction
	private TransactionManager transaction;

	
	/**
	 * Process the record being executed
	 * @return true to continue with the importing, otherwise return false
	 */
	protected abstract boolean processBatchRecord() throws Exception;


	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.AsyncTaskImpl#execute()
	 */
	@Override
	protected void execute() {
		executeBatch(new BatchIterator() {
			public boolean processRecord() {
				try {
					return processBatchRecord();
				} catch (Exception e) {
					exceptionHandler(e);
					return false;
				}
			}
		});
	}

	
	/**
	 * Execute the batch processing calling the <code>processRecord</code> of the {@link BatchIterator} passed as parameter
	 * @param it
	 */
	protected void executeBatch(BatchIterator it) {
		recordIndex = 0;
		
		if (automaticProgress)
			setProgress(0);

		beginTransaction();
		try {
			while (it.processRecord()) {
				recordIndex++;
				
				if ((automaticProgress) && (recordCount > 0)) 
					setProgress( Math.round(((float)recordIndex * 100) / ((float)recordCount)) );
				
				if (recordIndex % commitCounter == 0) {
					EntityManager em = getEntityManager();
					em.flush();
					commitTransaction();
					
					em.clear();
					beginTransaction();
				}
			}
			commitTransaction();
		}
		catch (Exception e) {
			rollbackTransaction();
			throw new RuntimeException(e);
		}
	}

	
	/**
	 * Return the instance of {@link TransactionManager} responsible for
	 * manually managing the database transaction
	 * @return {@link TransactionManager} instance
	 */
	public TransactionManager getTransaction() {
		if (transaction == null)
			transaction = (TransactionManager)Component.getInstance("transactionManager");
		return transaction;
	}

	/**
	 * Start a new transaction
	 */
	public void beginTransaction() {
		getTransaction().begin();
	}
	

	/**
	 * Commit a transaction that is under progress 
	 */
	public void commitTransaction() {
		getTransaction().commit();
	}


	/**
	 * Roll back a transaction that is under progress 
	 */
	public void rollbackTransaction() {
		getTransaction().rollback();
	}


	/**
	 * Return the {@link EntityManager} instance in use
	 * @return
	 */
	public EntityManager getEntityManager() {
		return (EntityManager)Component.getInstance("entityManager");
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


	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.AsyncTaskImpl#callFinishing()
	 */
	@Override
	protected void callFinishing() {
		beginTransaction();
		try {
			super.callFinishing();
			commitTransaction();
		} catch (Exception e) {
			rollbackTransaction();
		}
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.AsyncTaskImpl#callStarting()
	 */
	@Override
	protected void callStarting() {
		beginTransaction();
		try {
			super.callStarting();
			commitTransaction();
		} catch (Exception e) {
			rollbackTransaction();
		}
	}

}
