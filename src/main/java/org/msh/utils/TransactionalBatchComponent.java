package org.msh.utils;

import org.jboss.seam.Component;
import org.jboss.seam.transaction.UserTransaction;

import javax.persistence.EntityManager;

/**
 * Base class to give access to transaction control for the entity manager by exposing methods 
 * to start, commit or rollback a transaction. <p/>
 * It also supports long transactional batch processing 
 * @author Ricardo Memoria
 *
 */
public abstract class TransactionalBatchComponent {

	private Exception exception;
	
	/**
	 * Execute the batch processing
	 * @return true if batch processing was successfully executed, otherwise returns false and the error
	 * can be obtained by the method getException();
	 */
	public boolean execute() {
		try {
			boolean bFirst = true;

			startTransaction();
			startExecution();
			while (true) {
				if (!bFirst)
					startTransaction();
				boolean cont = executeIteration();
				commit();

				if (!cont)
					break;
				bFirst = false;
			}
			
		} catch (Exception e) {
			exception = e;
			e.printStackTrace();
			try {
				rollback();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			return false;
		}

		// finish execution
		try {
			finishExecution();
		} catch (Exception e) {
			exception = e;
		}

		return true;
	}


	/**
	 * @return false if execution is over, otherwise true to commit the changes, start a new transaction
	 * and call it again 
	 */
	protected abstract boolean executeIteration() throws Exception;

	
	/**
	 * Return the exception generated by the execute method in case an error occurs
	 * @return
	 */
	public Exception getException() {
		return exception;
	}


	/**
	 * Called by execute to initialize the batch processing
	 */
	protected void startExecution() throws Exception {
		// do nothing
	}

	
	/**
	 * Called by execute to finish the batch processing
	 */
	protected void finishExecution() throws Exception {
		// do nothing
	}
	
	/**
	 * Return the user transaction in the current scope 
	 * @return {@link UserTransaction} instance
	 */
	protected UserTransaction getUserTransaction() {
		return (UserTransaction)Component.getInstance("org.jboss.seam.transaction.transaction");
	}
	
	/**
	 * Return the entity manager. Before using this entity manager, a transaction must be started
	 * @return
	 */
	protected EntityManager getEntityManager() {
		return (EntityManager)Component.getInstance("entityManager");
	}

	
	/**
	 * Start a transaction in the scope of the entity manager. If the transaction was already started, 
	 * nothing is done
	 * @throws Exception
	 */
	protected void startTransaction() throws Exception {
		UserTransaction tx = getUserTransaction();
		if (tx.isActive())
			return;
		tx.begin();
		getEntityManager().joinTransaction();
	}


	/**
	 * Commit an on-going transaction
	 */
	protected void commit() throws Exception {
		UserTransaction tx = getUserTransaction();
		getEntityManager().flush();
		tx.commit();
	}
	
	
	/**
	 * Roll back an on-going transaction
	 */
	protected void rollback() throws Exception {
		getUserTransaction().rollback();
	}
	
}
