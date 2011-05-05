package org.msh.tb.indicators;

import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.CultureResult;
import org.msh.tb.entities.enums.MicroscopyResult;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.utils.date.DateUtils;


@Name("interimOutcomeIndicator")
public class InterimOutcomeIndicator extends Indicator2D {
	private static final long serialVersionUID = 3070089202436585662L;

	private static final String culture = "(select min(e.dateCollected) from ExamCulture e " +
			"where e.tbcase.id = c.id and e.result = :res1)";
	private static final String microscopy = "(select min(e.dateCollected) from ExamMicroscopy e " +
			"where e.tbcase.id = c.id and e.result = :res2)";

	private int numcases;
	private String negCol;
	private String posCol;
	private String unkCol;
	private String diedCol;
	private String defCol;
	private String transCol;


	
	/* (non-Javadoc)
	 * @see org.msh.tb.indicators.Indicator#createIndicators()
	 */
	@Override
	protected void createIndicators() {
		setNewCasesOnly(true);
		
		negCol = getMessage("manag.ind.interim.negative");
		posCol = getMessage("manag.ind.interim.positive");
		unkCol = getMessage("manag.ind.interim.unknown");
		diedCol = getMessage("CaseState.DIED");
		defCol = getMessage("CaseState.DEFAULTED");
		transCol = getMessage("CaseState.TRANSFERRED_OUT");
		
		getTable().addColumn(negCol, null);
		getTable().addColumn(posCol, null);
		getTable().addColumn(unkCol, null);
		getTable().addColumn(diedCol, null);
		getTable().addColumn(defCol, null);
		getTable().addColumn(transCol, null);

		// calculate the number of cases
		numcases = calcNumberOfCases("c.state >= " + CaseState.ONTREATMENT.ordinal());
		
		if (numcases == 0)
			return;
		
		setGroupFields(null);
		setCondition("c.state >= " + CaseState.ONTREATMENT.ordinal());
		
		List<Object[]> lst = createQuery()
			.setParameter("res1", CultureResult.NEGATIVE)
			.setParameter("res2", MicroscopyResult.NEGATIVE)
			.getResultList();
		
		for (Object[] vals: lst) {
			addValue((Date)vals[0], (CaseState)vals[1], (Date)vals[2], (Date)vals[3]);
		}
	}


	
	/**
	 * Add a value to a specific cell in the table based on the dates of the case
	 * @param dtIniTreat
	 * @param dtCult
	 * @param dtMicro
	 */
	protected void addValue(Date dtIniTreat, CaseState state, Date dtCult, Date dtMicro) {		
		String row = Integer.toString(numcases);

		if (state == CaseState.DIED) {
			addValue(diedCol, row, 1F);
			return;
		}
		
		if (state == CaseState.DEFAULTED) {
			addValue(defCol, row, 1F);
		}
		
		// is unknown ?
		if ((dtCult == null) && (dtMicro == null)) {
			addValue(unkCol, row, 1F);
		}
		else {
			boolean negative = false;
			
			if ((dtCult != null) && (dtMicro != null)) {
				int m1 = DateUtils.monthsBetween(dtIniTreat, dtCult);
				int m2 = DateUtils.monthsBetween(dtIniTreat, dtMicro);
				negative = (m1 <= 6) && (m2 <= 6);
			}
			
			if (negative)
				 addValue(negCol, row, 1F);
			else addValue(posCol, row, 1F);
		}
	}


	
	/* (non-Javadoc)
	 * @see org.msh.tb.indicators.CaseHQLBase#getHQLSelect()
	 */
	@Override
	public String getHQLSelect() {
		if (numcases == 0)
			return super.getHQLSelect();
		
		return "select c.treatmentPeriod.iniDate, c.state, " + culture + ", " + microscopy;
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.indicators.CaseHQLBase#getClassification()
	 */
	@Override
	public CaseClassification getClassification() {
		return CaseClassification.DRTB;
	}

}
