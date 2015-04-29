package org.msh.tb.entities;

import org.msh.etbm.commons.transactionlog.mapping.PropertyLog;
import org.msh.tb.entities.enums.DstResult;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="examdst")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="DISCRIMINATOR", discriminatorType=DiscriminatorType.STRING)
@DiscriminatorValue("gen")
public class ExamDST extends LaboratoryExam implements Serializable {
	private static final long serialVersionUID = -1911463378908689952L;

	@OneToMany(cascade={CascadeType.ALL}, mappedBy="exam")
	private List<ExamDSTResult> results = new ArrayList<ExamDSTResult>();

	@PropertyLog(ignore=true)
	private int numResistant;

	@PropertyLog(ignore=true)
	private int numSusceptible;

	@PropertyLog(ignore=true)
	private int numContaminated;

    @Override
    public ExamResult getExamResult() {
        if ((results == null) || (results.size() == 0)) {
            return ExamResult.UNDEFINED;
        }

        for (ExamDSTResult res: results) {
            if (res.getResult() == DstResult.RESISTANT) {
                return ExamResult.POSITIVE;
            }
        }

        return ExamResult.NEGATIVE;
    }

    /**
	 * Search for a result by the substance 
	 * @param sub - Substance to be used to search result
	 * @return - Susceptibility result
	 */
	public ExamDSTResult findResultBySubstance(Substance sub) {
		for (ExamDSTResult res: results) {
			if (res.getSubstance().equals(sub)) {
				return res;
			}
		}
		return null;
	}

	public List<ExamDSTResult> getResults() {
		return results;
	}

	public void setResults(List<ExamDSTResult> results) {
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
}
