package org.msh.tb.kh.entities;

import org.msh.tb.entities.ExamDST;
import org.msh.tb.entities.enums.SampleType;

//@Entity
//@DiscriminatorValue("kh")
public class ExamDST_Kh extends ExamDST{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4683305582959181355L;
	
	private SampleType sampleType;

	public SampleType getSampleType() {
		return sampleType;
	}
	
	public void setSampleType(SampleType sampleType) {
		this.sampleType = sampleType;
	}
}
