package org.msh.tb.ua;

import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.tb.indicators.core.IndicatorFilters;
import org.msh.tb.indicators.core.IndicatorMicroscopyResult;
import org.msh.tb.indicators.core.IndicatorTable;
import org.msh.utils.date.DateUtils;

@Name("reportTB10")
public class ReportTB10 extends Indicator2D {
	private static final long serialVersionUID = -8510290531301762778L;

	private IndicatorTable table2000;
	private IndicatorTable table1000;

	/**
	 * Total number of cases in the indicator 
	 */
	private int numcases;
	

	@Override
	protected void createIndicators() {
		table1000 = getTable();
		
		initTable1000();
		initTable2000();
		
		IndicatorFilters filters = getIndicatorFilters();
		filters.setPatientType(null);
		filters.setMicroscopyResult(null);
		//filters.setCultureResult(null);
		
		setConsolidated(true);
		generateTable1000();
		//generateTable2000();
	}


	/**
	 * Initialize structure of table 1000 
	 */
	protected void initTable1000() {
		IndicatorTable table = getTable1000();
		
		String total = getMessage("uk_UA.reports.tb10.1.total");

		table.addColumn(getMessage("uk_UA.reports.tb10.1.header2"), "numcases");
		table.addColumn(total, "month2");
		table.addColumn("%", "month2_perc");

		table.addColumn(total, "month3");
		table.addColumn("%", "month3_perc");

		table.addColumn(total, "month4");
		table.addColumn("%", "month4_perc");
		
		table.addColumn(getMessage("uk_UA.reports.tb10.1.header7"), "noexams");
		table.addColumn(getMessage("uk_UA.reports.tb10.1.header8"), "others");
		
		table.addRow(getMessage("uk_UA.reports.newcases"), "newcases");
		table.addRow(getMessage("uk_UA.reports.relapses"), "relapses");
		table.addRow(getMessage("uk_UA.reports.other"), "others");
	}


