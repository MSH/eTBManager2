package org.msh.tb.entities;

import org.msh.etbm.commons.transactionlog.mapping.PropertyLog;

import javax.persistence.*;
import java.io.Serializable;


@Entity
@Table(name="countrystructure")
public class CountryStructure extends WSObject implements Serializable, SyncKey{
	private static final long serialVersionUID = -9182467866935116572L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;
	
	@Embedded
	@PropertyLog(messageKey="form.name")
	private LocalizedNameComp name = new LocalizedNameComp();
	
	@Column(name="STRUCTURE_LEVEL")
	@PropertyLog(messageKey="global.level")
	private int level;

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
	 * @param level the level to set
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return (name != null? name.toString() : super.toString());
	}
}
