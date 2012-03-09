package org.msh.tb.ua;

import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.ExtraOutcomeInfo;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.tb.indicators.core.IndicatorCultureResult;
import org.msh.tb.indicators.core.IndicatorFilters;
import org.msh.tb.indicators.core.IndicatorMicroscopyResult;
import org.msh.tb.indicators.core.IndicatorTable;
import org.msh.tb.indicators.core.IndicatorTable.TableRow;

@Name("reportTB08")
public class ReportTB08 extends Indicator2D {
	private static final long serialVersionUID = -1617171254497253851L;

	private IndicatorTable table2000;
	private IndicatorTable table3000;

	private static final String statesIn = "(" + 
		CaseState.CURED.ordinal() + "," + 
		CaseState.TREATMENT_COMPLETED.ordinal() + "," +
		CaseState.DIED.ordinal() + "," +
		CaseState.FAILED.ordinal() + "," +
		CaseState.TREATMENT_INTERRUPTION.ordinal() + "," +
		CaseState.NOT_CONFIRMED.ordinal() + "," +
		CaseState.TRANSFERRED_OUT.ordinal() + ")";


	/* (non-Javadoc)
	 * @see org.msh.tb.indicators.core.Indicator#createIndicators()
	 */
	@Override
	protected void createIndicators() {
		initTable(getTable1000());
		initTable(getTable2000());
		initTable3000();
		
		generateTable1000();
		generateTable2000();
		generateTable3000();
	}

	
	/**
	 * Initialize table layout. Because both table 1000 and table 2000 share the same layout, they use a common
	 * method to initialize its layout
	 * @param table
	 */
	protected void initTable(IndicatorTable table) {
		table.addColumn(getMessage("CaseState.CURED"), CaseState.CURED);
		table.addColumn(getMessage("CaseState.TREATMENT_COMPLETED"), CaseState.TREATMENT_COMPLETED);
		table.addColumn(getMessage("uk_UA.ExtraOutcomeInfo.TB"), ExtraOutcomeInfo.TB);
		table.addColumn(getMessage("uk_UA.ExtraOutcomeInfo.OTHER_CAUSES"), ExtraOutcomeInfo.OTHER_CAUSES);
		table.addColumn(getMessage("uk_UA.ExtraOutcomeInfo.CULTURE_SMEAR"), ExtraOutcomeInfo.CULTURE_SMEAR);
		table.addColumn(getMessage("uk_UA.ExtraOutcomeInfo.CLINICAL_EXAM"), ExtraOutcomeInfo.CLINICAL_EXAM);
		table.addColumn(getMessage("uk_UA.ExtraOutcomeInfo.TRANSFER_CATIV"), ExtraOutcomeInfo.TRANSFER_CATIV);
		table.addColumn(getMessage("uk_UA.ExtraOutcomeInfo.OTHER_CAUSES"), CaseState.FAILED);
		table.addColumn(getMessage("CaseState.TREATMENT_INTERRUPTION"), CaseState.TREATMENT_INTERRUPTION);
		table.addColumn(getMessage("CaseState.NOT_CONFIRMED"), CaseState.NOT_CONFIRMED);
		table.addColumn(getMessage("CaseState.TRANSFERRED_OUT"), CaseState.TRANSFERRED_OUT);
		table.addColumn(getMessage("global.total"), "TOTAL");
		
		String txtPositive = getMessage("global.positive");
		String txtNegative = getMessage("global.negative");
		
		table.addRow(txtPositive, "new_positive");
		table.addRow(txtNegative, "new_negative");
		
		table.addRow(txtPositive, "relapse_positive");
		table.addRow(txtNegative, "relapse_negative");
		
		table.addRow(txtPositive, "other_positive");
		table.addRow(txtNegative, "other_negative");
	}

	
	/**
	 * Initialize structure of table 3000
	 */
	protected void initTable3000() {
		table3000.addColumn(getMessage("uk_UA.reports.tb08.3.header2"), "col1");
		table3000.addColumn(getMessage("uk_UA.reports.tb08.3.header3"), "col2");
		table3000.addColumn(getMessage("uk_UA.reports.tb08.3.header4"), "col3");
		table3000.addRow(getMessage("PatientType.NEW"), "new");
		table3000.addRow(getMessage("PatientType.RELAPSE"), "relapse");
		table3000.addRow(getMessage("PatientType.OTHER"), "other");
	}

