package org.msh.tb.entities;

import org.msh.tb.entities.enums.DstResult;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="examdstresult")
public class ExamDSTResult implements Serializable, SyncKey {
	private static final long serialVersionUID = -5594762900664251756L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

	@ManyToOne
	@JoinColumn(name="SUBSTANCE_ID")
	private Substance substance;
	
	@ManyToOne
	@JoinColumn(name="EXAM_ID")
	private ExamDST exam;
	
	private DstResult result;
	
	@Transient
	// Ricardo: TEMPORARY UNTIL A SOLUTION IS FOUND. Just to attend a request from the XML data model to
	// map an XML node to a property in the model
	private Integer clientId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExamDSTResult that = (ExamDSTResult) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (substance != null ? !substance.equals(that.substance) : that.substance != null) return false;
        if (exam != null ? !exam.equals(that.exam) : that.exam != null) return false;
        if (result != that.result) return false;
        return !(clientId != null ? !clientId.equals(that.clientId) : that.clientId != null);

    }

    @Override
    public int hashCode() {
        int result1 = id != null ? id.hashCode() : 0;
        result1 = 31 * result1 + (substance != null ? substance.hashCode() : 0);
        result1 = 31 * result1 + (exam != null ? exam.hashCode() : 0);
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        result1 = 31 * result1 + (clientId != null ? clientId.hashCode() : 0);
        return result1;
    }

    /**
	 * @return
	 */
	public Integer getClientId() {
		return clientId;
	}
	
	/**
	 * @param clientId
	 */
	public void setClientId(Integer clientId) {
		this.clientId = clientId;
	}


	public Substance getSubstance() {
		return substance;
	}

	public void setSubstance(Substance substante) {
		this.substance = substante;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public DstResult getResult() {
		return result;
	}

	public void setResult(DstResult result) {
		this.result = result;
	}

	/**
	 * @return the exam
	 */
	public ExamDST getExam() {
		return exam;
	}

	/**
	 * @param exam the exam to set
	 */
	public void setExam(ExamDST exam) {
		this.exam = exam;
	}
}
