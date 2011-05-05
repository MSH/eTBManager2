package org.msh.tb.ph.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;

import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.TbCase;
import org.msh.tb.ph.entities.enums.PreEnrollmentOrigin;

@Entity
public class CaseDataPH {

	@Id
	private Integer id;

	// specific information by country
	@OneToOne(cascade={CascadeType.ALL})
	@PrimaryKeyJoinColumn
	private TbCase tbcase;

	@Column(length=50)
	private String preEnrollmentNumber;
	
	@Column(length=50)
	private String catIVRegNumber;

	/**
	 * Contact name of the patient
	 */
	@Column(length=100)
	private String personToNotify;
	
	/**
	 * Address of person to notify
	 */
	@Column(length=100)
	private String personAddress;
	
	@Column(length=100)
	private String personPhone;
	
	@ManyToOne
	@JoinColumn(name="SOURCEOFREFERRAL_ID")
	private FieldValue sourceOfReferral;
	
	@OneToMany(mappedBy="caseDataPH", cascade={CascadeType.ALL})
	private List<PhysicalExam> physicalExams = new ArrayList<PhysicalExam>();
	
	private PreEnrollmentOrigin preEnrollmentOrigin;
	

	/**
	 * Search for a physical exam result by its exam type 
	 * @param exam 
	 * @return PhysicalExam instance, or null if exam doesn't exist
	 */
	public PhysicalExam findExamResult(FieldValue exam) {
		for (PhysicalExam pe: getPhysicalExams()) {
			if (pe.getExam().equals(exam))
				return pe;
		}
		
		return null;
	}


	/**
	 * @return the physicalExams
	 */
	public List<PhysicalExam> getPhysicalExams() {
		return physicalExams;
	}

	/**
	 * @param physicalExams the physicalExams to set
	 */
	public void setPhysicalExams(List<PhysicalExam> physicalExams) {
		this.physicalExams = physicalExams;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param personToNotify the personToNotify to set
	 */
	public void setPersonToNotify(String personToNotify) {
		this.personToNotify = personToNotify;
	}

	/**
	 * @return the personToNotify
	 */
	public String getPersonToNotify() {
		return personToNotify;
	}

	/**
	 * @param sourceOfReferral the sourceOfReferral to set
	 */
	public void setSourceOfReferral(FieldValue sourceOfReferral) {
		this.sourceOfReferral = sourceOfReferral;
	}

	/**
	 * @return the sourceOfReferral
	 */
	public FieldValue getSourceOfReferral() {
		return sourceOfReferral;
	}

	/**
	 * @param tbcase the tbcase to set
	 */
	public void setTbcase(TbCase tbcase) {
		this.tbcase = tbcase;
	}

	/**
	 * @return the tbcase
	 */
	public TbCase getTbcase() {
		return tbcase;
	}

	/**
	 * @return the preEnrollmentNumber
	 */
	public String getPreEnrollmentNumber() {
		return preEnrollmentNumber;
	}

	/**
	 * @param preEnrollmentNumber the preEnrollmentNumber to set
	 */
	public void setPreEnrollmentNumber(String preEnrollmentNumber) {
		this.preEnrollmentNumber = preEnrollmentNumber;
	}

	/**
	 * @return the catIVRegNumber
	 */
	public String getCatIVRegNumber() {
		return catIVRegNumber;
	}

	/**
	 * @param catIVRegNumber the catIVRegNumber to set
	 */
	public void setCatIVRegNumber(String catIVRegNumber) {
		this.catIVRegNumber = catIVRegNumber;
	}

	/**
	 * @return the personAddress
	 */
	public String getPersonAddress() {
		return personAddress;
	}

	/**
	 * @param personAddress the personAddress to set
	 */
	public void setPersonAddress(String personAddress) {
		this.personAddress = personAddress;
	}

	/**
	 * @return the personPhone
	 */
	public String getPersonPhone() {
		return personPhone;
	}

	/**
	 * @param personPhone the personPhone to set
	 */
	public void setPersonPhone(String personPhone) {
		this.personPhone = personPhone;
	}


	/**
	 * @return the preEnrollmentOrigin
	 */
	public PreEnrollmentOrigin getPreEnrollmentOrigin() {
		return preEnrollmentOrigin;
	}


	/**
	 * @param preEnrollmentOrigin the preEnrollmentOrigin to set
	 */
	public void setPreEnrollmentOrigin(PreEnrollmentOrigin preEnrollmentOrigin) {
		this.preEnrollmentOrigin = preEnrollmentOrigin;
	}

}