	/**
	 * Generate the report values
	 */
	protected void generateTable1000() {
		IndicatorFilters filters = getIndicatorFilters();
		IndicatorTable table = getTable1000();

		filters.setMicroscopyResult(IndicatorMicroscopyResult.POSITIVE);
		filters.setInfectionSite(InfectionSite.PULMONARY);
		List<Object[]> lst = generateValuesByField("c.patientType, c.state, ua.extraOutcomeInfo", 
				"c.patientType in (0,1,2,3,4,5) and c.state in " + statesIn);
		addTableValues(table, lst, "_positive");

		filters.setMicroscopyResult(IndicatorMicroscopyResult.POSITIVE);
		String cond = getHQLMicroscopyCondition();
		filters.setMicroscopyResult(null);
		filters.setInfectionSite(InfectionSite.PULMONARY);
		lst = generateValuesByField("c.patientType, c.state, ua.extraOutcomeInfo", 
				"c.patientType in (0,1,2,3,4,5) and c.state in " + statesIn +
				" and not " + cond);
		addTableValues(table, lst, "_negative");
	}


	/**
	 * Generate values of table 2000 for the report
	 */
	protected void generateTable2000() {
		IndicatorFilters filters = getIndicatorFilters();
		IndicatorTable table = getTable2000();

		// pulmonary positives
		filters.setInfectionSite(InfectionSite.PULMONARY);
		List<Object[]> lst = generateValuesByField("c.patientType, c.state, ua.extraOutcomeInfo", 
				"c.patientType in (0,1,2,3,4,5) and c.state in " + statesIn + " and (" +
				getHQLMicroscopyOrCultureCondition(IndicatorMicroscopyResult.POSITIVE, IndicatorCultureResult.POSITIVE) +")");
		addTableValues(table, lst, "_positive");

		// pulmonary negatives
		filters.setMicroscopyResult(IndicatorMicroscopyResult.POSITIVE);
		filters.setCultureResult(IndicatorCultureResult.POSITIVE);
		String condition = "c.patientType in (0,1,2,3,4,5) " +
			" and c.state in " + statesIn +
			" and not " + getHQLMicroscopyCondition() +
			" and not " + getHQLCultureCondition();
		filters.setMicroscopyResult(null);
		filters.setCultureResult(null);
		filters.setInfectionSite(InfectionSite.PULMONARY);
		lst = generateValuesByField("c.patientType, c.state, ua.extraOutcomeInfo", condition);
		addTableValues(table, lst, "_negative");
	}


	/**
	 * Generate data of table 3000
	 */
	protected void generateTable3000() {
		IndicatorFilters filters = getIndicatorFilters();
		IndicatorTable table = getTable3000();

		filters.setInfectionSite(InfectionSite.PULMONARY);
		filters.setMicroscopyResult(null);
		filters.setCultureResult(null);
	
		// calculate number of cases with pulmonary destruction at notification
		List<Object[]> lst = generateValuesByField("c.patientType", 
				"c.patientType in (0,1,2,3,4,5) and ua.pulmonaryDestruction='YES'");
		addValues(table, "col1", lst);

		// calculate number of cases with pulmonary destruction at notification and where the last exam has no destruction
		lst = generateValuesByField("c.patientType", 
			"c.patientType in (0,1,2,3,4,5) and ua.pulmonaryDestruction='YES' " +
			"and exists(from ExamXRay exam where exam.tbcase.id=c.id and exam.destruction=true" +
			" and exam.date = (select max(aux.date) from ExamXRay aux where aux.tbcase.id=c.id))");
		addValues(table, "col2", lst);

		// calculate percentage
		table.getColumns().get(2).setNumberPattern("#,###,##0.00");
		for (TableRow row: table.getRows()) {
			Float val1 = table.getValue("col1", row.getId().toString());
			Float val2 = table.getValue("col2", row.getId().toString());
			float perc = 0;
			
			if ((val1 != null) && (val2 != null))
				perc = val2 / val1 * 100;
			else {
				if (val1 != null)
					perc = 100;
			}
			table.addIdValue("col3", row.getId(), perc);
		}
	}


