package org.msh.tb.ua;

import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.AgeRange;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.HIVResult;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.MicroscopyResult;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.tb.indicators.core.IndicatorCultureResult;
import org.msh.tb.indicators.core.IndicatorFilters;
import org.msh.tb.indicators.core.IndicatorMicroscopyResult;
import org.msh.tb.indicators.core.IndicatorTable;

import sun.java2d.pipe.GeneralCompositePipe;

@Name("reportTB07")
public class ReportTB07 extends Indicator2D {
	private static final long serialVersionUID = -8462692609433997419L;
	private static final String rowid = "row";

	private IndicatorTable table2000;
	private IndicatorTable table3000;
	private IndicatorTable table4000;
	private IndicatorTable table5000;

	//	private int numcases;

	private String txtNewCases, txtRelapses, txtOther, txtAll, txtMale, txtFemale;

	/*public int getNumcases(){
		return numcases;
	}
	public void setNumcases(int n){
		this.numcases = n;
	}*/

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
	@Override
	protected String getHQLJoin() {
		return null;
	}

	@Override
	protected String getHQLMicroscopyCondition() {
		IndicatorFilters filters = getIndicatorFilters();
		if (filters.getMicroscopyResult() == null)
			return null;

		String cond = getHQLMicroscopyResultCondition(filters.getMicroscopyResult());

		return "exists(select ex.id from ExamMicroscopy ex where ex.result " + cond + " and ex.tbcase.id = c.id" +
		" and ex.dateCollected = (select min(sp.dateCollected) from ExamMicroscopy sp where sp.tbcase.id = c.id)" +
		" and ex.dateCollected >= c.registrationDate)";// and c.id = 1095798
	}

	/**
	 * Generate table 1000 report
	 */
	protected void generateTable1000() {
		IndicatorTable table = getTable1000();
		fetchOnlyMicroscopy(IndicatorMicroscopyResult.POSITIVE, table);
		fetchOnlyMicroscopy(IndicatorMicroscopyResult.NEGATIVE, table);
		fetchExtraPulmonary(table);
	}

	/**
	 * Fetch only extrapulmonary cases
	 * @param table table to write
	 */
	private void fetchExtraPulmonary(IndicatorTable table) {
		getIndicatorFilters().setMicroscopyResult(null);
		getIndicatorFilters().setCultureResult(null);
		getIndicatorFilters().setInfectionSite(InfectionSite.EXTRAPULMONARY);
		List<Object[]> lst = generateValuesByField(getGeneralGroupBy(), "c.patientType in (0,1,2,3,4,5) and c.infectionSite != null and c.state >= " + CaseState.ONTREATMENT.ordinal());
		addTableValues(table, lst, null, InfectionSite.EXTRAPULMONARY);
		addTableValues(getTable3000(), lst, null, InfectionSite.EXTRAPULMONARY);
		getIndicatorFilters().setInfectionSite(InfectionSite.BOTH);
		lst = generateValuesByField(getGeneralGroupBy(), "c.patientType in (0,1,2,3,4,5) and c.infectionSite != null and c.state >= " + CaseState.ONTREATMENT.ordinal());
		addTableValues(table, lst, null, InfectionSite.EXTRAPULMONARY);
		addTableValues(getTable3000(), lst, null, InfectionSite.EXTRAPULMONARY);
	}
	/**
	 * Fetch only microscopy results and write to the table
	 * @param res sort of result
	 * @param table table to write
	 */
	private void fetchOnlyMicroscopy(IndicatorMicroscopyResult res,  IndicatorTable table) {
		getIndicatorFilters().setMicroscopyResult(res);
		getIndicatorFilters().setCultureResult(null);
		getIndicatorFilters().setInfectionSite(InfectionSite.PULMONARY);
		List<Object[]> lst = generateValuesByField(getGeneralGroupBy(), getGeneralCondition());
		addTableValues(table, lst, res, InfectionSite.PULMONARY);
		getIndicatorFilters().setInfectionSite(InfectionSite.BOTH);
		lst = generateValuesByField(getGeneralGroupBy(), getGeneralCondition());
		addTableValues(table, lst, res, InfectionSite.PULMONARY);
	}
	/**
	 * general group by fields
	 * @return
	 */
	private String getGeneralGroupBy() {
		return "c.patientType, c.infectionSite, c.patient.gender";
	}
	/**
	 * general search condition
	 * @return
	 */
	private String getGeneralCondition() {
		return "c.patientType in (0,1,2,3,4,5)and c.infectionSite != null and c.state >= " + CaseState.ONTREATMENT.ordinal();
	}
	/**
	 * Print list cases for debug only
	 * insert this one, after filters and criteria
	 * @param header header for print
	 * @param criteria additional search criteria or null, if only filters interested
	 */
	private void printCases(String header, String criteria) {
		List<Object[]> lst1 = generateValuesByField("c", criteria);
		System.out.println(header);
		for(Object[] o : lst1){
			TbCase c = (TbCase)o[0];
			System.out.println(c.getDisplayCaseNumber()+ " id " + c.getId());
		}


	}

