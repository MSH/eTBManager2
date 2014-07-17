package org.msh.tb.entities;

import org.msh.tb.entities.enums.MicroscopyResult;
import org.msh.tb.entities.enums.SampleType;
import org.msh.tb.transactionlog.Operation;
import org.msh.tb.transactionlog.PropertyLog;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="exammicroscopy")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="DISCRIMINATOR", discriminatorType=DiscriminatorType.STRING)
@DiscriminatorValue("gen")
public class ExamMicroscopy extends LaboratoryExamResult implements Serializable {
	private static final long serialVersionUID = 1514632458011926044L;

	@PropertyLog(operations={Operation.NEW, Operation.DELETE})
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