	protected void addValues(IndicatorTable table, String col, List<Object[]> lst) {
		for (Object[] vals: lst) {
			PatientType pt = (PatientType)vals[0];
			String s;
			switch (pt) {
			case NEW: s = "new";
				break;
			case RELAPSE:  s = "relapse";
				break;
			default: s = "other";
			}
			Float val = ((Long)vals[1]).floatValue();
			table.addIdValue(col, s, val);
		}
	}


	/**
	 * Return condition of culture or microscopy results 
	 * @return
	 */
	private String getHQLMicroscopyOrCultureCondition(IndicatorMicroscopyResult microscopyResult, IndicatorCultureResult cultureResult) {
		IndicatorFilters filters = (IndicatorFilters)Component.getInstance("indicatorFilters");
		filters.setMicroscopyResult(microscopyResult);
		String condition = getHQLMicroscopyCondition();

		filters.setCultureResult(cultureResult);
		condition = "(" + condition + " or " + getHQLCultureCondition() + ")";

		filters.setMicroscopyResult(null);
		filters.setCultureResult(null);
		
		return condition;
	}
	
	

	/**
	 * Add values from the result list to the table
	 * @param table
	 * @param lst
	 * @param micResult
	 */
	protected void addTableValues(IndicatorTable table, List<Object[]> lst, String micResult) {
		for (Object[] vals: lst) {
			PatientType patientType = (PatientType)vals[0];
			CaseState state = (CaseState)vals[1];
			ExtraOutcomeInfo extraOutcome = (ExtraOutcomeInfo)vals[2];
			
			String key;
			switch (patientType) {
			case NEW: key = "new"; 
				break;
			case RELAPSE: key = "relapse"; 
				break;
			default: key = "other";
				break;
			}
			
			Object col = state;
			
			if (state == CaseState.DIED) {
				if ((extraOutcome == ExtraOutcomeInfo.TB) || (extraOutcome == ExtraOutcomeInfo.OTHER_CAUSES))
					col = extraOutcome;
				else col = ExtraOutcomeInfo.OTHER_CAUSES; // default value
			}
			else if (state == CaseState.FAILED) {
				if ((extraOutcome == ExtraOutcomeInfo.CLINICAL_EXAM) ||
					(extraOutcome == ExtraOutcomeInfo.CULTURE_SMEAR) ||
					(extraOutcome == ExtraOutcomeInfo.TRANSFER_CATIV))
					 col = extraOutcome;
				else col = state;
			}

			Float val = ((Long)vals[3]).floatValue();

			if (col != null) {
				table.addIdValue(col, key + micResult, val);
				table.addIdValue("TOTAL", key + micResult, val);
			}
		}
	}



	/**
	 * Create all tables of the report and generate indicators
	 */
	@Override
	protected void createTable() {
		table2000 = new IndicatorTable();
		table3000 = new IndicatorTable();
		super.createTable();
	}



	/**
	 * Return table 1000 report, from the original table object from {@link IndicatorTable}
	 * @return
	 */
	public IndicatorTable getTable1000() {
		return getTable();
	}

	
	/**
	 * Return table2000 report
	 * @return
	 */
	public IndicatorTable getTable2000() {
		if (table2000 == null)
			createTable();
		return table2000;
	}


	/**
	 * Return table3000 report
	 * @return
	 */
	public IndicatorTable getTable3000() {
		if (table3000 == null)
			createTable();
		return table3000;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.indicators.core.CaseHQLBase#getHQLJoin()
	 */
	@Override
	protected String getHQLFrom() {
		return "from CaseDataUA ua join ua.tbcase c";
	}
}
