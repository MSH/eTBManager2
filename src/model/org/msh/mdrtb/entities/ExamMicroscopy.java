package org.msh.mdrtb.entities;

import java.io.Serializable;

import javax.persistence.Entity;

import org.msh.mdrtb.entities.enums.SampleType;
import org.msh.mdrtb.entities.enums.MicroscopyResult;

@Entity
public class ExamMicroscopy extends LaboratoryExamResult implements Serializable {
	private static final long serialVersionUID = 1514632458011926044L;

	private MicroscopyResult result;
	
	private Integer numberOfAFB;


	private SampleType sampleType;
	
	public MicroscopyResult getResult() {
		return result;
	}

	public void setResult(MicroscopyResult result) {
		this.result = result;
	}

	/**
	 * @param numberOfAFB the numberOfAFB to set
	 */
	public void setNumberOfAFB(Integer numberOfAFB) {
		this.numberOfAFB = numberOfAFB;
	}

	/**
	 * @return the numberOfAFB
	 */
	public Integer getNumberOfAFB() {
		return numberOfAFB;
	}

	/**
	 * @return the sampleType
	 */
	public SampleType getSampleType() {
		return sampleType;
	}

	/**
	 * @param sampleType the sampleType to set
	 */
	public void setSampleType(SampleType sampleType) {
		this.sampleType = sampleType;
	}
}
