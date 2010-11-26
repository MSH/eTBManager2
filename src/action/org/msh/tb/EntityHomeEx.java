package org.msh.tb;

import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;

import org.jboss.seam.Component;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Expressions.ValueExpression;
import org.jboss.seam.framework.EntityHome;
import org.jboss.seam.international.Messages;
import org.jboss.seam.security.AuthorizationException;
import org.msh.mdrtb.entities.User;
import org.msh.mdrtb.entities.UserLogin;
import org.msh.mdrtb.entities.UserWorkspace;
import org.msh.mdrtb.entities.WSObject;
import org.msh.mdrtb.entities.Workspace;
import org.msh.mdrtb.entities.enums.CaseClassification;
import org.msh.mdrtb.entities.enums.RoleAction;
import org.msh.tb.log.LogInfo;
import org.msh.tb.log.LogService;
import org.msh.utils.EntityQuery;

public class EntityHomeEx<E> extends EntityHome<E> {
	private static final long serialVersionUID = 2466367489746346196L;
	private boolean bNew = true;
	private ValueExpression deletedMessage;
	private ValueExpression updatedMessage;
	private ValueExpression createdMessage;
	
	private LogService logService;
	private boolean transactionLogActive = true;
	private boolean displayMessage = true;
	private boolean checkSecurityOnOpen = true;
	
	private UserLogin userLogin;

	
	/**
	 * Initiate transaction log service for this entityHome. The transaction will be saved on a update or persist call
	 */
	public void initTransactionLog() {
		if ((!bNew) && (logService == null)) {
			logService = new LogService();
			logService.startEntityMonitoring(getInstance(), !isManaged(), getExtraNestedProperties());
		}
	}
	
	
	/**
	 * Return the extra nested properties to be included in the log values list
	 * @return
	 */
	public List<String> getExtraNestedProperties() {
		return null;
	}
	
	/**
	 * Returns the current managed workspace
	 * @return
	 */
	public Workspace getWorkspace() {
		return getEntityManager().find(Workspace.class, getUserLogin().getDefaultWorkspace().getId());
	}

	
	/**
	 * Return user information about the workspace in use
	 * @return {@link UserWorkspace} instance
	 */
	public UserWorkspace getUserWorkspace() {
		return (UserWorkspace)Component.getInstance("userWorkspace");
	}

	
	/**
	 * Returns the managed user
	 * @return
	 */
	public User getUser() {
		return getEntityManager().find(User.class, getUserLogin().getUser().getId());
	}
	

	/**
	 * Return instance of {@link UserLogin} corresponding to the user session
	 * @return {@link UserLogin} instance
	 */
	public UserLogin getUserLogin() {
		if (userLogin == null)
			userLogin = (UserLogin)Component.getInstance("userLogin");
		return userLogin;
	}


	@Override
	public String persist() {
		Object obj = getInstance();
		if (obj instanceof WSObject) {
			WSObject wsobj = (WSObject)getInstance();
			if (wsobj.getWorkspace() == null)
				wsobj.setWorkspace(getWorkspace());
		}

		String ret = super.persist();
		
		saveTransactionLog(bNew? RoleAction.NEW: RoleAction.EDIT);
		
		return ret;
	}
	
	@Override
	public String update() {
		String ret = super.update();
		saveTransactionLog(RoleAction.EDIT);
		return ret;
	}
	
	@Override
	public String remove() {
		if (!isManaged())
			return "error";

		saveTransactionLog(RoleAction.DELETE);
		
		EntityQuery<E> entityQuery = getEntityQuery();
		if (entityQuery != null)
			entityQuery.getResultList().remove(getInstance());

		return super.remove();
	}
	
	@Override
	protected void initDefaultMessages()
	{
	      Expressions expressions = new Expressions();
	      if (createdMessage == null) {
	         createdMessage = expressions.createValueExpression(Messages.instance().get("default.entity_created"));
	      }
	      if (updatedMessage == null) {
	         updatedMessage = expressions.createValueExpression(Messages.instance().get("default.entity_updated"));
	      }
	      if (deletedMessage == null) {
	         deletedMessage = expressions.createValueExpression(Messages.instance().get("default.entity_deleted"));
	      }
	}
	

	@Override
	public ValueExpression getUpdatedMessage()
	{
		return updatedMessage;
	}
	
	@Override
	public ValueExpression getDeletedMessage()
	{
		return deletedMessage;
	}

	
	@Override
	public ValueExpression getCreatedMessage()
	{
		if (bNew)
			 return createdMessage;
		else return updatedMessage;
	}


