/**
 * 
 */
package org.msh.tb.entities;

import org.msh.etbm.commons.transactionlog.Operation;
import org.msh.etbm.commons.transactionlog.mapping.PropertyLog;

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
	//if null will be deleted in any desktop
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="UNIT_TO_BE_DELETED_ID")
	@PropertyLog(operations={Operation.NEW, Operation.DELETE})
	private Tbunit unitToBeDeleted;

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

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to be set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	public Tbunit getUnitToBeDeleted() {
		return unitToBeDeleted;
	}

	public void setUnitToBeDeleted(Tbunit unitToBeDeleted) {
		this.unitToBeDeleted = unitToBeDeleted;
	}
}
