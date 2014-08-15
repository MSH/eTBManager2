package org.msh.tb.ng;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.ResistancePattern;
import org.msh.tb.entities.Substance;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.DstResult;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.tb.indicators.core.IndicatorTable;
import org.msh.tb.indicators.core.IndicatorTable.TableColumn;
import org.msh.tb.resistpattern.ResistancePatternsQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates indicator about DST resistance patterns for NG (Mono-Resistance
 * only)
 * 
 * @author Rao
 * 
 */
@Name("resistancePatternIndicatorng")
public class ResistancePatternIndicatorNG extends Indicator2D {
	private static final long serialVersionUID = 3368662304401083530L;

	@In(create = true)
	ResistancePatternsQuery resistancePatterns;

	private int total;
	private int flag = 0;
	private int countNewPatients = 0;

	private String newp;
	private String newPer;
	private String oldp;
	private String oldPer;
	private String tot;

	@Override
	protected void createIndicators() {
		setGroupFields(null);

		String[] message = new String[5];
		newp = getMessage("manag.ind.dstprofile.nevertreated");
		newPer = getMessage("manag.ind.dstprofile.nevertreatedpercent");
		oldp = getMessage("manag.ind.dstprofile.prevtreated");
		oldPer = getMessage("manag.ind.dstprofile.prevtreatedpercent");
		tot    = getMessage("global.total");

		message[0] = newp;
		message[1] = newPer;
		message[2] = oldp;
		message[3] = oldPer;
		message[4] = tot;

		calcTotal();
		if (total == 0)
			return;

		// Mounts resistance pattern of a set of medicines

		for (ResistancePattern pattern : resistancePatterns.getResultList()) {

			if (pattern.getSubstances().size() == 0)
				return;
			String s = "";

			for (Substance sub : pattern.getSubstances()) {
				if (!s.isEmpty()) {
					s = s + ",";
				}
				s = s + sub.getId().toString();
			}

			String strPattern = pattern.getName().toString();

//			String cond = "(select count(*) from ExamDSTResult res "
//					+ "join res.exam exam "
//					+ "where exam.tbcase.id = c.id and res.substance.id in ("
//					+ s
//					+ ") and res.result = "
//					+ DstResult.RESISTANT.ordinal()
//					+ " and exam.numResistant = "
//					+ pattern.getSubstances().size()
//					+ " and exam.dateCollected = (select min(aux.dateCollected) from ExamDST aux "
//					+ "where aux.tbcase.id = c.id)) = "
//					+ pattern.getSubstances().size();
			
			// Removing the criteria for exam.numResistant
			String cond = "(select count(*) from ExamDSTResult res "
				+ "join res.exam exam "
				+ "where exam.tbcase.id = c.id and res.substance.id in ("
				+ s
				+ ") and res.result = "
				+ DstResult.RESISTANT.ordinal()
				+ " and exam.sample.dateCollected = (select min(aux.sample.dateCollected) from ExamDST aux "
				+ "where aux.tbcase.id = c.id)) = "
				+ pattern.getSubstances().size();

			IndicatorTable table = getTable();
			table.addColumn(newp, null);
			TableColumn newPercent = table.addColumn(newPer, null);
			newPercent.setHighlight(true);

			table.addColumn(oldp, null);
			TableColumn oldPercent = table.addColumn(oldPer, null);
			oldPercent.setHighlight(true);

			flag = 2;
			countNewPatients = 0;

			setCondition(cond);
			List<TbCase> tbCaseList = new ArrayList<TbCase>();
			tbCaseList = createQuery().getResultList();

			if (tbCaseList.size() != 0) {
				for (int k = 0; k < tbCaseList.size(); k++) {
					if (tbCaseList.get(k).getPatientType().getKey()
							.equalsIgnoreCase("PatientType.NEW")) {
						countNewPatients = ++countNewPatients;
					}
				}
			}
			Float newPer = null;
			Float oldPer = null;
			int oldPat = tbCaseList.size() - countNewPatients;

			if (tbCaseList.size() != 0) {				
				newPer = ((float) countNewPatients / (float) total) * 100;
				oldPer = ((float) oldPat / (float) total) * 100;
			} else {
				newPer = new Float(0);
				oldPer = new Float(0);
			}

			int totalPatients = countNewPatients + oldPat;
			
			String strAnyRes = getMessage("manag.ind.dstprofile.anyresistto");
			addValue(message[0], strAnyRes + " "+ strPattern, new Float(
					countNewPatients));
			addValue(message[1], strAnyRes + " "+ strPattern, new Float(newPer));
			addValue(message[2], strAnyRes + " "+ strPattern, new Float(oldPat));
			addValue(message[3], strAnyRes + " "+ strPattern, new Float(oldPer));
			addValue(message[4], strAnyRes + " "+ strPattern, new Float(totalPatients));

		}

		// addValue( getMessages().get("manag.ind.resist.other"), total); //this
		// is 1D
	}

	@Override
	public CaseClassification getClassification() {
		return CaseClassification.DRTB;
	}

	/**
	 * Calculates total quantity of cases with DST exam
	 */
	protected void calcTotal() {
		flag = 1;
		String cond = "exists(select exam.id from ExamDST exam where exam.tbcase.id = c.id)";
		setCondition(cond);
		total = ((Long) createQuery().getSingleResult()).intValue();
	}

	@Override
	protected String getHQLSelect() {
		// TODO Auto-generated method stub
		String sqlSel = "";
		if (flag == 1) {
			flag = 0;
			sqlSel = super.getHQLSelect();
		}
		if (flag == 2) {
			flag = 0;
			// sqlSel = "select c.patientType";
			sqlSel = "";
			// setGroupFields("c.patientType");
		}
		return sqlSel;
	}
}
