package org.msh.tb.ua.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.msh.tb.entities.CaseData;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.Laboratory;
import org.msh.tb.transactionlog.Operation;
import org.msh.tb.transactionlog.PropertyLog;
import org.msh.tb.ua.entities.enums.MolecularBiologyResult;

@Entity
@Table(name="molecularbiology")
public class MolecularBiologyUA extends CaseData implements Serializable {
	private static final long serialVersionUID = 1656875453491288619L;

	@Temporal(TemporalType.DATE)
	@PropertyLog(operations={Operation.NEW, Operation.EDIT}, messageKey="cases.exams.dateRelease")
	private Date dateRelease;

	@PropertyLog(operations={Operation.ALL}, messageKey="uk_UA.MolecularBiology.result")
	private MolecularBiologyResult result;

	@ManyToOne
	@JoinColumn(name="METHOD_ID")
	@PropertyLog(operations={Operation.NEW, Operation.EDIT})
	private FieldValue method;

	@ManyToOne
	@JoinColumn(name="LABORATORY_ID")
	@PropertyLog(operations={Operation.ALL})
	private Laboratory laboratory; 
	
	//existing TB-micobacteries
	private boolean pcr;
	//resistance to substance
	private Boolean h;
	private Boolean r;
	private Boolean km;
	private Boolean cm;
	private Boolean e;
	private Boolean lfx;
	private Boolean mfx;

	/**
	 * Return true if at least one of substance by method of this object is positive
	 * */
	public boolean isResistance(){
		if (MolecularBiologyResult.GeneXpert.equals(result) && r==true)
			return true;
		if (MolecularBiologyResult.GenoTypeMTBDRplus.equals(result) && (r==true || h==true))
			return true;
		if (MolecularBiologyResult.GenoTypeMTBDRsl.equals(result) && (km==true || cm==true || e==true || lfx==true || mfx==true))
			return true;
		return false;
	}
	
	//=================GETTERS & SETTERS==================
	public FieldValue getMethod() {
		return method;
	}

	public void setMethod(FieldValue method) {
		this.method = method;
	}

	public Date getDateRelease() {
		return dateRelease;
	}

	public void setDateRelease(Date dateRelease) {
		this.dateRelease = dateRelease;
	}

	public Laboratory getLaboratory() {
		return laboratory;
	}

	public void setLaboratory(Laboratory laboratory) {
		this.laboratory = laboratory;
	}

	public MolecularBiologyResult getResult() {
		return result;
	}

	public void setResult(MolecularBiologyResult result) {
		this.result = result;
	}

	/**
	 * @return the pcr
	 */
	public boolean isPcr() {
		return pcr;
	}

	/**
	 * @param pcr the pcr to set
	 */
	public void setPcr(boolean pcr) {
		this.pcr = pcr;
	}

	/**
	 * @return the h
	 */
	public Boolean getH() {
		if (h == null)
			h = false;
		return h;
	}

	/**
	 * @param h the h to set
	 */
	public void setH(Boolean h) {
		this.h = h;
	}

	/**
	 * @return the r
	 */
	public Boolean getR() {
		if (r == null)
			r = false;
		return r;
	}

	/**
	 * @param r the r to set
	 */
	public void setR(Boolean r) {
		this.r = r;
	}

	/**
	 * @return the km
	 */
	public Boolean getKm() {
		if (km == null)
			km = false;
		return km;
	}

	/**
	 * @param km the km to set
	 */
	public void setKm(Boolean km) {
		this.km = km;
	}

	/**
	 * @return the cm
	 */
	public Boolean getCm() {
		if (cm == null)
			cm = false;
		return cm;
	}

	/**
	 * @param cm the cm to set
	 */
	public void setCm(Boolean cm) {
		this.cm = cm;
	}

	/**
	 * @return the e
	 */
	public Boolean getE() {
		if (e == null)
			e = false;
		return e;
	}

	/**
	 * @param e the e to set
	 */
	public void setE(Boolean e) {
		this.e = e;
	}

	/**
	 * @return the lfx
	 */
	public Boolean getLfx() {
		if (lfx == null)
			lfx = false;
		return lfx;
	}

	/**
	 * @param lfx the lfx to set
	 */
	public void setLfx(Boolean lfx) {
		this.lfx = lfx;
	}

	/**
	 * @return the mfx
	 */
	public Boolean getMfx() {
		if (mfx == null)
			mfx = false;
		return mfx;
	}

	/**
	 * @param mfx the mfx to set
	 */
	public void setMfx(Boolean mfx) {
		this.mfx = mfx;
	}
}
