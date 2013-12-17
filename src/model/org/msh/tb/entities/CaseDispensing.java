package org.msh.tb.entities;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.validator.NotNull;
import org.msh.tb.transactionlog.PropertyLog;

@Entity
@Table(name="casedispensing")
public class CaseDispensing implements Serializable, Transactional, SyncKey {
	private static final long serialVersionUID = -4617800123206697915L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;
	
	@ManyToOne
	@JoinColumn(name="CASE_ID")
	@NotNull
	private TbCase tbcase;
	
	@Column(name="DISP_MONTH")
	private int month;
	
	@Column(name="DISP_YEAR")
	private int year;

	private int totalDays;

	@OneToOne
	@PrimaryKeyJoinColumn
	private CaseDispensingDays dispensingDays;
	
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


	public void updateTotalDays() {
		if (dispensingDays == null)
			new RuntimeException("No information about days dispensed in CaseInfo");
		totalDays = dispensingDays.getNumDispensingDays();
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
	 * @return the month
	 */
	public int getMonth() {
		return month;
	}

	/**
	 * @param month the month to set
	 */
	public void setMonth(int month) {
		this.month = month;
	}

	/**
	 * @return the year
	 */
	public int getYear() {
		return year;
	}

	/**
	 * @param year the year to set
	 */
	public void setYear(int year) {
		this.year = year;
	}

	/**
	 * @return the dispensingDays
	 */
	public CaseDispensingDays getDispensingDays() {
		return dispensingDays;
	}

	/**
	 * @param dispensingDays the dispensingDays to set
	 */
	public void setDispensingDays(CaseDispensingDays dispensingDays) {
		this.dispensingDays = dispensingDays;
	}

	/**
	 * @return the totalDays
	 */
	public int getTotalDays() {
		return totalDays;
	}

	/**
	 * @param totalDays the totalDays to set
	 */
	public void setTotalDays(int totalDays) {
		this.totalDays = totalDays;
	}

	/** {@inheritDoc}
	 */
	@Override
	public TransactionLog getLastTransaction() {
		return lastTransaction;
	}

	/** {@inheritDoc}
	 */
	@Override
	public void setLastTransaction(TransactionLog transactionLog) {
		this.lastTransaction = transactionLog;
	}
}
