package org.msh.tb.bd.cases.exams;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.msh.tb.bd.entities.enums.SkinTestResult;
import org.msh.tb.entities.LaboratoryExamResult;

@Entity
@Table(name="examskin")
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
