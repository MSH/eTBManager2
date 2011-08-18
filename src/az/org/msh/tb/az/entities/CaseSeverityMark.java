package org.msh.tb.az.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.msh.tb.entities.FieldValue;

@Entity
@Table(name="caseseveritymark")
public class CaseSeverityMark {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

	@ManyToOne
	@JoinColumn(name="SYMPTOM_ID")
	private FieldValue severityMark;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="CASE_ID")
	private TbCaseAZ tbcase;

	@Column(length=100)
	private String comments;

	@Transient
	private boolean checked;

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
	public TbCaseAZ getTbcase() {
		return tbcase;
	}

	/**
	 * @param tbcase the tbcase to set
	 */
	public void setTbcase(TbCaseAZ tbcase) {
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
	 * @return the checked
	 */
	public boolean isChecked() {
		return checked;
	}

	/**
	 * @param checked the checked to set
	 */
	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	/**
	 * @return the severityMark
	 */
	public FieldValue getSeverityMark() {
		return severityMark;
	}

	/**
	 * @param severityMark the severityMark to set
	 */
	public void setSeverityMark(FieldValue severityMark) {
		this.severityMark = severityMark;
	}
}
