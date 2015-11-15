package org.msh.tb.bd.cases.exams;

import org.msh.etbm.commons.transactionlog.mapping.PropertyLog;
import org.msh.tb.bd.entities.enums.DotProvider;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.MedicalExamination;

import javax.persistence.*;
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
public class MedicalExaminationBD extends MedicalExamination {
	private static final long serialVersionUID = 2760727118134685773L;

	private DotProvider dotType;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="DOTTYPE_ID")
    @PropertyLog(messageKey="MedicalExamination.DOTProviderType")
    private FieldValue dotTypeFv;

	public DotProvider getDotType() {
		return dotType;
	}

	public void setDotType(DotProvider dotType) {
		this.dotType = dotType;
	}

    public FieldValue getDotTypeFv() { return dotTypeFv; }

    public void setDotTypeFv(FieldValue dotTypeFv) { this.dotTypeFv = dotTypeFv; }

}
