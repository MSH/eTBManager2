/**
 * 
 */
package org.msh.tb.sync;

/**
 * Store information sent from the client about deleted entities. The content
 * is the result of serialization of the XML file
 * 
 * @author Ricardo Memoria
 *
 */
public class DeletedEntity {

	private String entityName;
	private int entityId;
	
	/**
	 * @return the entityName
	 */
	public String getEntityName() {
		return entityName;
	}
	/**
	 * @param entityName the entityName to set
	 */
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}
	/**
	 * @return the entityId
	 */
	public int getEntityId() {
		return entityId;
	}
	/**
	 * @param entityId the entityId to set
	 */
	public void setEntityId(int entityId) {
		this.entityId = entityId;
	}

}
