package org.msh.tb.entities;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.validator.NotNull;
import org.jboss.seam.international.LocaleSelector;
import org.msh.tb.transactionlog.Operation;
import org.msh.tb.transactionlog.PropertyLog;
import org.msh.tb.workspaces.customizable.WorkspaceCustomizationService;


/**
 * Base class to all laboratory exam results stored 
 * @author Ricardo Memoria
 *
 */
@MappedSuperclass
public abstract class LaboratoryExamResult implements Serializable, Transactional {
	private static final long serialVersionUID = 3229952267481224824L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

	@Temporal(TemporalType.DATE)
	@NotNull
	@PropertyLog(messageKey="PatientSample.dateCollected", operations={Operation.NEW, Operation.DELETE})
	private Date dateCollected;
	
	@Column(length=50)
	@PropertyLog(messageKey="PatientSample.sampleNumber", operations={Operation.NEW, Operation.DELETE})
	private String sampleNumber;
	
	@Column(length=250)
	@PropertyLog(messageKey="global.comments")
	private String comments;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="CASE_ID")
	@PropertyLog(ignore=true)
	private TbCase tbcase;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="LABORATORY_ID")
	@PropertyLog(messageKey="Laboratory", operations={Operation.NEW, Operation.DELETE})
	private Laboratory laboratory;

	@Temporal(TemporalType.DATE)
	@PropertyLog(messageKey="cases.exams.dateRelease", operations={Operation.NEW})
	private Date dateRelease;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="METHOD_ID")
	@PropertyLog(messageKey="cases.exams.method", operations={Operation.NEW})
	private FieldValue method;
	
	/**
	 * Point to the transaction log that contains information about the last time this entity was changed (updated or created)
	 */
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="lastTransaction_ID")
	@PropertyLog(ignore=true)
	private TransactionLog lastTransaction;
	
	/**
	 * Point to the transaction log that contains information about the creation of this entity
	 */
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="createTransaction_ID")
	@PropertyLog(ignore=true)
	private TransactionLog createTransaction;

	@Transient
	// Ricardo: TEMPORARY UNTIL A SOLUTION IS FOUND. Just to attend a request from the XML data model to
	// map an XML node to a property in the model
	private Integer clientId;
	
	/**
	 * @return
	 */
	public Integer getClientId() {
		return clientId;
	}
	
	/**
	 * @param clientId
	 */
	public void setClientId(Integer clientId) {
		this.clientId = clientId;
	}


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
		WorkspaceCustomizationService wsservice = WorkspaceCustomizationService.instance();
		return wsservice.getExamControl().getMonthDisplay(tbcase, getDateCollected());
	}


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


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		Locale locale = LocaleSelector.instance().getLocale();
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
		return dateFormat.format(getDateCollected()) + " - " + getTbcase().getPatient().getFullName();
	}

	public TbCase getTbcase() {
		return tbcase;
	}

	public void setTbcase(TbCase tbcase) {
		this.tbcase = tbcase;
	}

	public Date getDateCollected() {
		return dateCollected;
	}

	public void setDateCollected(Date dateCollected) {
		this.dateCollected = dateCollected;
	}

	public String getSampleNumber() {
		return sampleNumber;
	}

	public void setSampleNumber(String sampleNumber) {
		this.sampleNumber = sampleNumber;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.entities.Transactional#getLastTransaction()
	 */
	@Override
	public TransactionLog getLastTransaction() {
		return lastTransaction;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.entities.Transactional#getCreateTransaction()
	 */
	@Override
	public TransactionLog getCreateTransaction() {
		return createTransaction;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.entities.Transactional#setLastTransaction(org.msh.tb.entities.TransactionLog)
	 */
	@Override
	public void setLastTransaction(TransactionLog transactionLog) {
		this.lastTransaction = transactionLog;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.entities.Transactional#setCreateTransaction(org.msh.tb.entities.TransactionLog)
	 */
	@Override
	public void setCreateTransaction(TransactionLog transactionLog) {
		this.createTransaction = transactionLog;
	}
}
