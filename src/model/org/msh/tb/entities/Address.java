package org.msh.tb.entities;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.msh.tb.entities.enums.LocalityType;

@Embeddable
public class Address {
	
	@Column(length=100)
	private String address;

	@Column(length=100)
	private String complement;

	@Column(length=20)
	private String zipCode;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ADMINUNIT_ID")
	private AdministrativeUnit adminUnit;
	
	private LocalityType localityType;
	
	@Override
	public String toString() {
		String s = (address == null? "": address);

		if ((complement != null) && (!complement.isEmpty())) {
			if (!s.isEmpty())
				s += '\n';
			s += complement;
		}
		
		if ((zipCode != null) && (!zipCode.isEmpty())) {
			if (!s.isEmpty())
				s += '\n';
			s += zipCode;
		}
		
		if (adminUnit != null) {
			if (!s.isEmpty())
				s += "\n";
			s += adminUnit.getFullDisplayName();
		}
		
		return s;
	}
	
	/**
	 * Checks if the address is empty.
	 * Name changed because of EL expression limitations (bug)
	 * @return
	 */
	public boolean isEmptyy() {
		return (isStringEmpty(getAddress())) || (getAdminUnit() == null); 
	}

	public void copy(Address addr) {
		address = addr.getAddress();
		complement = addr.getComplement();
		zipCode = addr.zipCode;
		localityType = addr.localityType;
		adminUnit = addr.getAdminUnit();
	}
	
	private boolean isStringEmpty(String s) {
		return (s == null) || (s.isEmpty());
	}
	
	public String getComplement() {
		return complement;
	}

	public void setComplement(String complement) {
		this.complement = complement;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	
	public LocalityType getLocalityType() {
		return localityType;
	}

	public void setLocalityType(LocalityType localityType) {
		this.localityType = localityType;
	}


	/**
	 * @param adminUnit the adminUnit to set
	 */
	public void setAdminUnit(AdministrativeUnit adminUnit) {
		this.adminUnit = adminUnit;
	}

	/**
	 * @return the adminUnit
	 */
	public AdministrativeUnit getAdminUnit() {
		return adminUnit;
	}

}
