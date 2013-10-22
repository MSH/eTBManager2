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

	
	private int clientId;
	private Integer serverId;
	private boolean newServerId;
	
	/**
	 * Default constructor
	 * @param clientId
	 * @param serverId
	 */
	public EntityKey(int clientId, Integer serverId) {
		super();
		this.clientId = clientId;
		this.serverId = serverId;
		newServerId = serverId == null;
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
	
}