	protected void execUpdateById(String hql) {
		// garante que o objeto foi carregado
		getInstance();
		Integer id = (Integer)getId();
		getEntityManager()
			.createQuery(hql)
			.setParameter("id", id)
			.executeUpdate();
	}

	
	/**
	 * Check if a field in a JSF inputText is a unique field
	 * @param context
	 * @param comp
	 * @param value
	 */
	public void validateUniqueValue(FacesContext context, UIComponent comp, Object value) {
		String fieldValue = (String)value;
		UIParameter param = (UIParameter)comp.findComponent("field");
		if (param == null)
			throw new RuntimeException("A parameter with id 'field' must be informed");
		
		String hql = "select count(*) from " + getEntityClass().getSimpleName() + " it " +
				"where upper(it." + param.getValue().toString() + ") = :value";
		
		param = (UIParameter)comp.findComponent("where");
		if (param != null)
			hql = hql + " and " + param.getValue().toString();

		if (getInstance() instanceof WSObject)
			hql += " and it.workspace.id = #{defaultWorkspace.id}";
		
		if (isManaged())
			hql = hql + " and it.id <> " + getId().toString();

		Long count = (Long)getEntityManager().createQuery(hql)
			.setParameter("value", fieldValue.toUpperCase())
			.getSingleResult();
		
		if (count > 0) {
			((UIInput)comp).setValid(false);
			String msg = getMessages().get("form.duplicatedname");
			context.addMessage(comp.getClientId(context), new FacesMessage(msg));
		}
	}
	
	
	/**
	 * Register transaction log for operation
	 */
	protected void saveTransactionLog(RoleAction action) {
		if (!transactionLogActive)
			return;

		String roleName = getRoleName();
		if (roleName == null)
			return;
		
		CaseClassification classif = getCaseClassificationForLog();

		// if the log service didn't map object state previously, so there is nothing to do
		if ((logService == null) && (action == RoleAction.EDIT))
			return;
		
		if (getLogService() == null)
			return;
		
		logService.setCaseClassification(classif);

		switch (action) {
		case DELETE: logService.saveRemoveTransaction(getInstance(), roleName);
			break;
		case EDIT: logService.saveChangingTransaction(roleName);
			break;
		case NEW: logService.saveNewEntityTransaction(getInstance(), roleName);
		}
	}


	/**
	 * Return the role name for transaction log. If no transaction log must be registered, return null
	 * @return name of the role for this entity home
	 */
	public String getRoleName() {
		LogInfo logInfo = getClass().getAnnotation(LogInfo.class);
		if ((logInfo == null) || (logInfo.roleName() == null))
			return null;
		
		return logInfo.roleName();
	}

	
	/**
	 * Return the case classification to be used to
	 * @return
	 */
	public CaseClassification getCaseClassificationForLog() {
		return null;
	}


	@Override
	public void setId(Object id) {
		super.setId(id);
		checkCanOpen();
	}
	
	protected void checkCanOpen() {
		if (!checkSecurityOnOpen)
			return;
		
		// check if is the same workspace
		bNew = !isManaged();
		if (!bNew) {
			Workspace objWs = getInstanceWorkspace();
			if (objWs != null) {
				Workspace ws = (Workspace)Component.getInstance("defaultWorkspace");
				if (!objWs.equals(ws))
					throw new AuthorizationException("Access denied. Wrong workspace");
			}
		}
		
		if ((!bNew) && (!isCanOpen()))
			throw new AuthorizationException("Access restricted to entity " + getEntityName() + " with id=" + getId());
	}


	/**
	 * Return the workspace assigned to the entity
	 * @return
	 */
	public Workspace getInstanceWorkspace() {
		Object obj = getInstance();
		if (obj instanceof WSObject)
			 return ((WSObject)obj).getWorkspace();
		else return null;
	}
	
	public boolean isCanOpen() {
		return true;
	}
	
	public void setIdWithLog(Object id) {
		setId(id);
		checkCanOpen();
		if (logService == null)
			initTransactionLog();
	}
	
	public Object getIdWithLog() {
		return getId();
	}
	
	public void setTransactionLogActive(boolean value) {
		transactionLogActive = value;
	}
	
	public boolean isTransactionLogActive() {
		return transactionLogActive;
	}
	
	public LogService getLogService() {
		if (logService == null) {
			logService = new LogService();
		}
		return logService;
	}

	@Override
	protected void deletedMessage() {
		if (displayMessage)
			super.deletedMessage();
	}
	
	@Override
	protected void createdMessage() {
		if (displayMessage)
			super.createdMessage();
	}

	@Override
	protected void updatedMessage() {
		if (displayMessage)
			super.updatedMessage();
	}


	/**
	 * @return the displayMessage
	 */
	public boolean isDisplayMessage() {
		return displayMessage;
	}


	/**
	 * @param displayMessage the displayMessage to set
	 */
	public void setDisplayMessage(boolean displayMessage) {
		this.displayMessage = displayMessage;
	}


	public boolean isCheckSecurityOnOpen() {
		return checkSecurityOnOpen;
	}


	public void setCheckSecurityOnOpen(boolean checkSecurityOnOpen) {
		this.checkSecurityOnOpen = checkSecurityOnOpen;
	}
	
	/**
	 * Return the entity query assigned to this home class
	 */
	public EntityQuery<E> getEntityQuery() {
		return null;
	}
}
