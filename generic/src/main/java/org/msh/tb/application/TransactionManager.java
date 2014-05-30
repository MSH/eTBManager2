/**
 * 
 */
package org.msh.tb.application;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.transaction.UserTransaction;

/**
 * Simple SEAM component (but may be used as an ordinary object) to manage 
 * database transaction life cycle, with methods to start, commit or roll back it.
 * 
 * Transactions can be nested. If a call to begin a transaction is called and there is an on-going transaction,
 * no new transaction is started but a counter is incremented. The counter is
 * decremented as the transaction is commited, so when its value is 0, 
 * the transaction is really commited. 
 * 
 * @author Ricardo Memoria
 *
 */
@Name(TransactionManager.COMP_NAME)
@BypassInterceptors
public class TransactionManager {
	protected static final String COMP_NAME = "transactionManager";
	
	private UserTransaction transaction;
	private EntityManager entityManager;

	/**
	 * Incremented when a call to {@link TransactionManager#begin()} is made and
	 * decremented when a call to {@link TransactionManager#commit()} is made.
	 * Just when the value is 0 that the commit and roll back transaction is really called,
	 * so transaction can be started and committed in a nest way 
	 */
	private int beginTxCounter;

	
	/**
	 * Return true if there is an on-going transaction
	 * @return boolean value
	 */
	public boolean isActive() {
		return beginTxCounter > 0;
	}

	/**
	 * Return the transaction in use by the task
	 * @return
	 */
	protected UserTransaction getTransaction() {
		if (transaction == null)
			transaction = (UserTransaction)Component.getInstance("org.jboss.seam.transaction.transaction");
		return transaction;
	}


	/**
	 * Return the {@link EntityManager} instance in use
	 * @return
	 */
	public EntityManager getEntityManager() {
		if (entityManager == null)
			entityManager = (EntityManager)Component.getInstance("entityManager");
		return entityManager;
	}


	/**
	 * Start a new transaction
	 */
	public void begin() {
		// transaction was already started ?
		if (beginTxCounter > 0)
			return;

		try {
			getTransaction().begin();
			getEntityManager().joinTransaction();

			// increment counter
			beginTxCounter++;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	/**
	 * Commit a transaction that is under progress 
	 */
	public void commit() {
		if (beginTxCounter == 0)
			throw new RuntimeException("No transaction in progress");

		beginTxCounter--;
		
		// transaction was already started in some other part?
		if (beginTxCounter > 0)
			return;
		
		try {
			getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	/**
	 * Roll back a transaction that is under progress 
	 */
	public void rollback() {
		// there is any on-going transaction ?
		if (beginTxCounter == 0)
			return;

		beginTxCounter = 0;

		try {
			getTransaction().rollback();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	
	/**
	 * Return a reference to the component from the container
	 * @return instance of {@link TransactionManager} 
	 */
	public static TransactionManager instance() {
		return (TransactionManager)Component.getInstance(COMP_NAME);
	}
}