	/**
	 * Initialize structure of table 2000 
	 */
	protected void initTable2000() {
		IndicatorTable table = getTable2000();

		// titles are not used in the displaying of the table
		table.addColumn("1", "CaseState.DIED.TB");
		table.addColumn("2", "CaseState.DIED.OTHER_CAUSES");
		table.addColumn("3", "CaseState.TREATMENT_INTERRUPTION");
		table.addColumn("4", "CaseState.TRANSFERRED_OUT");
		table.addColumn("5", "CaseState.DIAGNOSTIC_CHANGED");
		table.addColumn("6", "CaseState.OTHER");
		table.addColumn("7", "NOT_DONE");
		
		table.addRow(getMessage("uk_UA.reports.newcases"), "newcases");
		table.addRow(getMessage("uk_UA.reports.relapses"), "relapses");
		table.addRow(getMessage("uk_UA.reports.other"), "others");
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.indicators.core.CaseHQLBase#getHQLInfectionSite()
	 */
	@Override
	protected String getHQLInfectionSite() {
		IndicatorFilters filters = getIndicatorFilters();
		if  (filters.getInfectionSite() == InfectionSite.PULMONARY)
			return "c.infectionSite in (" + InfectionSite.PULMONARY.ordinal() + "," + InfectionSite.BOTH.ordinal() + ")";
		else return super.getHQLInfectionSite();
	}
	
	/**
	 * Generate values of table 1000 querying the database based on the user filters 
	 */
	protected void generateTable1000() {
		// calculate the number of cases
		numcases = calcNumberOfCases("c.state >= " + CaseState.ONTREATMENT.ordinal());
		
		if (numcases == 0)
			return;

		String hqlMicroscopy = "(select min(e.dateCollected) from ExamMicroscopy e " +
				"where e.tbcase.id = c.id and e.result " + getHQLMicroscopyResultCondition(IndicatorMicroscopyResult.NEGATIVE)+")";
		// check if it has microscopy
		String hasMicr = "(select min(e.dateCollected) from ExamMicroscopy e where e.tbcase.id = c.id)";
		
		setGroupFields(null);
		setConsolidated(false);
		getIndicatorFilters().setInfectionSite(InfectionSite.PULMONARY);
		setHQLSelect("select c.treatmentPeriod.iniDate, c.diagnosisDate, c.patientType, c.state, " + 
				hqlMicroscopy +	", " + 
				hasMicr);
		setCondition("c.state >= " + CaseState.ONTREATMENT.ordinal()+" and c.patientType in (0,1,2,3,4,5) and exists(select e.id from ExamMicroscopy e where e.result in (1,2,3,4) and e.tbcase.id = c.id and e.dateCollected = (select min(sp.dateCollected) from ExamMicroscopy sp where sp.tbcase.id = c.id))");
		
		List<Object[]> lst = createQuery()
			.getResultList();

		for (Object[] vals: lst) {
			boolean noexam = vals[5] == null;
			addValueTable((Date)vals[0], (Date)vals[1], (PatientType)vals[2], (CaseState)vals[3], (Date)vals[4], noexam);
		}
		
		String rows[] = {"newcases", "relapses", "others"};
		String cols[] = {"month2", "month3", "month4"};
		
		for (String rowid: rows)
			for (String colid: cols) {
				calcPercentage(colid, rowid);
			}
	}

	/**
	 * Generate values of table 2000 querying the database based on the user filters 
	 */
	protected void generateTable2000() {
		
		// check if it has microscopy
		String hasMicr = "(select min(e.dateCollected) from ExamMicroscopy e where e.tbcase.id = c.id and e.result in (1,2,3,4))";
		String hasMicr2 = "(select max(e.dateCollected) from ExamMicroscopy e where e.tbcase.id = c.id), ";
		
		setConsolidated(true);
		getIndicatorFilters().setInfectionSite(InfectionSite.PULMONARY);
		
		List<Object[]> lst = generateValuesByField("c.patientType, c.state, "+hasMicr2, "c.patientType in (0,1,2,3,4,5) and exists"+hasMicr);
		

		for (Object[] vals: lst) {
			addValueTable((PatientType)vals[0], (CaseState)vals[1], (Long) vals[3]);
		}
		
		String rows[] = {"newcases", "relapses", "others"};
		String cols[] = {"month2", "month3", "month4"};
		
		for (String rowid: rows)
			for (String colid: cols) {
				calcPercentage(colid, rowid);
			}
	}

	
	private void calcPercentage(String colid, String rowid) {
		float total = table1000.getCellAsFloat("numcases", rowid);
		if (total == 0)
			return;
		
		float num = table1000.getCellAsFloat(colid, rowid);
		
		table1000.setValue(colid + "_perc", rowid, num * 100F / total);
	}

	
	/**
	 * Add a value to a specific cell in the table based on the dates of the case
	 * @param dtIniTreat
	 * @param dtDiagnos
	 * @param dtMicro
	 */
	protected void addValueTable(Date dtIniTreat, Date dtDiagnos, PatientType ptype, CaseState state, Date dtMicro, boolean noexam) {
		if (dtIniTreat == null)
			dtIniTreat = dtDiagnos;

		// select row id
		String rowkey;
		switch (ptype) {
		case NEW: 
			rowkey = "newcases";
			break;
		case RELAPSE: 
			rowkey = "relapses";
			break;
		default: 
			rowkey = "others";
		}

		// no culture or microscopy to the case ?
		if (noexam)
			table1000.addIdValue("noexams", rowkey, 1F);
		else {
			// is negative ?
			if (dtMicro != null) {
				// calculate month of negativation
				int negativeMonth = DateUtils.monthsBetween(dtIniTreat, dtMicro);
				
				if (negativeMonth <= 2)
					table1000.addIdValue("month2", rowkey, 1F);
				
				if (negativeMonth <= 3)
					table1000.addIdValue("month3", rowkey, 1F);
				
				if (negativeMonth <= 4)
					table1000.addIdValue("month4", rowkey, 1F);
				
				if (negativeMonth > 4)
					table1000.addIdValue("others", rowkey, 1F);
			}
			else table1000.addIdValue("others", rowkey, 1F);
		}
		
		table1000.addIdValue("numcases", rowkey, 1F);
	}

	/**
	 * Add a value to a specific cell in the table based on the dates of the case
	 * @param dtIniTreat
	 * @param dtDiagnos
	 * @param dtMicro
	 */
	protected void addValueTable(PatientType ptype, CaseState state, Long val) {
		// select row id
		String rowkey;
		String colkey;
		switch (ptype) {
		case NEW: 
			rowkey = "newcases";
			break;
		case RELAPSE: 
			rowkey = "relapses";
			break;
		default: 
			rowkey = "others";
		}
		
		switch (state) {
		case DIED:
			colkey = "1";
			break;
		case DIED_NOTTB:
			colkey = "2";
			break;
		case TREATMENT_INTERRUPTION:
			colkey = "3";
			break;
		case TRANSFERRED_OUT: case TRANSFERRING:
			colkey = "4";
			break;
		case DIAGNOSTIC_CHANGED_TO_NOT_DRTB: case DIAGNOSTIC_CHANGED:
			colkey = "5";
			break;
		case OTHER:
			colkey = "6";
			break;
		default:
			colkey = "7";
			break;
		}
		table2000.addIdValue(colkey, rowkey, val.floatValue());
		
	}
	
	
	/* (non-Javadoc)
	 * @see org.msh.tb.indicators.core.CaseHQLBase#getHQLJoin()
	 */
	protected String getHQLJoin() {
		return null;
	}


		/**
	 * Create all tables of the report and generate indicators
	 */
	@Override
	protected void createTable() {
		table2000 = new IndicatorTable();
		super.createTable();
	}
	
	public IndicatorTable getTable1000() {
		return getTable();
	}
	
	public IndicatorTable getTable2000() {
		if (table2000 == null)
			createTable();
		return table2000;
	}
}
