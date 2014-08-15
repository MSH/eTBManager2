package org.msh.tb.laboratories;

import org.msh.tb.entities.PatientSample;

/**
 * Temporary information about samples requests
 * @author Ricardo Memoria
 *
 */
public class SampleRequest {

	private PatientSample sample;
	
	private int index;
	
	private boolean addExamCulture;
	private boolean addExamMicroscopy;
	private boolean addExamDST;
	private boolean addExamIdentification;
	


	public SampleRequest() {
		super();
	}

	public SampleRequest(PatientSample sample) {
		super();
		this.sample = sample;
	}

	/**
	 * @return the sample
	 */
	public PatientSample getSample() {
		return sample;
	}

	/**
	 * @param sample the sample to set
	 */
	public void setSample(PatientSample sample) {
		this.sample = sample;
	}

	/**
	 * @return the addExamCulture
	 */
	public boolean isAddExamCulture() {
		return addExamCulture;
	}

	/**
	 * @param addExamCulture the addExamCulture to set
	 */
	public void setAddExamCulture(boolean addExamCulture) {
		this.addExamCulture = addExamCulture;
	}

	/**
	 * @return the addExamMicroscopy
	 */
	public boolean isAddExamMicroscopy() {
		return addExamMicroscopy;
	}

	/**
	 * @param addExamMicroscopy the addExamMicroscopy to set
	 */
	public void setAddExamMicroscopy(boolean addExamMicroscopy) {
		this.addExamMicroscopy = addExamMicroscopy;
	}

	/**
	 * @return the addExamDST
	 */
	public boolean isAddExamDST() {
		return addExamDST;
	}

	/**
	 * @param addExamDST the addExamDST to set
	 */
	public void setAddExamDST(boolean addExamDST) {
		this.addExamDST = addExamDST;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * @return the addExamIdentification
	 */
	public boolean isAddExamIdentification() {
		return addExamIdentification;
	}

	/**
	 * @param addExamIdentification the addExamIdentification to set
	 */
	public void setAddExamIdentification(boolean addExamIdentification) {
		this.addExamIdentification = addExamIdentification;
	}
}
