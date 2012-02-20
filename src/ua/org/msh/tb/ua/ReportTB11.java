package org.msh.tb.ua;

import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.tb.indicators.core.IndicatorCultureResult;
import org.msh.tb.indicators.core.IndicatorFilters;
import org.msh.tb.indicators.core.IndicatorMicroscopyResult;
import org.msh.tb.indicators.core.IndicatorTable;
import org.msh.utils.date.DateUtils;

@Name("reportTB11")
public class ReportTB11 extends Indicator2D {
	private static final long serialVersionUID = -8510290531301762778L;
	private String extraSubQuery = "";
	//private /*static final*/ String rowid;// = "row";
	
	/**
	 * Total number of cases in the indicator 
	 */
	private int numcases;
	
	private IndicatorTable table1;
	/* (non-Javadoc)
	 * @see org.msh.tb.indicators.core.Indicator#createIndicators()
	 */
	@Override
	protected void createIndicators() {
		table1 = getTable();
		initTable1();
		
		IndicatorFilters filters = getIndicatorFilters();
		filters.setPatientType(null);
		filters.setMicroscopyResult(null);
		filters.setCultureResult(null);
		
		setConsolidated(true);
		
		generateTable1();
	}

	protected void initTable1() {
		IndicatorTable table = getTable1();
		for (int i = 1; i < 31; i++) {
			table.addRow(getMessage("uk_UA.reports.tb11.1.rowheader"+i), "row"+i);
		}
		String total = getMessage("uk_UA.reports.tb11.1.total");

		table.addColumn(total, "newcases");
		table.addColumn("%", "newcases_perc");

		table.addColumn(total, "relapses");
		table.addColumn("%", "relapses_perc");

		table.addColumn(total, "others");
		table.addColumn("%", "others_perc");
		
			
	}
	
	protected void generateTable1() {
		String cols[] = {"newcases", "relapses", "others"};
		// calculate the number of cases
		numcases = calcNumberOfCases("c.state >= " + CaseState.ONTREATMENT.ordinal());
		List<Object[]> lst;
		String hasMicr = "";
		if (numcases == 0)
			return;
		getIndicatorFilters().setInfectionSite(InfectionSite.PULMONARY);
		setConsolidated(false);
		for (int i = 1; i < 5; i++){
		//String hqlCulture = "(select min(e.dateCollected) from ExamCulture e " +
		//		"where e.tbcase.id = c.id and e.result " + getHQLCultureResultCondition(IndicatorCultureResult.NEGATIVE) + ")";
		//String hqlMicroscopy = "(select min(e.dateCollected) from ExamMicroscopy e " +
		//		"where e.tbcase.id = c.id and e.result " + getHQLMicroscopyResultCondition(IndicatorMicroscopyResult.NEGATIVE) + ")";
		// check if it has culture or microscopy
		//String hasCult = "(select min(e.dateCollected) from ExamCulture e where e.tbcase.id = c.id)";
		String hqlMicroscopy = "(select min(e.dateCollected) from ExamMicroscopy e where e.tbcase.id = c.id";
		String examMethod = "";
		if (i==1) {
			examMethod = "ExamMicroscopy";
			hasMicr = "e.result in (0,1,2,3,4)";}
		if (i==2) {
			examMethod = "ExamMicroscopy";
			hasMicr = "e.result in (1,2,3,4)";}
		if (i==3) {
			examMethod = "ExamCulture";
			hasMicr = "e.result in (0,1,2,3,4)";}
		if (i==4) {
			examMethod = "ExamCulture";
			hasMicr = "e.result in (1,2,3,4)";}
		
		setHQLSelect("select c.treatmentPeriod.iniDate, c.patientType, c.state ");// + 
		// extraSubQuery = 
			//	hqlCulture + ", " + 
			//	hqlMicroscopy +	 
			//	hasCult + ", " + 
			//	hasMicr+ ")");
		setCondition("c.state >= " + CaseState.ONTREATMENT.ordinal());
		
		extraSubQuery = " and exists(select e.id from "+examMethod+" e where "+hasMicr+" and e.tbcase.id = c.id and e.dateCollected = (select min(sp.dateCollected) from "+examMethod+" sp	where sp.tbcase.id = c.id)) ";
		
		lst = createQuery()
			.getResultList();

		for (Object[] vals: lst) {
			//boolean noexam = vals[5] == null && vals[6] == null;
			addValueTable((Date)vals[0],(PatientType)vals[1], (CaseState)vals[2],"row"+i);
		}
		
		for (String colid: cols) {
			calcPercentage(colid, "row"+i);
		}
		}
		
		
	}
	
