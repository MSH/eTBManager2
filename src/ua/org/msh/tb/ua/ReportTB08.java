package org.msh.tb.ua;

import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.enums.CaseState;
import org.msh.mdrtb.entities.enums.ExtraOutcomeInfo;
import org.msh.mdrtb.entities.enums.InfectionSite;
import org.msh.mdrtb.entities.enums.MicroscopyResult;
import org.msh.mdrtb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.tb.indicators.core.IndicatorCultureResult;
import org.msh.tb.indicators.core.IndicatorFilters;
import org.msh.tb.indicators.core.IndicatorMicroscopyResult;
import org.msh.tb.indicators.core.IndicatorTable;

@Name("reportTB08")
public class ReportTB08 extends Indicator2D {
	private static final long serialVersionUID = -1617171254497253851L;

	private IndicatorTable table2000;

	private static final String statesIn = "(" + CaseState.CURED.ordinal() + "," + 
		CaseState.TREATMENT_COMPLETED.ordinal() + "," +
		CaseState.DIED.ordinal() + "," +
		CaseState.FAILED.ordinal() + "," +
		CaseState.DEFAULTED.ordinal() + "," +
		CaseState.TRANSFERRED_OUT.ordinal() + "," +
		CaseState.DIAGNOSTIC_CHANGED.ordinal() + ")";


	/* (non-Javadoc)
	 * @see org.msh.tb.indicators.core.Indicator#createIndicators()
	 */
	@Override
	protected void createIndicators() {
		initTable(getTable1000());
		initTable(getTable2000());
		
		generateTable1000();
		generateTable2000();
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
		table.addColumn(getMessage("CaseState.DEFAULTED"), CaseState.DEFAULTED);
		table.addColumn(getMessage("CaseState.TRANSFERRED_OUT"), CaseState.TRANSFERRED_OUT);
		table.addColumn(getMessage("CaseState.DIAGNOSTIC_CHANGED"), CaseState.DIAGNOSTIC_CHANGED);
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
	 * Generate the report values
	 */
	protected void generateTable1000() {
		IndicatorFilters filters = getIndicatorFilters();
		IndicatorTable table = getTable1000();

		filters.setMicroscopyResult(IndicatorMicroscopyResult.POSITIVE);
		filters.setInfectionSite(InfectionSite.PULMONARY);
		List<Object[]> lst = generateValuesByField("c.patientType, c.state, ua.extraOutcomeInfo", 
				"c.patientType != null and c.state in " + statesIn);
		addTableValues(table, lst, "_positive");

		filters.setMicroscopyResult(IndicatorMicroscopyResult.NEGATIVE);
		filters.setInfectionSite(InfectionSite.PULMONARY);
		lst = generateValuesByField("c.patientType, c.state, ua.extraOutcomeInfo", 
				"c.patientType != null and c.state in " + statesIn);
		addTableValues(table, lst, "_negative");
	}


	/**
	 * Generate values of table 2000 for the report
	 */
	protected void generateTable2000() {
		IndicatorFilters filters = getIndicatorFilters();
		IndicatorTable table = getTable2000();

		filters.setInfectionSite(InfectionSite.PULMONARY);
		List<Object[]> lst = generateValuesByField("c.patientType, c.state, ua.extraOutcomeInfo", 
				"c.patientType != null and c.state in " + statesIn + " and (" +
				getHQLMicroscopyOrCultureCondition(IndicatorMicroscopyResult.POSITIVE, IndicatorCultureResult.POSITIVE));
		addTableValues(table, lst, "_positive");
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
				else col = null;
			}
			else if (state == CaseState.FAILED) {
				if ((extraOutcome == ExtraOutcomeInfo.CLINICAL_EXAM) ||
					(extraOutcome == ExtraOutcomeInfo.CULTURE) ||
					(extraOutcome == ExtraOutcomeInfo.TRANSFER_CATIV))
					 col = extraOutcome;
				else col = null;
			}

			Float val = ((Long)vals[3]).floatValue();

			if (col != null) {
				addIdValue(col, key + micResult, val);
				addIdValue("TOTAL", key + micResult, val);
				System.out.println(col + " = " + val);
			}
		}
	}


	/**
	 * Create all tables of the report and generate indicators
	 */
	@Override
	protected void createTable() {
		table2000 = new IndicatorTable();
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


	/* (non-Javadoc)
	 * @see org.msh.tb.indicators.core.CaseHQLBase#getHQLJoin()
	 */
	@Override
	protected String getHQLFrom() {
		return "from CaseDataUA ua join ua.tbcase c";
	}
}
