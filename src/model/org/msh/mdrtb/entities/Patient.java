package org.msh.mdrtb.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.validator.NotNull;
import org.jboss.seam.Component;
import org.msh.mdrtb.entities.enums.Gender;
import org.msh.mdrtb.entities.enums.NameComposition;

/**
 * Store information about a patient
 * @author Ricardo Memoria
 *
 */
@Entity
public class Patient extends WSObject implements Serializable {
	private static final long serialVersionUID = 6137777841151141479L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

	@Column(length=100, name="PATIENT_NAME")
	@NotNull
	private String name;
	
	@Column(length=100)
	private String middleName;
	
	@Column(length=100)
	private String lastName;
	
	@Column(length=50)
	private String securityNumber;
	
	@Column(length=100)
	private String motherName;
	
	private Date birthDate;
	
	private Integer recordNumber;

	@NotNull
	private Gender gender;
	
	@Column(length=50)
	private String legacyId;


	public String getFullName() {
		Workspace ws = (Workspace)Component.getInstance("defaultWorkspace", true);
		return compoundName(ws);
	}
	
	public String compoundName(Workspace ws) {
		NameComposition comp = ws.getPatientNameComposition();

		switch (comp) {
		case FIRST_MIDDLE_LASTNAME:
			   return name + (middleName != null? " " + middleName: "") + (lastName != null? " " + lastName: "");

		case FULLNAME:
			return name;
		
		case FIRSTSURNAME:
			return name + (middleName != null? " " + middleName: "");
			
		case LAST_FIRST_MIDDLENAME:
			return (lastName != null? lastName + " ": "") + name + (middleName != null? " " + middleName: "");
			
		case SURNAME_FIRSTNAME:
			return (middleName != null? middleName + " ":"") + name;
		default:
		   return name + (middleName != null? " " + middleName: "") + (lastName != null? " " + lastName: "");
		}		
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public String getSecurityNumber() {
		return securityNumber;
	}

	public void setSecurityNumber(String securityNumber) {
		this.securityNumber = securityNumber;
	}

	public String getMotherName() {
		return motherName;
	}

	public void setMotherName(String motherName) {
		this.motherName = motherName;
	}

	public Integer getRecordNumber() {
		return recordNumber;
	}

	public void setRecordNumber(Integer recordNumber) {
		this.recordNumber = recordNumber;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getLegacyId() {
		return legacyId;
	}

	public void setLegacyId(String legacyId) {
		this.legacyId = legacyId;
	}

	/**
	 * @return the middleName
	 */
	public String getMiddleName() {
		return middleName;
	}

	/**
	 * @param middleName the middleName to set
	 */
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}
