package org.msh.tb.entities;

import org.hibernate.validator.NotNull;
import org.jboss.seam.international.LocaleSelector;
import org.msh.tb.entities.enums.ExamStatus;
import org.msh.tb.transactionlog.Operation;
import org.msh.tb.transactionlog.PropertyLog;
import org.msh.tb.workspaces.customizable.WorkspaceCustomizationService;

import javax.persistence.*;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Base class to all laboratory exam results stored 
 * @author Ricardo Memoria
 *
 */
@MappedSuperclass
public abstract class LaboratoryExam implements Serializable, Transactional, SyncKey {
	private static final long serialVersionUID = 3229952267481224824L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

/*
	@Temporal(TemporalType.DATE)
	@NotNull
	@PropertyLog(messageKey="PatientSample.dateCollected", operations={Operation.NEW, Operation.DELETE})
	private Date dateCollected;
	
	@Column(length=50)
	@PropertyLog(messageKey="PatientSample.sampleNumber", operations={Operation.NEW, Operation.DELETE})
	private String sampleNumber;
*/

    @ManyToOne(fetch = FetchType.LAZY)
    @PropertyLog(logEntityFields = true)
    @JoinColumn(name="sample_id")
    private PatientSample sample;

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

	@Transient
	// Ricardo: TEMPORARY UNTIL A SOLUTION IS FOUND. Just to attend a request from the XML data model to
	// map an XML node to a property in the model
	private Integer clientId;

    private ExamStatus status;
	
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
     * Return the date of sample collection
     * @return date value
     */
    public Date getDateCollected() {
        return sample != null? sample.getDateCollected(): null;
    }


    /**
     * Set date collected
     * @param dt
     */
    public void setDateCollected(Date dt) {
        if ((dt == null) && (sample == null)) {
            return;
        }

        if (sample == null) {
            sample = new PatientSample();
        }

        sample.setDateCollected(dt);
    }

    /**
     * Return the patient sample number
     * @return
     */
    public String getSampleNumber() {
        return sample != null? sample.getSampleNumber(): null;
    }

    /**
     * Change the patient sample number
     * @param spnumber the sample number
     */
    public void setSampleNumber(String spnumber) {
        if ((spnumber == null) && (sample == null)) {
            return;
        }

        if (sample != null) {
            sample = new PatientSample();
        }

        sample.setSampleNumber(spnumber);
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

/*
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
*/

	/* (non-Javadoc)
	 * @see org.msh.tb.entities.Transactional#getLastTransaction()
	 */
	@Override
	public TransactionLog getLastTransaction() {
		return lastTransaction;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.entities.Transactional#setLastTransaction(org.msh.tb.entities.TransactionLog)
	 */
	@Override
	public void setLastTransaction(TransactionLog transactionLog) {
		this.lastTransaction = transactionLog;
	}

    public PatientSample getSample() {
        return sample;
    }

    public void setPatientSample(PatientSample patientSample) {
        this.sample = patientSample;
    }

    public ExamStatus getStatus() {
        return status;
    }

    public void setStatus(ExamStatus status) {
        this.status = status;
    }
}
