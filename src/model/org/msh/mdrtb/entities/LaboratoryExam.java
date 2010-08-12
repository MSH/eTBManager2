package org.msh.mdrtb.entities;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.jboss.seam.international.LocaleSelector;


@MappedSuperclass
public abstract class LaboratoryExam implements Serializable {
	private static final long serialVersionUID = 3229952267481224824L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

	@Column(length=250)
	private String comments;

	@ManyToOne
	@JoinColumn(name="LABORATORY_ID")
	private Laboratory laboratory;

	@Temporal(TemporalType.DATE)
	private Date dateRelease;
	
	@ManyToOne
	@JoinColumn(name="METHOD_ID")
	private FieldValue method;


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
	 * @return the comments
	 */
	public String getComments() {
		return comments;
	}

	/**
	 * @param comments the comments to set
	 */
	public void setComments(String comments) {
		this.comments = comments;
	}

	/**
	 * @return the laboratory
	 */
	public Laboratory getLaboratory() {
		return laboratory;
	}

	/**
	 * @param laboratory the laboratory to set
	 */
	public void setLaboratory(Laboratory laboratory) {
		this.laboratory = laboratory;
	}

	/**
	 * @return the dateRelease
	 */
	public Date getDateRelease() {
		return dateRelease;
	}

	/**
	 * @param dateRelease the dateRelease to set
	 */
	public void setDateRelease(Date dateRelease) {
		this.dateRelease = dateRelease;
	}

	/**
	 * @return the method
	 */
	public FieldValue getMethod() {
		return method;
	}

	/**
	 * @param method the method to set
	 */
	public void setMethod(FieldValue method) {
		this.method = method;
	}
	
	public PatientSample getSample() {
		return null;
	}
	
	public void setSample(PatientSample sample) {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		Locale locale = LocaleSelector.instance().getLocale();
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
		return dateFormat.format(getSample().getDateCollected()) + " - " + getSample().getTbcase().getPatient().getFullName();
	}
}
