package org.msh.tb.bd.entities;

import java.io.Serializable;

import javax.persistence.Entity;

import org.msh.mdrtb.entities.LaboratoryExamResult;
import org.msh.tb.bd.entities.enums.BiopsyResult;

@Entity
public class ExamBiopsy extends LaboratoryExamResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1704814081123568208L;

	private BiopsyResult result;

	public BiopsyResult getResult() {
		return result;
	}

	public void setResult(BiopsyResult result) {
		this.result = result;
	}


}
