package org.msh.mdrtb.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.validator.NotNull;

@Entity
public class CaseComorbidity {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name="CASE_ID")
	@NotNull
	private TbCase tbcase;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="COMORBIDITY_ID")
	@NotNull
	FieldValue comorbidity;
	
	@Column(length=200)
	private String comment;


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
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
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
	 * @return the comorbidity
	 */
	public FieldValue getComorbidity() {
		return comorbidity;
	}

	/**
	 * @param comorbidity the comorbidity to set
	 */
	public void setComorbidity(FieldValue comorbidity) {
		this.comorbidity = comorbidity;
	} 
}
