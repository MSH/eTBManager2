package org.msh.tb.bd.cases.exams;

import org.msh.tb.bd.entities.enums.SkinTestResult;
import org.msh.tb.entities.LaboratoryExam;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name="examskin")
public class ExamSkin extends LaboratoryExam implements Serializable {

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


    @Override
    public ExamResult getExamResult() {
        return null;
    }
}
