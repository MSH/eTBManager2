package org.msh.tb.ua;

import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.AgeRange;
import org.msh.mdrtb.entities.enums.CaseClassification;
import org.msh.mdrtb.entities.enums.Gender;
import org.msh.mdrtb.entities.enums.InfectionSite;
import org.msh.mdrtb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.tb.indicators.core.IndicatorFilters;
import org.msh.tb.indicators.core.IndicatorMicroscopyResult;
import org.msh.tb.indicators.core.IndicatorTable;
import org.msh.tb.indicators.core.OutputSelection;

@Name("reportTB07")
public class ReportTB07 extends Indicator2D {
	private static final long serialVersionUID = -8462692609433997419L;

	private IndicatorTable table2000;
	private IndicatorTable table3000;
	private IndicatorTable table4000;
	private IndicatorTable table5000;
	
	private String txtNewCases, txtRelapses, txtOther, txtAll, txtMale, txtFemale;
	
	@Override
	protected void createIndicators() {
		txtNewCases = getMessage("uk_UA.reports.newcases");
		txtRelapses = getMessage("uk_UA.reports.relapses");
		txtOther = getMessage("uk_UA.reports.other");
		txtAll = getMessage("uk_UA.reports.all");
		txtMale = getMessage("uk_UA.reports.male");
		txtFemale = getMessage("uk_UA.reports.female");

		initTable1000();
		initTable2000();
		initTable3000();
		initTable4000();
		
		setConsolidated(true);
		generateTable1000();
		generateTable2000();
	}


	/**
	 * Generate table 1000 report
	 */
	protected void generateTable1000() {
		IndicatorFilters filters = (IndicatorFilters)Component.getInstance("indicatorFilters");
		filters.setClassification(CaseClassification.TB);
		
		filters.setMicroscopyResult(IndicatorMicroscopyResult.POSITIVE);
		filters.setInfectionSite(InfectionSite.PULMONARY);
		List<Object[]> lst = generateValuesByField("c.patientType, c.infectionSite, c.patient.gender", "c.patientType != null and c.infectionSite != null");
		addTable1000Values(lst, IndicatorMicroscopyResult.POSITIVE);
		
		filters.setMicroscopyResult(IndicatorMicroscopyResult.NEGATIVE);
		lst = generateValuesByField("c.patientType, c.infectionSite, c.patient.gender", "c.patientType != null and c.infectionSite != null");
		addTable1000Values(lst, IndicatorMicroscopyResult.NEGATIVE);
		
		filters.setMicroscopyResult(null);
		filters.setInfectionSite(InfectionSite.EXTRAPULMONARY);
		lst = generateValuesByField("c.patientType, c.infectionSite, c.patient.gender", "c.patientType != null and c.infectionSite != null");
		addTable1000Values(lst, IndicatorMicroscopyResult.NEGATIVE);
	}


	private void addTable1000Values(List<Object[]> lst, IndicatorMicroscopyResult result) {
		for (Object[] vals: lst) {
			PatientType patType = (PatientType)vals[0];
			InfectionSite site = (InfectionSite)vals[1];
			Gender gender = (Gender)vals[2];
			Long val = (Long)vals[3];

			if ((site == InfectionSite.PULMONARY) || (site == InfectionSite.EXTRAPULMONARY)) {
				String key = null;
				if (InfectionSite.PULMONARY.equals(site)) {
					if (result == IndicatorMicroscopyResult.POSITIVE)
						 key = "positive_";
					else key = "negative_";
				}
				else 
				if (InfectionSite.EXTRAPULMONARY.equals(site))
					key = "extrapulmonary_";
				
				addTableValue(getTable1000(), key, patType, val.floatValue());
				
				if (gender == Gender.MALE)
					 getTable1000().addIdValue("male", "row", val.floatValue());
				else getTable1000().addIdValue("female", "row", val.floatValue());
				getTable1000().addIdValue("all", "row", val.floatValue());
			}
		}		
	}


