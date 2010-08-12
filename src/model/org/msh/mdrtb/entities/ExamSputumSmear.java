package org.msh.mdrtb.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.msh.mdrtb.entities.enums.SampleType;
import org.msh.mdrtb.entities.enums.SputumSmearResult;

@Entity
public class ExamSputumSmear extends LaboratoryExam implements Serializable {
	private static final long serialVersionUID = 1514632458011926044L;

	private SputumSmearResult result;
	
	private Integer numberOfAFB;

	@OneToOne(mappedBy="examSputumSmear")
	private PatientSample sample;


	private SampleType sampleType;
	
	public SputumSmearResult getResult() {
		return result;
	}

	public void setResult(SputumSmearResult result) {
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
	 * @return the sample
	 */
	@Override
	public PatientSample getSample() {
		return sample;
	}

	/**
	 * @param sample the sample to set
	 */
	@Override
	public void setSample(PatientSample sample) {
		this.sample = sample;
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
