package org.msh.tb.kh.entities;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.TbCase;
import org.msh.tb.transactionlog.Operation;
import org.msh.tb.transactionlog.PropertyLog;

@Entity
@Table(name="tbcasekh")
public class TbCaseKH extends TbCase{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7345762489106416485L;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="suspectType")
	@PropertyLog(messageKey="TbField.SUSPECT_TYPE", operations={Operation.NEW, Operation.DELETE})
	private FieldValue suspectType;

	public FieldValue getSuspectType() {
		return suspectType;
	}

	public void setSuspectType(FieldValue suspectType) {
		this.suspectType = suspectType;
	}
	
	

	
	
	
	
}
