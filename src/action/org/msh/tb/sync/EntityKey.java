/**
 * 
 */
package org.msh.tb.sync;

/**
 * Store temporarily in memory the IDs of the client and server, and indicates
 * if the server ID was generated in the current context
 * 
 * @author Ricardo Memoria
 *
 */
public class EntityKey {

	private Class entityClass;
	private String entityName;
	private int clientId;
	private Integer serverId;
	private boolean newServerId;
	
	/**
	 * Default constructor
	 * @param clientId
	 * @param serverId
	 */
	public EntityKey(Class entityClass, int clientId, Integer serverId) {
		super();
		this.entityClass = entityClass;
		this.clientId = clientId;
		this.serverId = serverId;
		this.entityName = entityClass != null? entityClass.getSimpleName(): null;
		newServerId = serverId == null;
	}

	/**
	 * Return the entity class name
	 * @return String value
	 */
	public String getEntityName() {
		return entityName;
	}
	
	public void setEntityName(String name) {
		entityName = name;
	}
	
	/**
	 * @return the clientId
	 */
	public int getClientId() {
		return clientId;
	}
	/**
	 * @param clientId the clientId to set
	 */
	public void setClientId(int clientId) {
		this.clientId = clientId;
	}
	/**
	 * @return the serverId
	 */
	public Integer getServerId() {
		return serverId;
	}
	/**
	 * @param serverId the serverId to set
	 */
	public void setServerId(Integer serverId) {
		this.serverId = serverId;
	}
	/**
	 * @return the newServerId
	 */
	public boolean isNewServerId() {
		return newServerId;
	}
	/**
	 * @param newServerId the newServerId to set
	 */
	public void setNewServerId(boolean newServerId) {
		this.newServerId = newServerId;
	}

	/**
	 * @return the entityClass
	 */
	public Class getEntityClass() {
		return entityClass;
	}

	/**
	 * @param entityClass the entityClass to set
	 */
	public void setEntityClass(Class entityClass) {
		this.entityClass = entityClass;
	}
	
}
