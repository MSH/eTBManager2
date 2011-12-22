package org.msh.tb.transactionlog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.LocalizedNameComp;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.TransactionLog;
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
	 * Save a new transaction log
	 * @param eventName is the short name of the event (user role)
	 * @param action type of action related to this transaction (execution, new, edit, deleted)
	 */
	public TransactionLog save(String eventName, RoleAction action, String description, Integer entityId) {
		UserRole role = translateRole(eventName);
		checkEntityValues();

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

		TransactionLog log = new TransactionLog();
		log.setAction(action);
		log.setEntityId(entityId);
		log.setRole(role);
		log.setEntityDescription(description);
		log.setTransactionDate(new Date());
		log.setUser(getUserLog());
		log.setWorkspace(getWorkspaceLog());
		log.setComments(getDetailWriter().asXML());
		log.setAdminUnit(getAdminUnit());
		log.setUnit(getUnit());
		
		EntityManager em = getEntityManager();
		em.persist(log);
		em.flush();
		
		return log;
	}

	
	/**
	 * Save a transaction log of type execution
	 * @param userRole
	 * @param description
	 * @param entityId
	 */
	public void saveExecuteTransaction(String userRole, String description, Integer entityId) {
		save(userRole, RoleAction.EXEC, description, entityId);
	}

	
	/**
	 * Check if entity values changed since last time they were captured by the method <code>captureProperties</code>
	 */
	protected void checkEntityValues() {
		if (items == null)
			return;

		int index = 0;
		while (index < items.size()) {
			Item item = items.get(index);

			int counter = 0;
			while (counter < item.getValues().size()) {
				PropertyValue val = item.getValues().get(counter);
				if (item.getOperation() == Operation.EDIT) {
					if (!val.isValueChanged())
						 item.getValues().remove(counter);
					else counter++;
				}
				else {
					if (val.getValue() == null)
						item.getValues().remove(counter);
					else counter++;
				}
			}
			index++;
		}
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
		WorkspaceLog wslog = getEntityManager().find(WorkspaceLog.class, getUserLogin().getWorkspace().getId());
		
		if (wslog == null) {
			wslog = new WorkspaceLog();
			Workspace ws = getUserLogin().getDefaultWorkspace();
			wslog.setId(ws.getId());
			wslog.setName(new LocalizedNameComp());
			wslog.getName().setName1(ws.getName().getName1());
			wslog.getName().setName2(ws.getName().getName2());
			getEntityManager().persist(wslog);
		}
		return wslog;
	}
	

	/**
	 * Return the TB unit of the current user
	 * @return
	 */
	protected Tbunit getUnit() {
		UserWorkspace uw = (UserWorkspace)Component.getInstance("userWorkspace");
		Tbunit unit = getEntityManager().find(Tbunit.class, uw.getTbunit().getId());
		return unit;
	}
	
	/**
	 * @return
	 */
	protected AdministrativeUnit getAdminUnit() {
		UserWorkspace uw = (UserWorkspace)Component.getInstance("userWorkspace");
		AdministrativeUnit adminUnit = getEntityManager().find(AdministrativeUnit.class, uw.getTbunit().getAdminUnit().getId());
		return adminUnit;
	}

	/**
	 * Return the user to be used in log transactions
	 * @return
	 */
	protected UserLog getUserLog() {
		UserLog userLog = getEntityManager().find(UserLog.class, getUserLogin().getUser().getId());
		
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
			.getSingleResult();
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
}
