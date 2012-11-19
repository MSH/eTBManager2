package org.msh.tb.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.validator.NotNull;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.transactionlog.Operation;
import org.msh.tb.transactionlog.PropertyLog;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="DISCRIMINATOR", discriminatorType=DiscriminatorType.STRING)
@DiscriminatorValue("gen")
@Table(name="tbcontact")
public class TbContact implements Serializable {
	private static final long serialVersionUID = -6862380284209711375L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name="CASE_ID")
	@NotNull
	private TbCase tbcase;

	@PropertyLog(operations={Operation.ALL})
	private String name;
	
	@PropertyLog(messageKey="Gender")
	private Gender gender;
	
	@PropertyLog(messageKey="TbCase.age", operations={Operation.NEW})
	private String age;
	
	//VR: adding 'date of examination'
	private Date dateOfExamination;
	
	@ManyToOne
	@JoinColumn(name="CONTACTTYPE_ID")
	@PropertyLog(operations={Operation.NEW})
	private FieldValue contactType;
	
	private boolean examinated;
	
	@ManyToOne
	@JoinColumn(name="CONDUCT_ID")
	private FieldValue conduct;

	@Lob
	@PropertyLog(messageKey="global.comments")
	private String comments;
	
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
}
