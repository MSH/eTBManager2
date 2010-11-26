package org.msh.tb.log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Id;

import org.apache.commons.beanutils.PropertyUtils;
import org.jboss.seam.Component;
import org.msh.mdrtb.entities.LocalizedNameComp;
import org.msh.mdrtb.entities.LogValue;
import org.msh.mdrtb.entities.TransactionLog;
import org.msh.mdrtb.entities.User;
import org.msh.mdrtb.entities.UserLog;
import org.msh.mdrtb.entities.UserLogin;
import org.msh.mdrtb.entities.UserRole;
import org.msh.mdrtb.entities.Workspace;
import org.msh.mdrtb.entities.WorkspaceLog;
import org.msh.mdrtb.entities.enums.CaseClassification;
import org.msh.mdrtb.entities.enums.LogValueType;
import org.msh.mdrtb.entities.enums.RoleAction;

/**
 * Register log of events that occur in the system
 * @author Ricardo Memoria
 *
 */
public class LogService {

	private UserLogin userLogin;
	private EntityManager entityManager;
	private List<LogValue> values;

	private List<BeanState> beans;
	private BeanState mainBean;
	
	private CaseClassification caseClassification;
	

	/**
	 * Save the state of the entity. This method is first called when a user is about to execute changes in the
	 * entity.
	 * Calling the method {@link #saveActionTransaction(Object, String, String, List)} will register all changes
	 * made to the entity
	 * @param entity
	 */
	public void startEntityMonitoring(Object entity, boolean isNew, List<String> extraBeans) {
		beans = new ArrayList<BeanState>();
		mainBean = new BeanState(entity, isNew, extraBeans);
		beans.add(mainBean);
	}
	
	
	/**
	 * Add other entities to be monitored
	 * @param entity
	 */
	public void addEntityMonitoring(Object entity) {
		if (mainBean == null)
			throw new RuntimeException("MainBean must be informed first");
	
		beans.add(new BeanState(entity, mainBean.isNewBean(), null));
	}


	/**
	 * Save changes made to the entity since the method {@link #saveEntityState(Object)} was called
	 * @param role - User role that executed the transaction
	 * @param action - action executed. Can be VIEW, NEW, EDIT or DELETE
	 * @param comments - Any additional information to be included in the transaction
	 * @return {@link TransactionLog} object containing the transaction log registered
	 */
	public TransactionLog saveChangingTransaction(String role) {
		if (mainBean == null)
			throw new RuntimeException("Entity cannot be null");
		
		generateLogValues();
		if ((values == null) || (values.size() == 0))
			return null;
		
		return saveTransaction(mainBean.getBean(), role, mainBean.isNewBean()? RoleAction.NEW: RoleAction.EDIT);
	}

	
	/**
	 * Register a log transaction of an entity being removed
	 * @param entity
	 * @param role
	 * @return
	 */
	public TransactionLog saveRemoveTransaction(Object entity, String role) {
		return saveTransaction(entity, role, RoleAction.DELETE);
	}


	/**
	 * Save transaction for a new entity being persisted
	 * @param entity
	 * @param role
	 * @return
	 */
	public TransactionLog saveNewEntityTransaction(Object entity, String role) {
		return saveTransaction(entity, role, RoleAction.NEW);
	}
	

	/**
	 * Map the differences between two entities pointing to the same record (Id) and generate a {@link TransactionLog} object 
	 * for the specified role
	 * @param oldEntity
	 * @param newEntity
	 * @param role
	 * @return
	 */
	public TransactionLog saveEntityDifferences(Object oldEntity, Object newEntity, String role, List<String> extraNestedBeans) {
		beans = new ArrayList<BeanState>();
		mainBean = new BeanState(oldEntity, false, extraNestedBeans);
		beans.add(mainBean);
		
		// update to the new entity
		mainBean.setBean(newEntity);
		generateLogValues();
		
		if ((values == null) || (values.size() == 0))
			return null;
		
		return saveTransaction(mainBean.getBean(), role, mainBean.isNewBean()? RoleAction.NEW: RoleAction.EDIT);		
	}
	
	/**
	 * Save a transaction of {@link RoleAction} of type EXEC, i.e, the system register a transaction of a command
	 * executed by the user
	 * @param entity - Entity involved in the transaction
	 * @param role - {@link UserRole} name of the transaction
	 * @param comments - Any additional information to be included in the transaction
	 * @param values (Optional) list of values to be registered with the transaction
	 * @return {@link TransactionLog} object containing the transaction log registered
	 */
	public TransactionLog saveExecuteTransaction(Object entity, String role) {
		return saveTransaction(entity, role, RoleAction.EXEC);
	}
	

