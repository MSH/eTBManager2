package org.msh.tb.bd.cases.exams;

import org.msh.tb.bd.entities.enums.BiopsyResult;
import org.msh.tb.entities.LaboratoryExam;
import org.msh.tb.entities.enums.SampleType;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name="exambiopsy")
public class ExamBiopsy extends LaboratoryExam implements Serializable {

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

    @Override
    public ExamResult getExamResult() {
        return null;
    }
}
