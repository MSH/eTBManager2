package org.msh.tb.ua;

import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Regimen;
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
	private float [] numcases = new float [3];
	

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
		table.addColumn("CaseState.DIED.TB", "1");
		table.addColumn("CaseState.DIED.OTHER_CAUSES", "2");
		table.addColumn("CaseState.TREATMENT_INTERRUPTION", "3");
		table.addColumn("CaseState.TRANSFERRED_OUT", "4");
		table.addColumn("CaseState.DIAGNOSTIC_CHANGED", "5");
		table.addColumn("CaseState.OTHER", "6");
		table.addColumn("NOT_DONE", "7");
		
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
		getIndicatorFilters().setInfectionSite(InfectionSite.PULMONARY);
		
		numcases[0] = calcNumberOfCases("c.state >= " + CaseState.ONTREATMENT.ordinal()+" and c.patientType = " + PatientType.NEW.ordinal());
		numcases[1] = calcNumberOfCases("c.state >= " + CaseState.ONTREATMENT.ordinal()+" and c.patientType = " + PatientType.RELAPSE.ordinal());
		numcases[2] = calcNumberOfCases("c.state >= " + CaseState.ONTREATMENT.ordinal());
		numcases[2] -= numcases[0]+numcases[1];
		if (numcases[0]+numcases[1]+numcases[2] == 0)
			return;

		String hqlMicroscopy = "(select min(e.dateCollected) from ExamMicroscopy e " +
				"where e.tbcase.id = c.id and e.result " + getHQLMicroscopyResultCondition(IndicatorMicroscopyResult.NEGATIVE)+")";
		// check if it has microscopy
		String hasMicr = "(select max(e.dateCollected) from ExamMicroscopy e where e.tbcase.id = c.id and (e.dateCollected <= c.iniContinuousPhase or c.iniContinuousPhase = null))";
		
		setGroupFields(null);
		setConsolidated(false);
		setHQLSelect("select c.treatmentPeriod.iniDate, c.diagnosisDate, c.patientType, c.state, c.regimen, " + 
				hqlMicroscopy +	", " + 
				hasMicr);
		setCondition("c.state >= " + CaseState.ONTREATMENT.ordinal()+" and c.patientType in (0,1,2,3,4,5) and exists(select e.id from ExamMicroscopy e where e.result in (1,2,3,4) and e.tbcase.id = c.id and e.dateCollected = (select min(sp.dateCollected) from ExamMicroscopy sp where sp.tbcase.id = c.id))");
		
		List<Object[]> lst = createQuery()
			.getResultList();

		for (Object[] vals: lst) {
			boolean noexam = vals[6] == null;
			addValueTable((Date)vals[0], (Date)vals[1], (PatientType)vals[2], (CaseState)vals[3], (Regimen)vals[4], (Date)vals[5], noexam);
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
		String hasMicr2 = "(select max(e.dateCollected) from ExamMicroscopy e where e.tbcase.id = c.id and (e.dateCollected <= c.iniContinuousPhase or c.iniContinuousPhase = null)) ";
		
		setConsolidated(false);
		getIndicatorFilters().setInfectionSite(InfectionSite.PULMONARY);
		setGroupFields(null);
		setHQLSelect("select c.treatmentPeriod.iniDate, c.diagnosisDate, c.patientType, c.state, c.regimen, "+hasMicr2+", c.iniContinuousPhase");
		setCondition("c.state >= " + CaseState.ONTREATMENT.ordinal()+" and c.patientType in (0,1,2,3,4,5) and exists"+hasMicr);//+" and not exists"+hasMicr2);
		List<Object[]> lst = createQuery().getResultList();

		for (Object[] vals: lst) {
			addValueTable((Date)vals[0], (Date)vals[1], (PatientType)vals[2], (CaseState)vals[3], (Regimen) vals[4], (Date)vals[5],(Date)vals[6]);
		}
		
	}

	
	private void calcPercentage(String colid, String rowid) {
		float total = 0;
		if (rowid=="newcases") total = numcases[0];	
		if (rowid=="relapses") total = numcases[1];
		if (rowid=="others") total = numcases[2];
		
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
	protected void addValueTable(Date dtIniTreat, Date dtDiagnos, PatientType ptype, CaseState state, Regimen reg, Date dtMicro, boolean noexam) {
		if (dtIniTreat == null)
			dtIniTreat = dtDiagnos;

		// select row id
		String rowkey;
		String colkey = null;
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
		
				
		if (noexam)
			table1000.addIdValue("noexams", rowkey, 1F);
		else {
			// is negative ?
			if (dtMicro != null){ 
				if (DateUtils.monthsBetween(dtIniTreat, dtMicro) <= reg.getMonthsIntensivePhase())
					{
					// calculate month of negativation
					int negativeMonth = DateUtils.monthsBetween(dtIniTreat, dtMicro);
			
					if (negativeMonth <= 2 && ptype.equals(PatientType.NEW))
						table1000.addIdValue("month2", rowkey, 1F);
					
					if (negativeMonth <= 3)
						table1000.addIdValue("month3", rowkey, 1F);
					
					if (negativeMonth <= 4)
						if (!ptype.equals(PatientType.NEW))
							table1000.addIdValue("month4", rowkey, 1F);
						else
							if (negativeMonth > 3)
							table1000.addIdValue("others", rowkey, 1F);	
					
					if (negativeMonth > 4)
						table1000.addIdValue("others", rowkey, 1F);
					}
				else 
					{
						table1000.addIdValue("noexams", rowkey, 1F);
						switch (state) {
						case DIED:
							colkey = "1";
							break;
						case DIED_NOTTB:
							colkey = "2";
							break;
						case TREATMENT_INTERRUPTION: case DEFAULTED: case FAILED:
							colkey = "3";
							break;
						case TRANSFERRED_OUT: case TRANSFERRING:
							colkey = "4";
							break;
						case DIAGNOSTIC_CHANGED_TO_NOT_DRTB: case DIAGNOSTIC_CHANGED: case CURED: case TREATMENT_COMPLETED:
							colkey = "5";
							break;
						default:
							colkey = "6";
							break;
						}
						if (colkey != null) table2000.addIdValue(colkey, rowkey, 1F);
						table2000.addIdValue("7",rowkey,1F);
					}
			}
			else table1000.addIdValue("others", rowkey, 1F);
			}
		
		table1000.addIdValue("numcases", rowkey, 1F);
	}

	/**
	 * Add a value to a specific cell in the table based on the dates of the case
	 * @param ptype
	 * @param state
	 * @param val
	 */
	protected void addValueTable(Date dtIniTreat, Date dtDiagnos, PatientType ptype, CaseState state, Regimen reg, Date dtMicro, Date dtIniContPhase) {
		// select row id
		if (dtIniTreat == null)
			dtIniTreat = dtDiagnos;
		
		String rowkey;
		String colkey = null;
		
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
		//if ((dtIniContPhase == null) || ((dtIniContPhase != null) && (DateUtils.daysBetween(dtMicro, dtIniContPhase)>14))){
		if (dtMicro != null)
			if (DateUtils.monthsBetween(dtIniTreat, dtMicro) > reg.getMonthsIntensivePhase()){	
			switch (state) {
				case DIED:
					colkey = "1";
					break;
				case DIED_NOTTB:
					colkey = "2";
					break;
				case TREATMENT_INTERRUPTION: case DEFAULTED:
					colkey = "3";
					break;
				case TRANSFERRED_OUT: case TRANSFERRING:
					colkey = "4";
					break;
				case DIAGNOSTIC_CHANGED_TO_NOT_DRTB: case DIAGNOSTIC_CHANGED: case CURED:
					colkey = "5";
					break;
				default:
					colkey = "6";
					break;
		}
		if (colkey != null) table2000.addIdValue(colkey, rowkey, 1F);
		table2000.addIdValue("7",rowkey,1F);
		}
		
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
