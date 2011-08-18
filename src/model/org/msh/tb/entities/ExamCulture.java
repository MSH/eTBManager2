package org.msh.tb.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.msh.tb.entities.enums.CultureResult;
import org.msh.tb.entities.enums.SampleType;

@Entity
@Table(name="examculture")
public class ExamCulture extends LaboratoryExamResult implements Serializable {
	private static final long serialVersionUID = 1470269499087718301L;

	private CultureResult result;

	private Integer numberOfColonies;

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
