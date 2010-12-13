package org.msh.tb.ua;

import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.AgeRange;
import org.msh.mdrtb.entities.enums.Gender;
import org.msh.mdrtb.entities.enums.HIVResult;
import org.msh.mdrtb.entities.enums.InfectionSite;
import org.msh.mdrtb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.tb.indicators.core.IndicatorCultureResult;
import org.msh.tb.indicators.core.IndicatorFilters;
import org.msh.tb.indicators.core.IndicatorMicroscopyResult;
import org.msh.tb.indicators.core.IndicatorTable;

@Name("reportTB07")
public class ReportTB07 extends Indicator2D {
	private static final long serialVersionUID = -8462692609433997419L;
	private static final String rowid = "row";

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
		initTable5000();

		IndicatorFilters filters = getIndicatorFilters();
		filters.setPatientType(null);
		filters.setMicroscopyResult(null);
		filters.setCultureResult(null);
		
		setConsolidated(true);
		generateTable1000();
		generateTable2000();
		generateTable3000();
		generateTable4000();
		generateTable5000();
	}


	/**
	 * Generate table 1000 report
	 */
	protected void generateTable1000() {
		IndicatorFilters filters = (IndicatorFilters)Component.getInstance("indicatorFilters");
		IndicatorTable table = getTable1000();
		
		// pulmonary positive
		filters.setMicroscopyResult(IndicatorMicroscopyResult.POSITIVE);
		filters.setInfectionSite(InfectionSite.PULMONARY);
		List<Object[]> lst = generateValuesByField("c.patientType, c.infectionSite, c.patient.gender", "c.patientType != null and c.infectionSite != null");
		addTableValues(table, lst, IndicatorMicroscopyResult.POSITIVE, InfectionSite.PULMONARY);
		
		// pulmonary negative
		filters.setMicroscopyResult(IndicatorMicroscopyResult.POSITIVE);
		String condition = "c.patientType != null and c.infectionSite != null and not " + getHQLMicroscopyCondition();
		filters.setMicroscopyResult(null);
		filters.setInfectionSite(InfectionSite.PULMONARY);
		lst = generateValuesByField("c.patientType, c.infectionSite, c.patient.gender", condition);
		addTableValues(table, lst, IndicatorMicroscopyResult.NEGATIVE, InfectionSite.PULMONARY);
		
		// extrapulmonary
		filters.setMicroscopyResult(null);
		filters.setInfectionSite(InfectionSite.EXTRAPULMONARY);
		lst = generateValuesByField("c.patientType, c.infectionSite, c.patient.gender", "c.patientType != null and c.infectionSite != null");
		addTableValues(table, lst, null, InfectionSite.EXTRAPULMONARY);
		addTableValues(getTable3000(), lst, null, InfectionSite.EXTRAPULMONARY);

/*		calcNegativeColumn(table, "newcases");
		calcNegativeColumn(table, "relapses");
		calcNegativeColumn(table, "other");
		calcNegativeColumn(table, "all");
*/
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
	

	private void addTableValues(IndicatorTable table, List<Object[]> lst, IndicatorMicroscopyResult result, InfectionSite infectionSite) {
		for (Object[] vals: lst) {
			PatientType patType = (PatientType)vals[0];
			InfectionSite site = null;
			if (infectionSite == null)
				 site = (InfectionSite)vals[1];
			else site = infectionSite;
		
			Gender gender = (Gender)vals[2];
			Long val = (Long)vals[3];

			String key = null;
			if (result != null) 
				 key = (result == IndicatorMicroscopyResult.POSITIVE ? "positive_": "negative_");
			// if both (extra and pulmonary), system assigns it to pulmonary
			else key = (site == InfectionSite.EXTRAPULMONARY ? "extrapulmonary_": "pulmonary_"); 
			
			addTableValue(table, key, patType, val.floatValue());
			
			if (gender == Gender.MALE)
				 table.addIdValue("male", rowid, val.floatValue());
			else table.addIdValue("female", rowid, val.floatValue());
			table.addIdValue("all", rowid, val.floatValue());
		}		
	}


	private String getPatientTypeKey(PatientType patientType) {
		if (PatientType.NEW.equals(patientType))
			return "newcases";
		else
		if (PatientType.RELAPSE.equals(patientType))
			return "relapses";
		else return "other";
	}

	/**
	 * Add a value to a table
	 * @param table Table to include a value 
	 * @param prefix Prefix of the column name
	 * @param patientType type of patient to compose the column name with the prefix
	 * @param value value to include
	 */
	private void addTableValue(IndicatorTable table, String prefix, PatientType patientType, Float value) {
		String key = prefix + getPatientTypeKey(patientType);
		
		table.addIdValue(key, rowid, value);
		table.addIdValue(prefix + "all", rowid, value);
	}
	
	
	private void generateTable2000() {
		IndicatorFilters filters = (IndicatorFilters)Component.getInstance("indicatorFilters");
		filters.setPatientType(PatientType.NEW);
		
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
			
			table.addIdValue(key, rowid, val);
			table.addIdValue(genderKey + "_all" , rowid, val);
			table.addIdValue("all", rowid, val);
		}
	}
	
	
	/**
	 * Generate values of table 3000 from the TB07 report
	 */
	private void generateTable3000() {
		IndicatorFilters filters = (IndicatorFilters)Component.getInstance("indicatorFilters");
		IndicatorTable table = getTable3000();

		filters.setInfectionSite(InfectionSite.PULMONARY);
		filters.setPatientType(null);
		
		// pulmonary positives
		String condition = getHQLTable3000Condition(IndicatorMicroscopyResult.POSITIVE, IndicatorCultureResult.POSITIVE);
		List<Object[]> lst = generateValuesByField("c.patientType, c.infectionSite, c.patient.gender", condition);
		addTableValues(table, lst, IndicatorMicroscopyResult.POSITIVE, null);

		// pulmonary negatives
		filters.setMicroscopyResult(IndicatorMicroscopyResult.POSITIVE);
		filters.setCultureResult(IndicatorCultureResult.POSITIVE);
		condition = "c.patientType != null and c.infectionSite != null and not " + getHQLMicroscopyCondition() +
			" and not " + getHQLCultureCondition();
		filters.setMicroscopyResult(null);
		filters.setCultureResult(null);
		filters.setInfectionSite(InfectionSite.PULMONARY);
		lst = generateValuesByField("c.patientType, c.infectionSite, c.patient.gender", condition);
		addTableValues(table, lst, IndicatorMicroscopyResult.NEGATIVE, null);

		// extrapulmonary is mounted by generateTable1000() method
		
/*		filters.setInfectionSite(InfectionSite.EXTRAPULMONARY);
		condition = "c.patientType != null and c.patient.gender != null " +
				"and (exists(select aux.id from ExamCulture aux where aux.tbcase.id = c.id) or " +
				"exists(select em.id from ExamMicroscopy em where em.tbcase.id = c.id))";
		lst = generateValuesByField("c.patientType, c.infectionSite, c.patient.gender", "c.patientType != null and c.infectionSite != null");
		addTableValues(table, lst, null);
*/	}


	/**
	 * Return condition of culture and microscopy for table 3000
	 * @return
	 */
	private String getHQLTable3000Condition(IndicatorMicroscopyResult microscopyResult, IndicatorCultureResult cultureResult) {
		IndicatorFilters filters = (IndicatorFilters)Component.getInstance("indicatorFilters");
		filters.setMicroscopyResult(microscopyResult);
		String condition = getHQLMicroscopyCondition();

		filters.setCultureResult(cultureResult);
		condition = "(" + condition + " or " + getHQLCultureCondition() + ")";

		filters.setMicroscopyResult(null);
		filters.setCultureResult(null);
		
		return "c.patientType != null and c.infectionSite != null and " + condition;
	}
	

	/**
	 * Generate values of table 4000 from the TB07 report
	 */
	private void generateTable4000() {
		IndicatorFilters filters = (IndicatorFilters)Component.getInstance("indicatorFilters");
		IndicatorTable table = getTable4000();

		filters.setInfectionSite(null);
		filters.setPatientType(null);
		filters.setMicroscopyResult(null);
		filters.setCultureResult(null);
		
		String condition = "c.infectionSite != null and c.patientType != null and " +
				"exists(select aux.id from ExamHIV aux where aux.tbcase.id=c.id and aux.result = " + HIVResult.POSITIVE.ordinal() + ")";
		List<Object[]> lst = generateValuesByField("c.patientType, c.infectionSite", condition);

		for (Object[] vals: lst) {
			PatientType ptype = (PatientType)vals[0];
			InfectionSite site = (InfectionSite)vals[1];
			Float total = ((Long)vals[2]).floatValue();
			
			if ((site == InfectionSite.PULMONARY) || (site == InfectionSite.EXTRAPULMONARY)) {
				String sitekey = (site == InfectionSite.PULMONARY ? "pulmonary_" : "extrapulmonary_");
				String cellkey = sitekey + getPatientTypeKey(ptype);
				table.addIdValue(cellkey, rowid, total);
				table.addIdValue(sitekey + "all", rowid, total);
				table.addIdValue("all", rowid, total);
			}
		}
	}

	/**
	 * Generate values of table 4000 from the TB07 report
	 */
	private void generateTable5000() {
		IndicatorFilters filters = (IndicatorFilters)Component.getInstance("indicatorFilters");
		IndicatorTable table = getTable5000();

		filters.setInfectionSite(null);
		filters.setPatientType(null);
		filters.setMicroscopyResult(IndicatorMicroscopyResult.POSITIVE);
		filters.setCultureResult(null);
		
		String condition = "c.registrationDate > (select min(exam.dateCollected) from ExamMicroscopy exam where exam.tbcase.id = c.id)"; 
		
		float num = calcNumberOfCases(condition);
		table.setValue("col2", rowid, num);
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
		
		table.addRow("TOTAL", rowid);
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
		
		table.addRow("TOTAL", rowid);
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
		
		table.addRow("TOTAL", rowid);
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
		
		table.addRow("TOTAL", rowid);
	}


	/**
	 * Initialize table 5000
	 */
	private void initTable5000() {
		IndicatorTable table = getTable5000();

		table.addColumn(getMessage("uk_UA.reports.tb07.5.tblheader2"), "col1");
		table.addColumn(getMessage("uk_UA.reports.tb07.5.tblheader3"), "col2");
		table.addColumn("%", "col3");
		
		table.addRow("TOTAL", rowid);
	}

	/**
	 * Create all tables of the report and generate indicators
	 */
	@Override
	protected void createTable() {
		table2000 = new IndicatorTable();
		table3000 = new IndicatorTable();
		table4000 = new IndicatorTable();
		table5000 = new IndicatorTable();
		super.createTable();
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
			createTable();
		return table2000;
	}

	/**
	 * @return the table3000
	 */
	public IndicatorTable getTable3000() {
		if (table3000 == null)
			createTable();
		return table3000;
	}

	/**
	 * @return the table4000
	 */
	public IndicatorTable getTable4000() {
		if (table4000 == null)
			createTable();
		return table4000;
	}

	/**
	 * @return the table5000
	 */
	public IndicatorTable getTable5000() {
		if (table5000 == null)
			createTable();
		return table5000;
	}

}
