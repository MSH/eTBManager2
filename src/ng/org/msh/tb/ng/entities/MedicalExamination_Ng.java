package org.msh.tb.ng.entities;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.msh.tb.entities.MedicalExamination;
import org.msh.tb.ng.entities.enums.Qualification;

@Entity
@DiscriminatorValue("ng")
public class MedicalExamination_Ng extends MedicalExamination{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1942104893681482326L;
	
private Qualification Qualification;

@Column(length=100)
private String otherQualifiedProfessional;
	
	public Qualification getQualification() {
		return Qualification;
	}
	
	public void setQualification(Qualification qualification) {
		Qualification = qualification;
	}

	public String getOtherQualifiedProfessional() {
		return otherQualifiedProfessional;
	}

	public void setOtherQualifiedProfessional(String otherQualifiedProfessional) {
		this.otherQualifiedProfessional = otherQualifiedProfessional;
	}


}
