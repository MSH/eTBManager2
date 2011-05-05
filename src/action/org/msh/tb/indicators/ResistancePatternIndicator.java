package org.msh.tb.indicators;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.ResistancePattern;
import org.msh.tb.entities.Substance;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.DstResult;
import org.msh.tb.indicators.core.Indicator;

/**
 * Generates indicator about resistance patterns
 * @author Ricardo Memoria
 *
 */
@Name("resistancePatternIndicator")
public class ResistancePatternIndicator extends Indicator {
	private static final long serialVersionUID = 3368662304401083530L;

	@In(create=true) ResistancePatternsQuery resistancePatterns;

	private int total;
	
	@Override
	protected void createIndicators() {
		setGroupFields(null);

		calcTotal();
		if (total == 0)
			return;
		
		for (ResistancePattern pattern: resistancePatterns.getResultList()) {
			addResistancePattern(pattern);
		}
		
		addValue( getMessages().get("manag.ind.resist.other"), total);
	}


	@Override
	public CaseClassification getClassification() {
		return CaseClassification.DRTB;
	}
	
	/**
	 * Mounts resistance pattern of a set of medicines
	 * @param substances
	 */
	protected void addResistancePattern(ResistancePattern pattern) {
		if (pattern.getSubstances().size() == 0)
			return;
		String s = "";
		for (Substance sub: pattern.getSubstances()) {
			if (!s.isEmpty()) {
				s = s + ",";
			}
			s = s + sub.getId().toString();
		}
		
		// rule: check if the first DST exam of the case and check if it's according to the resistance pattern 
		String cond = "(select count(*) from ExamDSTResult res " +
				"join res.exam exam " +
				"where exam.tbcase.id = c.id and res.substance.id in (" + s +
				") and res.result = " + DstResult.RESISTANT.ordinal() + 
				" and exam.numResistant = " + pattern.getSubstances().size() + 
				" and exam.dateCollected = (select min(aux.dateCollected) from ExamDST aux " +
				"where aux.tbcase.id = c.id)) = " + pattern.getSubstances().size();
		
		setCondition(cond);
		Long val = (Long)createQuery().getSingleResult();
		addValue(pattern.getName(), val.intValue());
		
		total -= val;
	}


	/**
	 * Calculates total quantity of cases with DST exam
	 */
	protected void calcTotal() {
		String cond = "exists(select exam.id from ExamDST exam where exam.tbcase.id = c.id)";
		setCondition(cond);
		total = ((Long) createQuery().getSingleResult()).intValue();
	}
}
