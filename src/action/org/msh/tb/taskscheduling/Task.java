package org.msh.tb.taskscheduling;

import java.util.Date;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.msh.tb.entities.Tag;
import org.msh.tb.entities.TransactionLog;
import org.msh.tb.entities.UserLog;
import org.msh.tb.entities.UserRole;
import org.msh.tb.entities.WorkspaceLog;
import org.msh.tb.entities.enums.RoleAction;

public abstract class Task {
	private UserLog userLog;
	private UserRole userRole;
	
    @In(create=true) EntityManager entityManager;
  
    @Asynchronous
    @Transactional
    public abstract QuartzTriggerHandle createQuartzTestTimer(@Expiration Date when, @IntervalCron String interval);
    
	/**
	 * Register transaction log for operation
	 */
	protected void saveTransactionLog(Object entity, Integer workspaceId) {
		if(getWorkspaceLog(workspaceId) == null)
			return;
		
		TransactionLog log = new TransactionLog();
		log.setRole(getUserRole());
		log.setUser(getUserLog());
		log.setAction(RoleAction.EXEC);
		log.setEntityDescription(entity.toString());
		log.setTransactionDate(new Date());
		log.setWorkspace(getWorkspaceLog(workspaceId));
		log.setTitleSuffix(getEventName());
		log.setEntityClass(Tag.class.getSimpleName());
		log.setComments(getLogDescription());
		
		entityManager.persist(log);
		entityManager.flush();
	}
	
	protected abstract String getEventName();
	
	protected abstract String getLogDescription();
	
	private WorkspaceLog getWorkspaceLog(Integer workspaceId){
		WorkspaceLog w;
		try{
			w = (WorkspaceLog) entityManager.createQuery("from WorkspaceLog where id = :id")
												.setParameter("id", workspaceId)
												.getSingleResult();
		}catch(Exception e){
			return null;
		}
		
		return w;
	}
	
	private UserLog getUserLog(){
		if(this.userLog == null)
			this.userLog = (UserLog) entityManager.createQuery("from UserLog where id = 1") //Ricardo user
												.getSingleResult();
		return userLog;
	}
	
	private UserRole getUserRole(){
		if(this.userRole == null)
			this.userRole = (UserRole) entityManager.createQuery("from UserRole where id = 210")
													.getSingleResult();
		return userRole;
	}
}
