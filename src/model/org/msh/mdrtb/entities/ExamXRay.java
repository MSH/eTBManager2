package org.msh.mdrtb.entities;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.msh.mdrtb.entities.enums.XRayBaseline;
import org.msh.mdrtb.entities.enums.XRayEvolution;
import org.msh.mdrtb.entities.enums.XRayResult;

/**
 * Stores information about an X-Ray exam of a case
 * @author Ricardo Lima
 *
 */
@Entity
public class ExamXRay extends CaseData {

	private XRayResult result;
	
	private XRayEvolution evolution;
	
	private XRayBaseline baseline;
	
	private Boolean destruction;
	
	@ManyToOne
	@JoinColumn(name="PRESENTATION_ID")
	private FieldValue presentation;

	/**
	 * @return the baseline
	 */
	public XRayBaseline getBaseline() {
		return baseline;
	}

	/**
	 * @param baseline the baseline to set
	 */
	public void setBaseline(XRayBaseline baseline) {
		this.baseline = baseline;
	}

	/**
	 * Return the X-Ray result
	 * @return the result
	 */
	public XRayResult getResult() {
		return result;
	}

	/**
	 * Set the X-Ray result
	 * @param result the result to set
	 */
	public void setResult(XRayResult result) {
		this.result = result;
	}

	/**
	 * @return the evolution
	 */
	public XRayEvolution getEvolution() {
		return evolution;
	}

	/**
	 * @param evolution the evolution to set
	 */
	public void setEvolution(XRayEvolution evolution) {
		this.evolution = evolution;
	}

	/**
	 * @param presentation the presentation to set
	 */
	public void setPresentation(FieldValue presentation) {
		this.presentation = presentation;
	}

	/**
	 * @return the presentation
	 */
	public FieldValue getPresentation() {
		return presentation;
	}

	/**
	 * @return the destruction
	 */
	public Boolean getDestruction() {
		return destruction;
	}

	/**
	 * @param destruction the destruction to set
	 */
	public void setDestruction(Boolean destruction) {
		this.destruction = destruction;
	}
}
