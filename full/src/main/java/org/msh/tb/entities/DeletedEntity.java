/**
 * 
 */
package org.msh.tb.entities;

import javax.persistence.*;

/**
 * Store information sent from the client about deleted entities. The content
 * is the result of serialization of the XML file
 * 
 * @author Ricardo Memoria
 *
 */
@Entity
@Table(name="deletedentity")
public class DeletedEntity {

	@Id
	@GeneratedValue(strategy= GenerationType.AUTO)
	private Integer id;

	private String entityName;
	private int entityId;

	public DeletedEntity(){};

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

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
}