	private void calcPercentage(String colid, String rowid) {
		float total = numcases;//table1.getCellAsFloat("numcases", rowid);
		if (total == 0)
			return;
		
		float num = table1.getCellAsFloat(colid, rowid);
		
		table1.setValue(colid + "_perc", rowid, num * 100F / total);
	}
	
	/* (non-Javadoc)
	 * @see org.msh.tb.indicators.core.CaseHQLBase#createHQL()
	 */
	@Override
	protected String createHQL() {
		String s = getHQLSelect();
		
		String hql = getHQLFrom() + " ";
		if (s != null)
			hql = s + " " + hql;
		
		String joins = getHQLJoin();
		if (joins != null)
			hql += joins + " ";

		hql += getHQLWhere();
			
		hql += extraSubQuery;
		
		return hql;		
	}
	
	/**
	 * Add a value to a specific cell in the table based on the dates of the case
	 * @param dtIniTreat
	 * @param dtCult
	 * @param dtMicro
	 */
	protected void addValueTable(Date dtIniTreat, PatientType ptype, CaseState state, String rowid) {
		if (dtIniTreat == null)
			return;

		// select row id
		String colkey;
		switch (ptype) {
		case NEW: 
			colkey = "newcases";
			break;
		case RELAPSE: 
			colkey = "relapses";
			break;
		default: 
			colkey = "others";
		}
		table1.addIdValue(colkey, rowid, 1F);
		// no culture or microscopy to the case ?
		/*if (noexam)
			
		else {
			// is negative ?
			if ((dtCult != null) && (dtMicro != null)) {
				int negativeMonth = 0;
				// calculate month of negativation
				int m1 = DateUtils.monthsBetween(dtIniTreat, dtCult);
				int m2 = DateUtils.monthsBetween(dtIniTreat, dtMicro);
				negativeMonth = (m1 > m2? m1: m2);
				
				if (negativeMonth <= 2)
					table1.addIdValue("month2", rowkey, 1F);
				
				if (negativeMonth <= 3)
					table1.addIdValue("month3", rowkey, 1F);
				
				if (negativeMonth <= 4)
					table1.addIdValue("month4", rowkey, 1F);
				
				if (negativeMonth > 4)
					table1.addIdValue("others", rowkey, 1F);
			}
			else table1.addIdValue("others", rowkey, 1F);
		}
		
		table1.addIdValue("numcases", rowkey, 1F);*/
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.indicators.core.CaseHQLBase#getHQLJoin()
	 */
	protected String getHQLJoin() {
		return null;
	}
	
	/**
	 * Return table 1000 report, from the original table object from {@link IndicatorTable}
	 * @return
	 */
	public IndicatorTable getTable1() {
		return getTable();
	}
	/*
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
			if (result == null) {
				key = "negative_";
				addTableValue(table, key, patType, val.floatValue());	
			}
			
			key = "positive_";
			addTableValue(table, key, patType, val.floatValue());
				
			if (gender == Gender.MALE)
				 table.addIdValue("male", rowid, val.floatValue());
			else table.addIdValue("female", rowid, val.floatValue());
			table.addIdValue("all", rowid, val.floatValue());
		}		
	}

	@Override
	protected String getHQLInfectionSite() {
		IndicatorFilters filters = getIndicatorFilters();
		if  (filters.getInfectionSite() == InfectionSite.PULMONARY)
			 return "c.infectionSite in (" + InfectionSite.PULMONARY.ordinal() + "," + InfectionSite.BOTH.ordinal() + ")";
		else return super.getHQLInfectionSite();
	}
	
	
	private String getPatientTypeKey(PatientType patientType) {
		if (PatientType.NEW.equals(patientType))
			return "newcases";
		else
		if (PatientType.RELAPSE.equals(patientType))
			return "relapses";
		else return "other";
	}

	*
	 * Add a value to a table
	 * @param table Table to include a value 
	 * @param prefix Prefix of the column name
	 * @param patientType type of patient to compose the column name with the prefix
	 * @param value value to include
	 
	private void addTableValue(IndicatorTable table, String prefix, PatientType patientType, Float value) {
		String key = prefix + getPatientTypeKey(patientType);
		
		table.addIdValue(key, rowid, value);
		table.addIdValue(prefix + "all", rowid, value);
	}*/
}