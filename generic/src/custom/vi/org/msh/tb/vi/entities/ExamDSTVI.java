package org.msh.tb.vi.entities;


import org.msh.tb.entities.ExamDST;
import org.msh.tb.vi.MtbDetected;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("vi")
public class ExamDSTVI extends ExamDST {
	private static final long serialVersionUID = -6637563136130597290L;

	private MtbDetected mtbDetected;

	/**
	 * @return the mtbDetected
	 */
	public MtbDetected getMtbDetected() {
		return mtbDetected;
	}

	/**
	 * @param mtbDetected the mtbDetected to set
	 */
	public void setMtbDetected(MtbDetected mtbDetected) {
		this.mtbDetected = mtbDetected;
	}
}
