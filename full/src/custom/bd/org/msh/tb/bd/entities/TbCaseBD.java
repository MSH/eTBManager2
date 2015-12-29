package org.msh.tb.bd.entities;

import org.msh.etbm.commons.transactionlog.mapping.PropertyLog;
import org.msh.tb.bd.entities.enums.Occupation;
import org.msh.tb.bd.entities.enums.PulmonaryTypesBD;
import org.msh.tb.bd.entities.enums.SalaryRange;
import org.msh.tb.bd.entities.enums.SmearStatus;
import org.msh.tb.entities.CaseSideEffect;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.LaboratoryExam;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.YesNoType;
import org.msh.tb.sync.Sync;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Entity
@Table(name="tbcasebd")
public class TbCaseBD extends TbCase{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7151770665202536352L;
	
	private YesNoType bcgScar;
	
	private Occupation occupation;
	
	private SalaryRange salary;
	
	private PulmonaryTypesBD pulmonaryTypesBD;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="PATIENTREFTO_ID")
    @PropertyLog(messageKey="MedicalExamination.ReferredTo")
	@Sync(keyAttribute = true, internalKeyAttribute = "id")
    private FieldValue patientRefToFv;

    @Column(length=100)
    private String referredToUnitName;

    @Temporal(TemporalType.DATE)
    @Column(name="REF_TO_DATE")
    private Date refToDate;

	private SmearStatus followUpSmearStatus;

	@OneToMany(cascade={CascadeType.ALL}, mappedBy="tbcase", fetch=FetchType.LAZY)
	private List<ExamSkin> examsSkin = new ArrayList<ExamSkin>();

	@OneToMany(cascade={CascadeType.ALL}, mappedBy="tbcase", fetch=FetchType.LAZY)
	private List<ExamBiopsy> examsBiopsy = new ArrayList<ExamBiopsy>();

    public FieldValue getPatientRefToFv() {
        return patientRefToFv;
    }

    public void setPatientRefToFv(FieldValue patientRefToFv) {
        this.patientRefToFv = patientRefToFv;
    }

    public String getReferredToUnitName() {
        return referredToUnitName;
    }

    public void setReferredToUnitName(String referredToUnitName) {
        this.referredToUnitName = referredToUnitName;
    }

    public Date getRefToDate() {
        return refToDate;
    }

    public void setRefToDate(Date refToDate) {
        this.refToDate = refToDate;
    }

    public SalaryRange getSalary() {
		return salary;
	}

	public void setSalary(SalaryRange salary) {
		this.salary = salary;
	}

	public Occupation getOccupation() {
		return occupation;
	}

	public void setOccupation(Occupation occupation) {
		this.occupation = occupation;
	}

	public YesNoType getBcgScar() {
		return bcgScar;
	}

	public void setBcgScar(YesNoType bcgScar) {
		this.bcgScar = bcgScar;
	}

    /**
	 * Search for side effect data by the side effect
	 * @param sideEffect - FieldValue object representing the side effect
	 * @return - CaseSideEffect instance containing side effect data of the case, or null if there is no side effect data
	 */
	@Override
	public CaseSideEffectBD findSideEffectData(FieldValue sideEffect) {
		for (CaseSideEffect se: getSideEffects()) {
			if (se.getSideEffect().getValue().equals(sideEffect))
				return (CaseSideEffectBD)se;
		}
		return null;
	}

	public PulmonaryTypesBD getPulmonaryTypesBD() {
		return pulmonaryTypesBD;
	}
	
	public void setPulmonaryTypesBD(PulmonaryTypesBD pulmonaryTypesBD) {
		this.pulmonaryTypesBD = pulmonaryTypesBD;
	}

	public SmearStatus getFollowUpSmearStatus() {
		return followUpSmearStatus;
	}

	public void setFollowUpSmearStatus(SmearStatus followUpSmearStatus) {
		this.followUpSmearStatus = followUpSmearStatus;
	}

	public List<ExamSkin> getExamsSkin() {
		return examsSkin;
	}

	public void setExamsSkin(List<ExamSkin> examsSkin) {
		this.examsSkin = examsSkin;
	}

	public List<ExamBiopsy> getExamsBiopsy() {
		return examsBiopsy;
	}

	public void setExamsBiopsy(List<ExamBiopsy> examsBiopsy) {
		this.examsBiopsy = examsBiopsy;
	}

	@Override
	public List<LaboratoryExam> getAllLaboratoryExams(){
		List<LaboratoryExam> list = super.getAllLaboratoryExams();

		for(LaboratoryExam l : getExamsSkin())
			list.add(l);
		for(LaboratoryExam l : getExamsBiopsy())
			list.add(l);

		return list;
	}
}
