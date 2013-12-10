package org.msh.tb.na.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.msh.tb.entities.CaseSideEffect;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.TbCase;

@Entity
@Table(name="tbcasena")
public class TbCaseNA extends TbCase{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2227321155884824528L;
	
	@OneToMany(cascade={CascadeType.MERGE, CascadeType.PERSIST}, mappedBy="tbcase")
	private List<CaseDispensingNA> dispna = new ArrayList<CaseDispensingNA>();
	
	private boolean hospitalized;

	@Temporal(TemporalType.DATE)
	private Date hospitalizedDt;
	
	@Temporal(TemporalType.DATE)
	private Date dischargeDt;
	
	@Temporal(TemporalType.DATE)
	@Column(name="EVENT_DATE")
	private Date date;	
	
	@Column(length=50)
	private String unitRegCode;
	
	
	private String comments;
	
	
	/**
	 * Search for side effect data by the side effect
	 * @param sideEffect - FieldValue object representing the side effect
	 * @return - CaseSideEffect instance containing side effect data of the case, or null if there is no side effect data
	 */
	@Override
	public CaseSideEffectNA findSideEffectData(FieldValue sideEffect) {
		for (CaseSideEffect se: getSideEffects()) {
			if (se.getSideEffect().getValue().getId().equals(sideEffect.getId()))
				return (CaseSideEffectNA)se;
		}
		return null;
	}	

	public List<CaseDispensingNA> getDispna() {
		return dispna;
	}



	public void setDispna(List<CaseDispensingNA> dispna) {
		this.dispna = dispna;
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

	public String getUnitRegCode() {
		return unitRegCode;
	}

	public void setUnitRegCode(String unitRegCode) {
		this.unitRegCode = unitRegCode;
	}

	public boolean isHospitalized() {
		return hospitalized;
	}

	public void setHospitalized(boolean hospitalized) {
		this.hospitalized = hospitalized;
	}

	public Date getHospitalizedDt() {
		return hospitalizedDt;
	}

	public void setHospitalizedDt(Date hospitalizedDt) {
		this.hospitalizedDt = hospitalizedDt;
	}

	public Date getDischargeDt() {
		return dischargeDt;
	}

	public void setDischargeDt(Date dischargeDt) {
		this.dischargeDt = dischargeDt;
	}

	
}
