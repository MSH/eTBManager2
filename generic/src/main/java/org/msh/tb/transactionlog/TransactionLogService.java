package org.msh.tb.transactionlog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.beanutils.PropertyUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.LocalizedNameComp;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.TransactionLog;
import org.msh.tb.entities.Transactional;
import org.msh.tb.entities.User;
import org.msh.tb.entities.UserLog;
import org.msh.tb.entities.UserLogin;
import org.msh.tb.entities.UserRole;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.WorkspaceLog;
import org.msh.tb.entities.enums.RoleAction;

@Name("transactionLogService")
@BypassInterceptors
public class TransactionLogService {

	private DetailXMLWriter detailWriter;
	private List<Item> items;
	private String titleSuffix;


	/**
	 * Capture the properties of the entity that are to be saved in a transaction
	 * @param entity
	 */
	public void recordEntityState(Object entity, Operation oper) {
		if (oper == Operation.ALL)
			throw new IllegalArgumentException("Invalid Operation.ALL");

		EntityLogMapping map = EntityLogManager.instance().getEntityMapping(entity);
		List<PropertyValue> values = map.describeEntity(entity, oper);

		addItem(oper, values);
	}

	
	/**
	 * Store temporary information about a property value of an entity
	 * @param entity
	 * @param prop
	 * @param value
	 * @return
	 */
	private Item addItem(Operation oper, List<PropertyValue> values) {
		Item item = new Item(oper, values);
		if (items == null)
			items = new ArrayList<TransactionLogService.Item>();

		items.add(item);

		return item;
	}

	/**
	 * Save transaction log recovering entity data from entity
	 * @param eventName
	 * @param action
	 * @param entity
	 * @return
	 */
	public TransactionLog save(String eventName, RoleAction action, Object entity) {
		Integer id = null;

		try {
			id = (Integer)PropertyUtils.getProperty(entity, "id");
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("No property id found in entity " + entity.getClass().toString());
		}

		return save(eventName, action, entity.toString(), id, entity.getClass().getSimpleName(), entity);
	}


	/**
	 * Save a new transaction log
	 * @param eventName is the short name of the event (user role)
	 * @param action type of action related to this transaction (execution, new, edit, deleted)
	 */
	public TransactionLog save(String eventName, RoleAction action, String description, Integer entityId, String entityClass, Object entity) {
		// check if there are changes
		if ((!checkEntityValues()) && (action == RoleAction.EDIT))
			return null;

		UserRole role = translateRole(eventName);

		if (items != null) {
			DetailXMLWriter writer = getDetailWriter();
			for (Item item: items) {
				for (PropertyValue val: item.getValues()) {
					if (item.getOperation() != Operation.EDIT)
						 writer.addTableRow(val.getMapping().getMessageKey(), val.getValue());
					else writer.addTableRow(val.getMapping().getMessageKey(), val.getValue(), val.getEntityNewValue());
				}
			}
		}

		UserLog userLog = getUserLog();
		if (userLog == null)
			throw new RuntimeException("No user found for transaction log operation");

		TransactionLog log = new TransactionLog();
		log.setAction(action);
		log.setEntityId(entityId);
		log.setRole(role);
		log.setEntityDescription(description);
		log.setTransactionDate(new Date());
		log.setUser(userLog);
		log.setWorkspace(getWorkspaceLog());
		log.setAdminUnit(getAdminUnit());
		log.setUnit(getUnit());
		log.setTitleSuffix(titleSuffix);
		log.setEntityClass(entityClass);
		log.setComments(getDetailWriter().asXML());
		
		EntityManager em = getEntityManager();
		em.persist(log);
		em.flush();

		// update transaction information to the entity
		if (entity instanceof Transactional) {
			Transactional t = (Transactional)entity;
			t.setLastTransaction(log);
		}
		
		items = null;
		detailWriter = null;

		return log;
	}

	
	/**
	 * Save a transaction log of type execution
	 * @param userRole
	 * @param description
	 * @param entityId
	 */
	public TransactionLog saveExecuteTransaction(String userRole, String description, Integer entityId, String entityClass, Object entity) {
		return save(userRole, RoleAction.EXEC, description, entityId, entityClass, entity);
	}

	/**
	 * Save a transaction log of type execution using an entity to save its information
	 * @param userRole
	 * @param entity
	 */
	public TransactionLog saveExecuteTransaction(String userRole, Object entity) {
		return save(userRole, RoleAction.EXEC, entity);
	}

	
	/**
	 * Save the transaction data to an existing transaction. It's useful when you're executing a batch
	 * processing where a transaction must be available since from the beginning, but more information
	 * will be included to the transaction later
	 * @param id
	 */
	public TransactionLog appendTransaction(Integer id) {
		EntityManager em = getEntityManager();
		
		TransactionLog log = em.find(TransactionLog.class, id);

		if (items != null) {
			DetailXMLWriter writer = getDetailWriter();
			for (Item item: items) {
				for (PropertyValue val: item.getValues()) {
					if (item.getOperation() != Operation.EDIT)
						 writer.addTableRow(val.getMapping().getMessageKey(), val.getValue());
					else writer.addTableRow(val.getMapping().getMessageKey(), val.getValue(), val.getEntityNewValue());
				}
			}
		}

		log.setComments(getDetailWriter().asXML());

		em.persist(log);
		em.flush();
		
		return log;
	}
	
