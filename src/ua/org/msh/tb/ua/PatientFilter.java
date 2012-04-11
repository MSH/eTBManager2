package org.msh.tb.ua;

import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

/**
 * Define filter used in patient search while init new case
 * @author alexey
 *
 */
@Name("patientFilter")
@Scope(ScopeType.SESSION)
@Synchronized(timeout=10000L)
@BypassInterceptors
public class PatientFilter {
	
	private String name=null;

	private String middleName=null;

	private String lastName=null;

	private String securityNumber=null;

	private String motherName=null;

	private Date birthDate=null;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
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

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}
	public String getNameLike() {
		return (getName() == null) || (getName().isEmpty()) ? null: "%" + getName().toUpperCase() + "%"; 
	}

	public String getMiddleNameLike() {
		return (getMiddleName() == null) || (getMiddleName().isEmpty()) ? null: "%" + getMiddleName().toUpperCase() + "%"; 
	}

	public String getLastNameLike() {
		return (getLastName() == null) || (getLastName().isEmpty()) ? null: "%" + getLastName().toUpperCase() + "%"; 
	}
	
	
}
