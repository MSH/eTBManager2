package org.msh.tb.bd.entities;

import java.io.Serializable;

import javax.persistence.Entity;

import org.msh.mdrtb.entities.LaboratoryExamResult;
import org.msh.tb.bd.entities.enums.SkinTestResult;

@Entity
public class ExamSkin extends LaboratoryExamResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5916032251188255799L;
	private SkinTestResult result;

	public SkinTestResult getResult() {
		return result;
	}

	public void setResult(SkinTestResult result) {
		this.result = result;
	}


}
