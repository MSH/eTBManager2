package org.msh.tb.bd.cases.exams;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.msh.tb.bd.entities.enums.BiopsyResult;
import org.msh.tb.entities.LaboratoryExamResult;
import org.msh.tb.entities.enums.SampleType;

@Entity
@Table(name="exambiopsy")
public class ExamBiopsy extends LaboratoryExamResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1704814081123568208L;

	private BiopsyResult result;
	
	private SampleType sampleType;

	public BiopsyResult getResult() {
		return result;
	}

	public void setResult(BiopsyResult result) {
		this.result = result;
	}

	public SampleType getSampleType() {
		return sampleType;
	}

	public void setSampleType(SampleType sampleType) {
		this.sampleType = sampleType;
	}


}
