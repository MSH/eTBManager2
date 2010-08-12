package org.msh.mdrtb.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.msh.mdrtb.entities.enums.CultureResult;
import org.msh.mdrtb.entities.enums.SampleType;

@Entity
public class ExamCulture extends LaboratoryExam implements Serializable {
	private static final long serialVersionUID = 1470269499087718301L;

	private CultureResult result;

	private Integer numberOfColonies;

	@OneToOne(mappedBy="examCulture")
	private PatientSample sample;
	
	private SampleType sampleType;


	public CultureResult getResult() {
		return result;
	}

	public void setResult(CultureResult result) {
		this.result = result;
	}

	/**
	 * @return the numberOfColonies
	 */
	public Integer getNumberOfColonies() {
		return numberOfColonies;
	}

	/**
	 * @param numberOfColonies the numberOfColonies to set
	 */
	public void setNumberOfColonies(Integer numberOfColonies) {
		this.numberOfColonies = numberOfColonies;
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
