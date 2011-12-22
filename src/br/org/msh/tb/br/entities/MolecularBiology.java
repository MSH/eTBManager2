package org.msh.tb.br.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.msh.tb.br.entities.enums.MolecularBiologyResult;
import org.msh.tb.entities.CaseData;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.Laboratory;
import org.msh.tb.transactionlog.Operation;
import org.msh.tb.transactionlog.PropertyLog;

@Entity
@Table(name="molecularbiology")
public class MolecularBiology extends CaseData implements Serializable {
	private static final long serialVersionUID = 1656875453491288619L;

	@Temporal(TemporalType.DATE)
	private Date dateRelease;

	@PropertyLog(operations={Operation.ALL})
	private MolecularBiologyResult result;

	@ManyToOne
	@JoinColumn(name="METHOD_ID")
	@PropertyLog(operations={Operation.NEW, Operation.EDIT})
	private FieldValue method;

	@ManyToOne
	@JoinColumn(name="LABORATORY_ID")
	@PropertyLog(operations={Operation.ALL})
	private Laboratory laboratory; 



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
}
