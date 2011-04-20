package org.msh.tb.na.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.msh.mdrtb.entities.FieldValue;
import org.msh.mdrtb.entities.TbCase;
import org.msh.tb.log.FieldLog;

@Entity
public class CaseDataNA {

	@Id
    private Integer id;

	@Temporal(TemporalType.DATE)
	@Column(name="EVENT_DATE")
	private Date date;
	
	@Lob
	private String comments;
	
	@OneToOne(cascade={CascadeType.ALL})
	@PrimaryKeyJoinColumn
	@FieldLog(logEntityFields=true)	
	private TbCase tbcase;
	
	@OneToMany(cascade={CascadeType.ALL}, mappedBy="caseData")
	@FieldLog(ignore=true)
	private List<CaseSideEffectNA> sideEffects = new ArrayList<CaseSideEffectNA>();
	
	
	/**
	 * Search for side effect data by the side effect
	 * @param sideEffect - FieldValue object representing the side effect
	 * @return - CaseSideEffect instance containing side effect data of the case, or null if there is no side effect data
	 */
	public CaseSideEffectNA findSideEffectData(FieldValue sideEffect) {
		for (CaseSideEffectNA se: getSideEffects()) {
			if (se.getSideEffect().equals(sideEffect))
				return se;
		}
		return null;
	}	

	public List<CaseSideEffectNA> getSideEffects() {
		return sideEffects;
	}

	public void setSideEffects(List<CaseSideEffectNA> sideEffects) {
		this.sideEffects = sideEffects;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comment) {
		this.comments = comment;
	}

	public TbCase getTbcase() {
		return tbcase;
	}

	public void setTbcase(TbCase tbcase) {
		this.tbcase = tbcase;
	}




}
