package org.msh.tb.entities;

import org.hibernate.validator.NotNull;
import org.jboss.seam.international.LocaleSelector;
import org.msh.etbm.commons.transactionlog.Operation;
import org.msh.etbm.commons.transactionlog.mapping.PropertyLog;
import org.msh.tb.entities.enums.ExamStatus;
import org.msh.tb.sync.Sync;
import org.msh.tb.workspaces.customizable.WorkspaceCustomizationService;

import javax.persistence.*;
import javax.validation.constraints.Past;
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

    public enum ExamResult { UNDEFINED, POSITIVE, NEGATIVE };

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;


	@Temporal(TemporalType.DATE)
	@NotNull
	@Past
	@PropertyLog(messageKey="PatientSample.dateCollected", operations={Operation.NEW, Operation.DELETE})
	@Sync(keyAttribute = true)
	private Date dateCollected;
	
	@Column(length=50)
	@PropertyLog(messageKey="PatientSample.sampleNumber", operations={Operation.NEW, Operation.DELETE})
	private String sampleNumber;


/*
    @ManyToOne(fetch = FetchType.LAZY)
    @PropertyLog(logEntityFields = true)
    @JoinColumn(name="sample_id")
    private PatientSample sample;
*/

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="request_id")
    private ExamRequest request;

	@Column(length=250)
	@PropertyLog(messageKey="global.comments")
	private String comments;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="CASE_ID")
	@PropertyLog(ignore=true)
	@NotNull
	@Sync(keyAttribute = true, internalKeyAttribute = "id")
	private TbCase tbcase;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="LABORATORY_ID")
	@PropertyLog(messageKey="Laboratory", operations={Operation.NEW, Operation.DELETE})
	@Sync(keyAttribute = true, internalKeyAttribute = "id")
	private Laboratory laboratory;

	@Temporal(TemporalType.DATE)
	@PropertyLog(messageKey="cases.exams.dateRelease", operations={Operation.NEW})
	@Past
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
     * Return a common way if the result is positive, negative or not informed
     * @return
     */
    public abstract ExamResult getExamResult();

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
     * Check if the given date collected and sample number are the same as in the
     * exam data
     * @param dateCollected the date sample was collected
     * @param sampleNumber the given number of the sample collected
     * @return true if its the same as in the exam
     */
    public boolean isSameSample(Date dateCollected, String sampleNumber) {
        if (this.dateCollected != dateCollected) {
            if ((this.dateCollected == null) || (dateCollected == null)) {
                return false;
            }

            if (!this.dateCollected.equals(dateCollected)) {
                return false;
            }
        }

        if (this.sampleNumber == sampleNumber) {
            return true;
        }

        if ((this.sampleNumber == null) || (sampleNumber == null)) {
            return false;
        }

        return this.sampleNumber.equals(sampleNumber);
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
        return dateCollected;
    }


    /**
     * Set date collected
     * @param dt
     */
    public void setDateCollected(Date dt) {
        this.dateCollected = dt;
    }

    /**
     * Return the patient sample number
     * @return
     */
    public String getSampleNumber() {
        return sampleNumber;
    }

    /**
     * Change the patient sample number
     * @param spnumber the sample number
     */
    public void setSampleNumber(String spnumber) {
        this.sampleNumber = spnumber;
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
		Date dt = getDateCollected();
		TbCase tbcase = getTbcase();
		String s = tbcase != null? tbcase.getPatient().getFullName() : null;

		if (s == null) {
			return super.toString();
		}

		if (dt != null) {
			Locale locale = LocaleSelector.instance().getLocale();
			DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
			s = dateFormat.format(dt) + " - " + s;
		}

		return s;
	}

	public TbCase getTbcase() {
		return tbcase;
	}

	public void setTbcase(TbCase tbcase) {
		this.tbcase = tbcase;
	}

/*
	pub Date getDateCollected() {
		return dateCollected;
	}

	pub void setDateCollected(Date dateCollected) {
		this.dateCollected = dateCollected;
	}

	pub String getSampleNumber() {
		return sampleNumber;
	}

	pub void setSampleNumber(String sampleNumber) {
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

    public ExamStatus getStatus() {
        return status;
    }

    public void setStatus(ExamStatus status) {
        this.status = status;
    }

    public ExamRequest getRequest() {
        return request;
    }

    public void setRequest(ExamRequest request) {
        this.request = request;
    }
}
