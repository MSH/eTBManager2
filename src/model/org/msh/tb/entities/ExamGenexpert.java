package org.msh.tb.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.msh.tb.entities.enums.GenexpertResult;
import org.msh.tb.transactionlog.Operation;
import org.msh.tb.transactionlog.PropertyLog;

@Entity
@Table(name="examgenexpert")
public class ExamGenexpert extends LaboratoryExamResult implements Serializable{
	private static final long serialVersionUID = 7672681749376963359L;
	
	@PropertyLog(operations={Operation.ALL})
	private GenexpertResult result;
	
	private GenexpertResult rifResult;

	public GenexpertResult getResult() {
		return result;
	}

	public void setResult(GenexpertResult result) {
		this.result = result;
	}

	/**
	 * @return the rifResult
	 */
	public GenexpertResult getRifResult() {
		return rifResult;
	}

	/**
	 * @param rifResult the rifResult to set
	 */
	public void setRifResult(GenexpertResult rifResult) {
		this.rifResult = rifResult;
	}
}
