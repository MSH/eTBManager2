package org.msh.tb.bd.cases.exams;

import org.msh.tb.bd.entities.enums.DotProvider;
import org.msh.tb.entities.MedicalExamination;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *
 * Records information about a medical examination of a case in bangladesh workspace
 *
 * @author Utkarsh Srivastava
 *
 */
@Entity
@Table(name="medicalexaminationbd")
@DiscriminatorValue("bd")
public class MedicalExaminationBd extends MedicalExamination {
	private static final long serialVersionUID = 2760727118134685773L;

	private DotProvider dotType;

	public DotProvider getDotType() {
		return dotType;
	}

	public void setDotType(DotProvider dotType) {
		this.dotType = dotType;
	}
	
	
	
	
}
