package org.msh.tb.entities;

import org.msh.etbm.commons.transactionlog.mapping.PropertyLog;

import javax.persistence.*;

@Entity
@Table(name="healthsystem")
public class HealthSystem extends WSObject implements SyncKey {
	private static final long serialVersionUID = 8006343659248774062L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

	@Embedded
	@PropertyLog(messageKey="form.name")
	private LocalizedNameComp name = new LocalizedNameComp();

	@Column(length=50)
	@PropertyLog(messageKey="global.legacyId")
	private String legacyId;

	@Transient
	// Ricardo: TEMPORARY UNTIL A SOLUTION IS FOUND. Just to attend a request from the XML data model to
	// map an XML node to a property in the model
	private Integer clientId;

	@Override
	public Integer getClientId() {
		return clientId;
	}

	@Override
	public void setClientId(Integer clientId) {
		this.clientId = clientId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (!(other instanceof HealthSystem))
			return false;
		
		return ((HealthSystem)other).getId().equals(id);
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return (name.toString() != null? name.toString(): super.toString());
	}


	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public LocalizedNameComp getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(LocalizedNameComp name) {
		this.name = name;
	}


	/**
	 * @return the legacyId
	 */
	public String getLegacyId() {
		return legacyId;
	}


	/**
	 * @param legacyId the legacyId to set
	 */
	public void setLegacyId(String legacyId) {
		this.legacyId = legacyId;
	}
}
