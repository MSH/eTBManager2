package org.msh.tb.ua;

import org.jboss.seam.annotations.Name;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.tb.indicators.core.IndicatorFilters;
import org.msh.tb.indicators.core.IndicatorTable;

@Name("reportTB10")
public class ReportTB10 extends Indicator2D {
	private static final long serialVersionUID = -8510290531301762778L;

	private IndicatorTable table2000;

	@Override
	protected void createIndicators() {
		initTable1000();
		initTable2000();
		
		IndicatorFilters filters = getIndicatorFilters();
		filters.setPatientType(null);
		filters.setMicroscopyResult(null);
		filters.setCultureResult(null);
		
		setConsolidated(true);
		generateTable1000();
		generateTable2000();
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

	/**
	 * Generate values of table 1000 
	 */
	protected void generateTable1000() {
		
	}

	/**
	 * Generate values of table 2000 
	 */
	protected void generateTable2000() {
		
	}
	
	/**
	 * Create all tables of the report and generate indicators
	 */
	protected void createTables() {
		table2000 = new IndicatorTable();
		createTable();
	}
	
	public IndicatorTable getTable1000() {
		return getTable();
	}
	
	public IndicatorTable getTable2000() {
		if (table2000 == null)
			createTables();
		return table2000;
	}
}