	/**
	 * Check if entity values changed since last time they were captured by the method <code>captureProperties</code>
	 * 
	 * @return true if there are changes, or false if there is no change at all
	 */
	protected boolean checkEntityValues() {
		if (items == null)
			return false;

		boolean result = false;

		int index = 0;
		while (index < items.size()) {
			Item item = items.get(index);

			int counter = 0;
			while (counter < item.getValues().size()) {
				PropertyValue val = item.getValues().get(counter);
				if (item.getOperation() == Operation.EDIT) {
					boolean changed = val.isValueChanged();

					if (changed || val.getMapping().isLoggedForOperation(Operation.EDIT))
						 counter++;
					else item.getValues().remove(counter);

					if (changed)
						result = true;
				}
				else {
					if (val.getValue() == null) 
						item.getValues().remove(counter);
					else {
						counter++;
						result = true;
					}
				}
			}
			index++;
		}
		
		return result;
	}
	

	/**
	 * Return information about the session of the current user
	 * @return
	 */
	protected UserLogin getUserLogin() {
		return (UserLogin)Component.getInstance("userLogin");
	}

	

	/**
	 * Return the workspace to be used in log transactions
	 * @return
	 */
	protected WorkspaceLog getWorkspaceLog() {
		Workspace ws = (Workspace)Component.getInstance("defaultWorkspace");
		if (ws.getId()!=null){
			WorkspaceLog wslog = getEntityManager().find(WorkspaceLog.class, ws.getId());
			
			if (wslog == null) {
				wslog = new WorkspaceLog();
				wslog.setId(ws.getId());
				wslog.setName(new LocalizedNameComp());
				wslog.getName().setName1(ws.getName().getName1());
				wslog.getName().setName2(ws.getName().getName2());
				getEntityManager().persist(wslog);
			}
			return wslog;
		}
		return null;
	}
	

	/**
	 * Return the TB unit of the current user
	 * @return
	 */
	protected Tbunit getUnit() {
		UserWorkspace uw = null;
		try{
			uw = (UserWorkspace)Component.getInstance("userWorkspace");
			uw = getEntityManager().find(UserWorkspace.class, uw.getId());
		}
		catch(Exception e){
			e.printStackTrace();
		}
		Tbunit unit = getEntityManager().find(Tbunit.class, uw.getTbunit().getId());
		return unit;
	}

	/**
	 * @return
	 */
	protected AdministrativeUnit getAdminUnit() {
		UserWorkspace uw = null;
		try{
			uw = (UserWorkspace)Component.getInstance("userWorkspace");
			uw = getEntityManager().find(UserWorkspace.class, uw.getId());
		}
		catch(Exception e){
			e.printStackTrace();
		}
		AdministrativeUnit adminUnit = getEntityManager().find(AdministrativeUnit.class, uw.getTbunit().getAdminUnit().getId());
		return adminUnit;
	}

	/**
	 * Return the user to be used in log transactions
	 * @return
	 */
	protected UserLog getUserLog() {
		UserLogin userLogin = getUserLogin();
		if (userLogin == null)
			return null;
		
		UserLog userLog = getEntityManager().find(UserLog.class, userLogin.getUser().getId());
		
		if (userLog == null) {
			// save new user log information
			userLog = new UserLog();
			User user = getUserLogin().getUser();
			userLog.setId(user.getId());
			userLog.setName(user.getName());
			getEntityManager().persist(userLog);
			return userLog;
		} 
		return userLog;
	}


	
	/**
	 * Translate a event name (role name) into a managed instance of {@link UserRole} class
	 * @param eventName
	 * @return
	 */
	protected UserRole translateRole(String eventName) {
		EntityManager em = getEntityManager();
		
		UserRole role = (UserRole) em.createQuery("from UserRole where name = :name")
			.setParameter("name", eventName)
			.getResultList().get(0);
		return role;
	}

	
	/**
	 * Return the instance of the {@link EntityManager} related to this scope
	 * @return
	 */
	protected EntityManager getEntityManager() {
		return (EntityManager)Component.getInstance("entityManager");
	}
	
	/**
	 * Return the writer object to be used to write the details of the log
	 * @return
	 */
	public DetailXMLWriter getDetailWriter() {
		if (detailWriter == null)
			detailWriter = new DetailXMLWriter();
		
		return detailWriter;
	}
	
	
	public void addTableRow(String key, Object... value) {
		getDetailWriter().addTableRow(key, value);
	}
	
	/**
	 * Temporary data to store information about entity changes
	 * @author Ricardo Memoria
	 *
	 */
	private class Item {
		private List<PropertyValue> values;
		private Operation operation;
		
		private Item(Operation operation, List<PropertyValue> values) {
			this.operation = operation;
			this.values = values;
		}
		
		public List<PropertyValue> getValues() {
			return values;
		}
		
		public Operation getOperation() {
			return operation;
		}
	}

	/**
	 * @return the titleSuffix
	 */
	public String getTitleSuffix() {
		return titleSuffix;
	}


	/**
	 * @param titleSuffix the titleSuffix to set
	 */
	public void setTitleSuffix(String titleSuffix) {
		this.titleSuffix = titleSuffix;
	}
	
	
	/**
	 * Return the instance of the component
	 * @return
	 */
	public static TransactionLogService instance() {
		return (TransactionLogService)Component.getInstance("transactionLogService");
	}
}
