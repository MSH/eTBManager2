package org.msh.tb.entities;

import org.hibernate.validator.NotNull;
import org.msh.etbm.commons.transactionlog.Operation;
import org.msh.etbm.commons.transactionlog.mapping.PropertyLog;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.sync.Sync;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="DISCRIMINATOR", discriminatorType=DiscriminatorType.STRING)
@DiscriminatorValue("gen")
@Table(name="tbcontact")
public class TbContact implements Serializable, Transactional, SyncKey {
	private static final long serialVersionUID = -6862380284209711375L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name="CASE_ID")
	@NotNull
	@Sync(keyAttribute = true, internalKeyAttribute = "id")
	private TbCase tbcase;

	@PropertyLog(operations={Operation.ALL})
	@Sync(keyAttribute = true)
	private String name;
	
	@PropertyLog(messageKey="Gender")
	@Sync(keyAttribute = true)
	private Gender gender;
	
	@PropertyLog(messageKey="TbCase.age", operations={Operation.NEW})
	@Sync(keyAttribute = true)
	private String age;
	
	//VR: adding 'date of examination'
	private Date dateOfExamination;
	
	@ManyToOne
	@JoinColumn(name="CONTACTTYPE_ID")
	@PropertyLog(operations={Operation.NEW})
	@Sync(keyAttribute = true, internalKeyAttribute = "id")
	private FieldValue contactType;
	
	private boolean examinated;
	
	@ManyToOne
	@JoinColumn(name="CONDUCT_ID")
	private FieldValue conduct;

	@Lob
	@PropertyLog(messageKey="global.comments")
	private String comments;
	
	
	/**
	 * Point to the transaction log that contains information about the last time this entity was changed (updated or created)
	 */
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="lastTransaction_ID")
	@PropertyLog(ignore=true)
	private TransactionLog lastTransaction;
	
	
	// Ricardo: TEMPORARY UNTIL A SOLUTION IS FOUND. Just to attend a request from the XML data model to
	// map an XML node to a property in the model
	@Transient
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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the gender
	 */
	public Gender getGender() {
		return gender;
	}

	/**
	 * @param gender the gender to set
	 */
	public void setGender(Gender gender) {
		this.gender = gender;
	}

	/**
	 * @return the age
	 */
	public String getAge() {
		return age;
	}

	/**
	 * @param age the age to set
	 */
	public void setAge(String age) {
		this.age = age;
	}

	/**
	 * @return the contactType
	 */
	public FieldValue getContactType() {
		return contactType;
	}

	/**
	 * @param contactType the contactType to set
	 */
	public void setContactType(FieldValue contactType) {
		this.contactType = contactType;
	}

	/**
	 * @return the examinated
	 */
	public boolean isExaminated() {
		return examinated;
	}

	/**
	 * @param examinated the examinated to set
	 */
	public void setExaminated(boolean examinated) {
		this.examinated = examinated;
	}

	/**
	 * @return the conduct
	 */
	public FieldValue getConduct() {
		return conduct;
	}

	/**
	 * @param conduct the conduct to set
	 */
	public void setConduct(FieldValue conduct) {
		this.conduct = conduct;
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
	 * @return dateOfExamination
	 */
	public Date getDateOfExamination() {
		return dateOfExamination;
	}

	/**
	 * @param comments the comments to set
	 */
	public void setDateOfExamination(Date dateOfExamination) {
		this.dateOfExamination = dateOfExamination;
}

	/**
	 * @return the lastTransaction
	 */
	@Override
	public TransactionLog getLastTransaction() {
		return lastTransaction;
	}

	/**
	 * @param lastTransaction the lastTransaction to set
	 */
	@Override
	public void setLastTransaction(TransactionLog lastTransaction) {
		this.lastTransaction = lastTransaction;
	}
}
