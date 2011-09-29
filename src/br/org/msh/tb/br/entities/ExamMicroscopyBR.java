package org.msh.tb.br.entities;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.ExamMicroscopy;

@Entity
@DiscriminatorValue("br")
public class ExamMicroscopyBR extends ExamMicroscopy {
	private static final long serialVersionUID = 7430099062440521673L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="LAB_ADMINUNIT_ID")
	private AdministrativeUnit laboratoryAdminUnit;
	
	@Column(length=200)
	private String laboratoryName;


	/**
	 * @return the laboratoryName
	 */
	public String getLaboratoryName() {
		return laboratoryName;
	}

	/**
	 * @param laboratoryName the laboratoryName to set
	 */
	public void setLaboratoryName(String laboratoryName) {
		this.laboratoryName = laboratoryName;
	}

	public void setLaboratoryAdminUnit(AdministrativeUnit laboratoryAdminUnit) {
		this.laboratoryAdminUnit = laboratoryAdminUnit;
	}

	public AdministrativeUnit getLaboratoryAdminUnit() {
		return laboratoryAdminUnit;
	}
}