	/*
	 *TODO BOTH
	 */
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


	/**
	 * Generate data for table 2000
	 */
	private void generateTable2000() {
		IndicatorFilters filters = (IndicatorFilters)Component.getInstance("indicatorFilters");
		filters.setPatientType(PatientType.NEW);

		filters.setMicroscopyResult(IndicatorMicroscopyResult.POSITIVE);
		filters.setInfectionSite(InfectionSite.PULMONARY);
		List<Object[]> lst = generateValuesByField("c.age, c.patient.gender", "c.age != null and c.patient.gender != null and c.state >= " + CaseState.ONTREATMENT.ordinal());
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
	 * TODO CHANGE CONDITIONS TO OR
	 */
		private void generateTable3000() {
			IndicatorTable table = getTable3000();
			List<Object[]> lst;
			getIndicatorFilters().setInfectionSite(InfectionSite.PULMONARY);
			getIndicatorFilters().setPatientType(null);
			// columns 1-4
			
			// get with positive microscopy and negative culture
			getIndicatorFilters().setMicroscopyResult(IndicatorMicroscopyResult.POSITIVE);
			getIndicatorFilters().setCultureResult(IndicatorCultureResult.NEGATIVE);
			lst = generateValuesByField(getGeneralGroupBy(), getGeneralCondition());
			addTableValues(table, lst, IndicatorMicroscopyResult.POSITIVE, null);	
			
			// get all with negative microscopy and positive culture
			getIndicatorFilters().setMicroscopyResult(IndicatorMicroscopyResult.NEGATIVE);
			getIndicatorFilters().setCultureResult(IndicatorCultureResult.POSITIVE);
			lst = generateValuesByField(getGeneralGroupBy(), getGeneralCondition());
			addTableValues(table, lst, IndicatorMicroscopyResult.POSITIVE, null);	
			// get all with both positive
			getIndicatorFilters().setMicroscopyResult(IndicatorMicroscopyResult.POSITIVE);
			getIndicatorFilters().setCultureResult(IndicatorCultureResult.POSITIVE);
			//printCases("both positive", getGeneralCondition());
			lst = generateValuesByField(getGeneralGroupBy(), getGeneralCondition());
			addTableValues(table, lst, IndicatorMicroscopyResult.POSITIVE, null);	
			
			// both negative, columns 5-8
			getIndicatorFilters().setMicroscopyResult(IndicatorMicroscopyResult.NEGATIVE);
			getIndicatorFilters().setCultureResult(IndicatorCultureResult.NEGATIVE);
			//printCases("both negative", getGeneralCondition());
			lst = generateValuesByField(getGeneralGroupBy(), getGeneralCondition());
			addTableValues(table, lst, IndicatorMicroscopyResult.NEGATIVE, null);	
			
			
//			
//			filters.setInfectionSite(InfectionSite.PULMONARY);
//			filters.setPatientType(null);
//			
//			// pulmonary all positives microscopy
//			String condition = getHQLTable3000Condition(IndicatorMicroscopyResult.POSITIVE, IndicatorCultureResult.POSITIVE);
//			List<Object[]> lst = generateValuesByField("c.patientType, c.infectionSite, c.patient.gender", condition);
//			addTableValues(table, lst, IndicatorMicroscopyResult.POSITIVE, null);
//	
//			// pulmonary negatives
//			filters.setMicroscopyResult(IndicatorMicroscopyResult.NEGATIVE);
//			filters.setCultureResult(IndicatorCultureResult.NEGATIVE);
//			condition = "c.patientType in (0,1,2,3,4,5) and c.state >= " + CaseState.ONTREATMENT.ordinal() +" and c.infectionSite != null and " + getHQLMicroscopyCondition() +
//				" and " + getHQLCultureCondition();
//			filters.setMicroscopyResult(null);
//			filters.setCultureResult(null);
//			filters.setInfectionSite(InfectionSite.PULMONARY);
//			lst = generateValuesByField("c.patientType, c.infectionSite, c.patient.gender", condition);
//			addTableValues(table, lst, IndicatorMicroscopyResult.NEGATIVE, null);
	
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

		return "c.patientType in (0,1,2,3,4,5) and c.state >= " + CaseState.ONTREATMENT.ordinal() +" and c.infectionSite != null and " + condition;
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

		String condition = "c.infectionSite != null and c.state >= " + CaseState.ONTREATMENT.ordinal() +" and c.patientType in (0,1,2,3,4,5) and " +
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

		int numcases = filters.getNumcases();
		table.setValue("col2", rowid, num);
		table.setValue("col1", rowid, (float)numcases);
		if (numcases != 0)
			table.addIdValue("col3", rowid, num*100/numcases);
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
			table.addColumn(txtMale, r.getIniAge() + "_male");
			table.addColumn(txtFemale, r.getIniAge() + "_female");
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
