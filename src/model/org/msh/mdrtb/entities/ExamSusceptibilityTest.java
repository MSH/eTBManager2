package org.msh.mdrtb.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class ExamSusceptibilityTest extends LaboratoryExam implements Serializable {
	private static final long serialVersionUID = -1911463378908689952L;

	@OneToMany(cascade={CascadeType.ALL}, mappedBy="exam")
	private List<ExamSusceptibilityResult> results = new ArrayList<ExamSusceptibilityResult>();

	private int numResistant;
	private int numSusceptible;
	private int numContaminated;

	@OneToOne(mappedBy="examSusceptibilityTest")
	private PatientSample sample;

	/**
	 * Search for a result by the substance 
	 * @param sub - Substance to be used to search result
	 * @return - Susceptibility result
	 */
	public ExamSusceptibilityResult findResultBySubstance(Substance sub) {
		for (ExamSusceptibilityResult res: results) {
			if (res.getSubstance().equals(sub)) {
				return res;
			}
		}
		return null;
	}

	public List<ExamSusceptibilityResult> getResults() {
		return results;
	}

	public void setResults(List<ExamSusceptibilityResult> results) {
		this.results = results;
	}

	/**
	 * @param numResistant the numResistant to set
	 */
	public void setNumResistant(int numResistant) {
		this.numResistant = numResistant;
	}

	/**
	 * @return the numResistant
	 */
	public int getNumResistant() {
		return numResistant;
	}

	/**
	 * @return the numSusceptible
	 */
	public int getNumSusceptible() {
		return numSusceptible;
	}

	/**
	 * @param numSusceptible the numSusceptible to set
	 */
	public void setNumSusceptible(int numSusceptible) {
		this.numSusceptible = numSusceptible;
	}

	/**
	 * @return the numContaminated
	 */
	public int getNumContaminated() {
		return numContaminated;
	}

	/**
	 * @param numContaminated the numContaminated to set
	 */
	public void setNumContaminated(int numContaminated) {
		this.numContaminated = numContaminated;
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
}
