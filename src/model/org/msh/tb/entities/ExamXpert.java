package org.msh.tb.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.msh.tb.entities.enums.XpertResult;
import org.msh.tb.entities.enums.XpertRifResult;
import org.msh.tb.transactionlog.Operation;
import org.msh.tb.transactionlog.PropertyLog;

@Entity
@Table(name="examxpert")
public class ExamXpert extends LaboratoryExamResult implements Serializable{
	private static final long serialVersionUID = 7672681749376963359L;
	
	@PropertyLog(operations={Operation.ALL})
	private XpertResult result;
	
	private XpertRifResult rifResult;

	public XpertResult getResult() {
		return result;
	}

	public void setResult(XpertResult result) {
		this.result = result;
	}

	/**
	 * @return the rifResult
	 */
	public XpertRifResult getRifResult() {
		return rifResult;
	}

	/**
	 * @param rifResult the rifResult to set
	 */
	public void setRifResult(XpertRifResult rifResult) {
		this.rifResult = rifResult;
	}
}
