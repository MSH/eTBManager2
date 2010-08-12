package org.msh.mdrtb.entities;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.validator.NotNull;
import org.msh.tb.workspaces.WorkspaceCustomizationInterface;
import org.msh.tb.workspaces.customizable.ExamControl;

@Entity
public class PatientSample {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@Column(length=50)
	private String sampleNumber;
	
	@Temporal(TemporalType.DATE)
	private Date dateCollected;

	@ManyToOne
	@JoinColumn(name="CASE_ID")
	@NotNull
	private TbCase tbcase;
	
	@OneToOne(cascade={CascadeType.ALL})
	@JoinColumn(name="ExamCulture_ID")
	private ExamCulture examCulture;
	
	@OneToOne(cascade={CascadeType.ALL})
	@JoinColumn(name="ExamSputumSmear_ID")
	private ExamSputumSmear examSputumSmear;
	
	@OneToOne(cascade={CascadeType.ALL})
	@JoinColumn(name="ExamSusceptibilityTest_ID")
	private ExamSusceptibilityTest examSusceptibilityTest;

	
	/**
	 * Return month of treatment based on the start treatment date and the collected date
	 * @return
	 */
	public Integer getMonthTreatment() {
		Date dt = getDateCollected();
		
		if (getTbcase() == null)
			return null;

		return tbcase.getMonthTreatment(dt);
	}
	
	/**
	 * Returns a key related to the system messages to display the month
	 * @return
	 */
	public String getMonthDisplay() {
		ExamControl ctrl = WorkspaceCustomizationInterface.instance().getExamControl();
		return ctrl.getMonthDisplay(tbcase, dateCollected);
/*		Integer num = getMonthTreatment();
		
		if (num > 0) {
			return "global.monthth";  //Integer.toString(num);
		}
		
		Date dt = getDateCollected();
		Date dtReg = tbcase.getRegistrationDate();
		
		if ((dtReg == null) || (!dt.before(dtReg)))
			return "cases.exams.zero";
		else return "cases.exams.prevdt";
*/	}

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
	 * @return the dateCollected
	 */
	public Date getDateCollected() {
		return dateCollected;
	}

	/**
	 * @param dateCollected the dateCollected to set
	 */
	public void setDateCollected(Date dateCollected) {
		this.dateCollected = dateCollected;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dateCollected == null) ? 0 : dateCollected.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PatientSample other = (PatientSample) obj;
		if (dateCollected == null) {
			if (other.dateCollected != null)
				return false;
		} else if (!dateCollected.equals(other.dateCollected))
			return false;
		return true;
	}

	/**
	 * @return the tbcase
	 */
	public TbCase getTbcase() {
		return tbcase;
	}

	/**
	 * @param tbcase the tbcase to set
	 */
	public void setTbcase(TbCase tbcase) {
		this.tbcase = tbcase;
	}

	/**
	 * @return the sampleNumber
	 */
	public String getSampleNumber() {
		return sampleNumber;
	}

	/**
	 * @param sampleNumber the sampleNumber to set
	 */
	public void setSampleNumber(String sampleNumber) {
		this.sampleNumber = sampleNumber;
	}

	/**
	 * @return the examCulture
	 */
	public ExamCulture getExamCulture() {
		return examCulture;
	}

	/**
	 * @param examCulture the examCulture to set
	 */
	public void setExamCulture(ExamCulture examCulture) {
		this.examCulture = examCulture;
	}

	/**
	 * @return the examSputumSmear
	 */
	public ExamSputumSmear getExamSputumSmear() {
		return examSputumSmear;
	}

	/**
	 * @param examSputumSmear the examSputumSmear to set
	 */
	public void setExamSputumSmear(ExamSputumSmear examSputumSmear) {
		this.examSputumSmear = examSputumSmear;
	}

	/**
	 * @return the examSusceptibilityTest
	 */
	public ExamSusceptibilityTest getExamSusceptibilityTest() {
		return examSusceptibilityTest;
	}

	/**
	 * @param examSusceptibilityTest the examSusceptibilityTest to set
	 */
	public void setExamSusceptibilityTest(
			ExamSusceptibilityTest examSusceptibilityTest) {
		this.examSusceptibilityTest = examSusceptibilityTest;
	}
}