	/**
	 * Add a value to a table
	 * @param table Table to include a value 
	 * @param prefix Prefix of the column name
	 * @param patientType type of patient to compose the column name with the prefix
	 * @param value value to include
	 */
	private void addTableValue(IndicatorTable table, String prefix, PatientType patientType, Float value) {
		String key = prefix;
		if (PatientType.NEW.equals(patientType))
			key = key + "newcases";
		else
		if (PatientType.RELAPSE.equals(patientType))
			key = key + "relapses";
		else key = key + "other";
		
		table.addIdValue(key, "row", value);
		table.addIdValue(prefix + "all", "row", value);
	}
	
	
	private void generateTable2000() {
		IndicatorFilters filters = (IndicatorFilters)Component.getInstance("indicatorFilters");
		filters.setClassification(CaseClassification.TB);
		
		filters.setMicroscopyResult(IndicatorMicroscopyResult.POSITIVE);
		filters.setInfectionSite(InfectionSite.PULMONARY);
		List<Object[]> lst = generateValuesByField("c.age, c.patient.gender", "c.age != null and c.patient.gender != null");
		lst = groupValuesByAreRange(lst, 0, 2);
		
		IndicatorTable table = getTable2000();
		for (Object[] vals: lst) {
			AgeRange r = (AgeRange)vals[0];
			Gender gender = (Gender)vals[1];
			float val = ((Long)vals[2]).floatValue();
			
			String genderKey = gender == Gender.MALE? "male": "female";
			String key = r.getIniAge() + "_" + genderKey;
			
			table.addIdValue(key, "row", val);
			table.addIdValue(genderKey + "_all" , "row", val);
			table.addIdValue("all", "row", val);
		}
	}
	
	
	/**
	 * Initialize layout of the table 1000 of the TB07 report
	 */
	private void initTable1000() {
		IndicatorTable table = getTable1000();
		
		table.addColumn(txtNewCases, "positive_newcases");
		table.addColumn(txtRelapses, "positive_relapses");
		table.addColumn(txtOther, "positive_other");
		table.addColumn(txtAll, "positive_all");
		
		table.addColumn(txtNewCases, "negative_newcases");
		table.addColumn(txtRelapses, "negative_relapses");
		table.addColumn(txtOther, "negative_other");
		table.addColumn(txtAll, "negative_all");
		
		table.addColumn(txtNewCases, "extrapulmonary_newcases");
		table.addColumn(txtRelapses, "extrapulmonary_relapses");
		table.addColumn(txtOther, "extrapulmonary_other");
		table.addColumn(txtAll, "extrapulmonary_all");

		table.addColumn(txtMale, "male");
		table.addColumn(txtFemale, "female");
		table.addColumn(txtAll, "all");
		
		table.addRow("TOTAL", "row");
	}

	
	/**
	 * Initialize the layout of the table 2000 of the TB07 report
	 */
	private void initTable2000() {
		IndicatorTable table = getTable2000();
		
		List<AgeRange> ageRanges = getAgeRangeHome().getItems();

		for (AgeRange r: ageRanges) {
			table.addColumn(txtMale, r.getIniAge() + "_female");
			table.addColumn(txtFemale, r.getIniAge() + "_male");
		}
		
		table.addColumn(txtMale, "male_all");
		table.addColumn(txtFemale, "female_all");
		table.addColumn(txtAll, "all");
		
		table.addRow("TOTAL", "row");
	}


	/**
	 * Initialize layout of the table 3000 of the TB07 report
	 */
	private void initTable3000() {
		IndicatorTable table = getTable3000();
		
		table.addColumn(txtNewCases, "positive_newcases");
		table.addColumn(txtRelapses, "positive_relapses");
		table.addColumn(txtOther, "positive_other");
		table.addColumn(txtAll, "positive_all");
		
		table.addColumn(txtNewCases, "negative_newcases");
		table.addColumn(txtRelapses, "negative_relapses");
		table.addColumn(txtOther, "negative_other");
		table.addColumn(txtAll, "negative_all");
		
		table.addColumn(txtNewCases, "extrapulmonary_newcases");
		table.addColumn(txtRelapses, "extrapulmonary_relapses");
		table.addColumn(txtOther, "extrapulmonary_other");
		table.addColumn(txtAll, "extrapulmonary_all");

		table.addColumn(txtMale, "male");
		table.addColumn(txtFemale, "female");
		table.addColumn(txtAll, "all");
		
		table.addRow("TOTAL", "row");
	}


	/**
	 * Initialize layout of the table 4000 of the TB07 report
	 */
	private void initTable4000() {
		IndicatorTable table = getTable4000();
		
		table.addColumn(txtNewCases, "pulmonary_newcases");
		table.addColumn(txtRelapses, "pulmonary_relapses");
		table.addColumn(txtOther, "pulmonary_other");
		table.addColumn(txtAll, "pulmonary_all");
		
		table.addColumn(txtNewCases, "extrapulmonary_newcases");
		table.addColumn(txtRelapses, "extrapulmonary_relapses");
		table.addColumn(txtOther, "extrapulmonary_other");
		table.addColumn(txtAll, "extrapulmonary_all");

		table.addColumn(txtAll, "all");
		
		table.addRow("TOTAL", "row");
	}

	/**
	 * Create all tables of the report
	 */
	protected void createTables() {
		table2000 = new IndicatorTable();
		table3000 = new IndicatorTable();
		table4000 = new IndicatorTable();
		table5000 = new IndicatorTable();
	}


	/**
	 * @return the table1000
	 */
	public IndicatorTable getTable1000() {
		return getTable();
	}


	/**
	 * @return the table2000
	 */
	public IndicatorTable getTable2000() {
		if (table2000 == null)
			createTables();
		return table2000;
	}

	/**
	 * @return the table3000
	 */
	public IndicatorTable getTable3000() {
		return table3000;
	}

	/**
	 * @return the table4000
	 */
	public IndicatorTable getTable4000() {
		return table4000;
	}

	/**
	 * @return the table5000
	 */
	public IndicatorTable getTable5000() {
		return table5000;
	}

}
