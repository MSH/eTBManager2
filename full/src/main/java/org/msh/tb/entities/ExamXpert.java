package org.msh.tb.entities;

import org.msh.tb.entities.enums.XpertResult;
import org.msh.tb.entities.enums.XpertRifResult;
import org.msh.tb.transactionlog.Operation;
import org.msh.tb.transactionlog.PropertyLog;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name="examxpert")
public class ExamXpert extends LaboratoryExam implements Serializable{
	private static final long serialVersionUID = 7672681749376963359L;

    @PropertyLog(operations={Operation.ALL})
	private XpertResult result;
	
	private XpertRifResult rifResult;

    @Override
    public ExamResult getExamResult() {
        if (result == null) {
            return ExamResult.UNDEFINED;
        }

        return result == XpertResult.TB_DETECTED? ExamResult.POSITIVE: ExamResult.NEGATIVE;
    }

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