	/**
	 * Register a transaction log
	 * @param entity
	 * @param role
	 * @param action
	 * @param comments
	 * @param values
	 * @return {@link TransactionLog} object containing transaction log registered
	 */
	protected TransactionLog saveTransaction(Object entity, String role, RoleAction action) {
		UserRole userRole = getUserRole(role);
		
		TransactionLog log = new TransactionLog();

		// sempre considera que o ID é um Integer
		log.setEntityId((Integer)getEntityId(entity));
		log.setAction(action);
		log.setRole(userRole);
		log.setTransactionDate(new Date());
		log.setUser(getUserLog());
		log.setWorkspace(getWorkspaceLog());
		log.setEntityClass(entity.getClass().getSimpleName());
		log.setCaseClassification(caseClassification);
		
		// pega a descrição da entidade
		String desc = entity.toString();
		if (desc.length() > 100)
			desc = desc.substring(0, 96) + "...";
		log.setEntityDescription(desc);
		
		boolean hasPrevValues = false;

		if (values != null) {
			log.setLogValues(values);
			for (LogValue val: values) {
				val.setTransactionLog(log);
				val.truncateValues();
				if (val.getPrevValue() != null)
					hasPrevValues = true;
			}
			log.setNumValues(values.size());
		}
		log.setHasPrevValues(hasPrevValues);
		
		EntityManager em = getEntityManager();
		em.persist(log);
		em.flush();
		
		resetValues();
		
		return log;
	}
	


	protected WorkspaceLog getWorkspaceLog() {
		WorkspaceLog wslog = getEntityManager().find(WorkspaceLog.class, getUserLogin().getDefaultWorkspace().getId());
		
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



	protected UserRole getUserRole(String roleName) {
		UserRole role = (UserRole)getEntityManager().createQuery("from UserRole r where r.name = :name")
			.setParameter("name", roleName)
			.getSingleResult();
		return role;
	}
	

	
	/**
	 * Return the entity ID
	 * @param entity
	 * @return Object containing the entity ID
	 */
	protected Object getEntityId(Object entity) {
		Class c = entity.getClass();
		
		Field fid = null;
		
		try {
			// pega a lista de propriedades
			Map props = PropertyUtils.describe(entity);
			String pname = null;

			// procura pela propriedade com notação Id
			for (Object prop: props.keySet()) {
				pname = prop.toString();
				try {
					Field f = c.getDeclaredField(pname);
					Annotation a = f.getAnnotation(Id.class);
					if (a != null) {
						fid = f;
						break;
					}
				} catch (Exception e) {}
			}

			if (fid == null)
				return null;

			return props.get(pname);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	
	protected EntityManager getEntityManager() {
		if (entityManager == null)
			entityManager = (EntityManager)Component.getInstance("entityManager", true);
		return entityManager;
	}


	
	protected UserLogin getUserLogin() {
		if (userLogin == null)
			userLogin = (UserLogin)Component.getInstance("userLogin");
		return userLogin;
	}


	/**
	 * Generate list of {@link LogValue} objects according to the changes in the object state
	 * @param entity - Entity to map the values
	 * @param action - Acceptable values are RoleAction.NEW or RoleAction.EDIT
	 * @return List of {@link LogValue} objects
	 */
	public void generateLogValues() {
		for (BeanState beanState: beans) {
			List<LogValue> beanValues = beanState.generateLogValues();
			
			for (LogValue val: beanValues) {
				if (values == null)
					values = new ArrayList<LogValue>();
				values.add(val);
			}
		}
	}
	
	public List<LogValue> getValues() {
		return values;
	}

	public void resetValues() {
		values = null;
	}
	
	public LogValue addMessageValue(String key, String prevMessage, String newMessage) {
		LogValue val = addValue(key, prevMessage, newMessage);
		val.setType(LogValueType.MESSAGE);
		return val;
	}
	
	public LogValue addMessageValue(String key, String newMessage) {
		LogValue val = addValue(key, newMessage);
		val.setType(LogValueType.MESSAGE);
		return val;
	}
	
	public LogValue addValue(String key, Object value) {
		if (values == null)
			values = new ArrayList<LogValue>();
		
		LogValue val = new LogValue();
		val.setKey(key);
		val.setNewObjectValue(value);
		values.add(val);
		
		return val;
	}


	public LogValue addValue(String key, Object prevValue, Object newValue) {
		if (values == null)
			values = new ArrayList<LogValue>();

		LogValue val = new LogValue();
		val.setKey(key);
		val.setPrevObjectValue(prevValue);
		val.setNewObjectValue(newValue);
		values.add(val);
		
		return val;
	}


	/**
	 * @return the caseClassification
	 */
	public CaseClassification getCaseClassification() {
		return caseClassification;
	}


	/**
	 * @param caseClassification the caseClassification to set
	 */
	public void setCaseClassification(CaseClassification caseClassification) {
		this.caseClassification = caseClassification;
	}
	
}
