package org.msh.tb.ng.entities;

import org.msh.etbm.commons.transactionlog.mapping.PropertyLog;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.MedicalExamination;
import org.msh.tb.entities.enums.YesNoType;
import org.msh.tb.ng.entities.enums.Qualification;

import javax.persistence.*;

@Entity
@DiscriminatorValue("ng")
public class MedicalExaminationNG extends MedicalExamination{

	private static final long serialVersionUID = 1942104893681482326L;
	
    private Qualification Qualification;

    @Column(length=100)
    private String otherQualifiedProfessional;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="SIDE_EFFECT")
    @PropertyLog(messageKey="TbField.SIDEEFFECT")
    private FieldValue sideeffect;

    private YesNoType patientReferred;

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

	public FieldValue getSideeffect() {
		return sideeffect;
	}
	
	public void setSideeffect(FieldValue sideeffect) {
		this.sideeffect = sideeffect;
	}

	public YesNoType getPatientReferred() {
		return patientReferred;
	}
	
	public void setPatientReferred(YesNoType patientReferred) {
		this.patientReferred = patientReferred;
	}

}
