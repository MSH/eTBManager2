package org.msh.tb.ua;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.ExamDST;
import org.msh.tb.entities.ExamDSTResult;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.tb.indicators.core.IndicatorFilters;
import org.msh.tb.indicators.core.IndicatorTable;

@Name("reportTB11")
public class ReportTB11 extends Indicator2D {
	private static final long serialVersionUID = -8510290531301762778L;
	private String extraSubQuery = "";
	String cols[] = {"newcases", "relapses", "others"};
	
	//private /*static final*/ String rowid;// = "row";
	
	/**
	 * Total number of cases in the indicator 
	 */
	private float numcases;
	
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
		// calculate the number of cases
		numcases = calcNumberOfCases("c.state >= " + CaseState.ONTREATMENT.ordinal());
		//numcases =1;
		List<TbCase> lst;
		
		String res = "";
		String resdst = "";
		String dstQuery = "";
		String [] sub = {"H","R","E","S"};
		
		int indBlockTab = 0;
		String examMethod = "ExamDST";
		res = ".result = 1"; 
		resdst = " in ('H','R','E','S')";
		
		if (numcases == 0)
			return;
		getIndicatorFilters().setInfectionSite(InfectionSite.PULMONARY);
		setConsolidated(false);
		for (int i = 1; i < 31; i++){
			switch (i) {
			case 1: {
				examMethod = "ExamMicroscopy";
				res = ".result in (0,1,2,3,4)";
				indBlockTab = 1;
				dstQuery = "";
				break;}
			case 2: {
				examMethod = "ExamMicroscopy";
				res = ".result in (1,2,3,4)";
				indBlockTab = 1;
				dstQuery = "";
				break;}
			case 3: {
				examMethod = "ExamCulture";
				res = ".result in (0,1,2,3,4)";
				indBlockTab = 2;
				dstQuery = "";
				break;}
			case 4: {
				examMethod = "ExamCulture";
				res = ".result in (1,2,3,4)";
				indBlockTab = 2;
				dstQuery = "";
				break;}
			case 5: {
				examMethod = "ExamDST";
				resdst = " in ('H','R','E','S')"; //ex"+res+" and
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				indBlockTab = 3;
				break;}
			case 6: {
				examMethod = "ExamDST";
				res = ".result = 2"; // 2 ~ Susceptible!
				resdst = " in ('H','R','E','S')"; 
				/*dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+
				" and (ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 = 'H')" +
				" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 = 'R')" +
				" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 = 'E')" +
				" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 = 'S')" +
				"))";*/
				indBlockTab = 3;
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				break;}
			case 7: {
				examMethod = "ExamDST";
				res = ".result = 1"; // 1 ~ Resistant!
				resdst = " in ('H','R','E','S')";
				/*dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+
						" and (ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 = 'H')" +
						" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 = 'R')" +
						" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 = 'E')" +
						" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 = 'S')" +
						"))";*/
				indBlockTab = 3;
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				break;}

			case 9: case 14:case 15: case 17:{
				sub[0] = "H";
				sub[1] = "R"; 
				sub[2] = "E"; 
				sub[3] = "S";
				res = ".result = 1"; 
				resdst = " in ('H','R','E','S')";
				indBlockTab = 4;
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				break;}
			
			case 10: {
				sub[0] = "R";
				sub[1] = "H"; 
				sub[2] = "E"; 
				sub[3] = "S"; 
				res = ".result = 1"; 
				resdst = " in ('H','R','E','S')";
				indBlockTab = 4;
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				break;}
			case 11: {
				sub[0] = "E";
				sub[1] = "R"; 
				sub[2] = "H"; 
				sub[3] = "S";
				res = ".result = 1"; 
				resdst = " in ('H','R','E','S')";
				indBlockTab = 4;
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				break;}
			case 12: {
				sub[0] = "S";
				sub[1] = "R"; 
				sub[2] = "E"; 
				sub[3] = "H";
				res = ".result = 1"; 
				resdst = " in ('H','R','E','S')";
				indBlockTab = 4;
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				break;}
			case 16: {
				sub[0] = "H";
				sub[1] = "R"; 
				sub[2] = "S"; 
				sub[3] = "E";
				res = ".result = 1"; 
				resdst = " in ('H','R','E','S')";
				indBlockTab = 4;
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				break;}
			case 21:{
				sub[0] = "H";
				sub[1] = "E"; 
				sub[2] = "S"; 
				sub[3] = "R";
				res = ".result = 1"; 
				resdst = " in ('H','R','E','S')";
				indBlockTab = 4;
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				break;}
			case 25:{
				sub[0] = "R";
				sub[1] = "E"; 
				sub[2] = "S"; 
				sub[3] = "H";
				res = ".result = 1"; 
				resdst = " in ('H','R','E','S')";
				indBlockTab = 4;
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				break;}
			
			case 19:{
				sub[0] = "H";
				sub[1] = "E"; 
				sub[2] = "R"; 
				sub[3] = "S";
				res = ".result = 1"; 
				resdst = " in ('H','R','E','S')";
				indBlockTab = 4;
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				break;}
			case 20:{
				sub[0] = "H";
				sub[1] = "S"; 
				sub[2] = "R"; 
				sub[3] = "E";
				res = ".result = 1"; 
				resdst = " in ('H','R','E','S')";
				indBlockTab = 4;
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				break;}
			case 23:{
				sub[0] = "R"; 
				sub[1] = "S";
				sub[2] = "H";
				sub[3] = "E"; 
				res = ".result = 1"; 
				resdst = " in ('H','R','E','S')";
				indBlockTab = 4;
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				break;}
			case 24:{
				sub[0] = "R"; 
				sub[1] = "E";
				sub[2] = "H";
				sub[3] = "S"; 
				res = ".result = 1"; 
				resdst = " in ('H','R','E','S')";
				indBlockTab = 4;
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				break;}
			case 26:{
				sub[0] = "E"; 
				sub[1] = "S";
				sub[2] = "H";
				sub[3] = "R"; 
				res = ".result = 1"; 
				resdst = " in ('H','R','E','S')";
				indBlockTab = 4;
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				break;}
			
			default: 
				break;
			}
		setHQLSelect("select c");
		
		setCondition("c.state >= " + CaseState.ONTREATMENT.ordinal());
		extraSubQuery = " and c.treatmentPeriod.iniDate != '' and exists(select e.id from "+examMethod+" e where e"+(dstQuery == ""? res : dstQuery)+" and e.tbcase.id = c.id and e.dateCollected = (select min(sp.dateCollected) from "+examMethod+" sp where sp.tbcase.id = c.id)) ";
		
		lst = createQuery().getResultList();
		Iterator<TbCase> it = lst.iterator();
		outer:
		while(it.hasNext()){
			TbCase tc = it.next();
			//if (tc.isValidated() && tc.getNotificationUnit().getWorkspace().getId().equals(940358))
			switch (i) {
			case 1:case 2:case 3:case 4:case 5:{
				addValueTable(tc.getPatientType(),i);
				break;}
			case 6:{
				for (ExamDST ex: tc.getExamsDST()){
					int key =0;						
					if (rightTest(ex,tc))
						for (ExamDSTResult exr: ex.getResults()) {
							if (exr.getResult().ordinal()==2){
								if (exr.getSubstance().getAbbrevName().getName1().equals(sub[0]) || exr.getSubstance().getAbbrevName().getName1().equals(sub[1]) || exr.getSubstance().getAbbrevName().getName1().equals(sub[2]) || exr.getSubstance().getAbbrevName().getName1().equals(sub[3]))
										key++;
								if (key == 4)
									addValueTable(tc.getPatientType(),i); 
								}
							}	
						}
				break;}
			case 7:{
				for (ExamDST ex: tc.getExamsDST()){
					int key =0;						
					if (rightTest(ex,tc))
						for (ExamDSTResult exr: ex.getResults()) {
							if (exr.getResult().ordinal()==1){
								if (exr.getSubstance().getAbbrevName().getName1().equals(sub[0]) || exr.getSubstance().getAbbrevName().getName1().equals(sub[1]) || exr.getSubstance().getAbbrevName().getName1().equals(sub[2]) || exr.getSubstance().getAbbrevName().getName1().equals(sub[3]))
										key++;
								if (key == 4)
									addValueTable(tc.getPatientType(),i); 
								}
							}	
						}
				break;}
			
//			Case when 3 substances must be resistant and 1 other must be not resistant 			
			case 9:case 10:case 11:case 12:{
				for (ExamDST ex: tc.getExamsDST()) {
					//System.out.println(ex.getDateRelease());
					
					if (rightTest(ex,tc))
					for (ExamDSTResult exr: ex.getResults()) {
						if (exr.getResult().ordinal()==1){
						if (exr.getSubstance().getAbbrevName().getName1().equals(sub[1]) || exr.getSubstance().getAbbrevName().getName1().equals(sub[2]) || exr.getSubstance().getAbbrevName().getName1().equals(sub[3]))
								continue outer;
						if (exr.getSubstance().getAbbrevName().getName1().equals(sub[0]))
							addValueTable(tc.getPatientType(),i); 
						}	
					}
				}	
				break;}
//			Case when 2 substances must be resistant and 2 other must be not resistant 
			case 14:case 19:case 20:case 23:case 24:case 26:{
				for (ExamDST ex: tc.getExamsDST()) {
					int key=0;
					if (rightTest(ex,tc))
					for (ExamDSTResult exr: ex.getResults()) {
						if (exr.getResult().ordinal()==1){
						if (exr.getSubstance().getAbbrevName().getName1().equals(sub[2]) || exr.getSubstance().getAbbrevName().getName1().equals(sub[3]))
								continue outer;
						if (exr.getSubstance().getAbbrevName().getName1().equals(sub[0]) || exr.getSubstance().getAbbrevName().getName1().equals(sub[1]))
							key++;
						if (key == 2)
							addValueTable(tc.getPatientType(),i); 
						}	
					}
				}	
				break;}
//			Case when 1 substances must be resistant and 3 other must be not resistant 			
			case 15:case 16:case 21:case 25:{
				for (ExamDST ex: tc.getExamsDST()) {
					int key=0;
					if (rightTest(ex,tc))
					for (ExamDSTResult exr: ex.getResults()) {
						if (exr.getResult().ordinal()==1){
						if (exr.getSubstance().getAbbrevName().getName1().equals(sub[3]))
								continue outer;
						if (exr.getSubstance().getAbbrevName().getName1().equals(sub[2]) || exr.getSubstance().getAbbrevName().getName1().equals(sub[0]) || exr.getSubstance().getAbbrevName().getName1().equals(sub[1]))
							key++;
						if (key == 3)
							addValueTable(tc.getPatientType(),i); 
						}	
					}
				}	
				break;}
//			Case when 4 substances must be resistant 			
			case 17:{
				for (ExamDST ex: tc.getExamsDST()) {
					int key=0;
					if (rightTest(ex,tc))
					for (ExamDSTResult exr: ex.getResults()) {
						if (exr.getResult().ordinal()==1){
						
						if (exr.getSubstance().getAbbrevName().getName1().equals(sub[2]) || exr.getSubstance().getAbbrevName().getName1().equals(sub[3]) || exr.getSubstance().getAbbrevName().getName1().equals(sub[0]) || exr.getSubstance().getAbbrevName().getName1().equals(sub[1]))
							key++;
						if (key == 4)
							addValueTable(tc.getPatientType(),i); 
						}	
					}
				}	
				break;}
		}
		}
		}
		numcases = table1.getCellAsFloat("newcases", "row5")+table1.getCellAsFloat("relapses", "row5")+table1.getCellAsFloat("others", "row5");
		calcSummaryFields();
		for (int i = 1; i < 31; i++)
			for (String colid: cols) 
				calcPercentage(colid,i);	
}
	
	private void calcSummaryFields() {
		float total;
		for (int i = 1; i < 31; i++)
			switch (i){
			case 8:case 13:case 18:case 22:{
				calcSum4next(i);
				break;
			}
			case 27:{
				for (String colid: cols){
					total = table1.getCellAsFloat(colid, "row9");
					total += table1.getCellAsFloat(colid, "row13");
					total += table1.getCellAsFloat(colid, "row18");
					
					table1.addIdValue(colid, "row"+i, total);			
				}
				break;
			}
			case 28:{
				for (String colid: cols){
					total = table1.getCellAsFloat(colid, "row10");
					total += table1.getCellAsFloat(colid, "row13");
					total += table1.getCellAsFloat(colid, "row22");
					
					table1.addIdValue(colid, "row"+i, total);			
				}
				break;
			}
			case 29:{
				for (String colid: cols){
					total = table1.getCellAsFloat(colid, "row11");
					total += table1.getCellAsFloat(colid, "row15");
					total += table1.getCellAsFloat(colid, "row17");
					total += table1.getCellAsFloat(colid, "row19");
					total += table1.getCellAsFloat(colid, "row21");
					total += table1.getCellAsFloat(colid, "row24");
					total += table1.getCellAsFloat(colid, "row25");
					total += table1.getCellAsFloat(colid, "row26");
					
					table1.addIdValue(colid, "row"+i, total);			
				}
				break;
			}
			case 30:{
				for (String colid: cols){
					total = table1.getCellAsFloat(colid, "row12");
					total += table1.getCellAsFloat(colid, "row16");
					total += table1.getCellAsFloat(colid, "row17");
					total += table1.getCellAsFloat(colid, "row20");
					total += table1.getCellAsFloat(colid, "row21");
					total += table1.getCellAsFloat(colid, "row23");
					total += table1.getCellAsFloat(colid, "row25");
					total += table1.getCellAsFloat(colid, "row26");
					
					table1.addIdValue(colid, "row"+i, total);			
				}
				break;
			}
				
			}
	}
	
	private void calcSum4next(int i) {
		float total;
		for (String colid: cols){
			total = 0;
			for (int j = i+1; j < i+5; j++) {	
			total += table1.getCellAsFloat(colid, "row"+j);
			}
			table1.addIdValue(colid, "row"+i, total);			
		}	
	}
	
	private boolean rightTest(ExamDST ex, TbCase tc){
		Calendar dateTest = Calendar.getInstance();
		Calendar dateIniTreat = Calendar.getInstance();
		dateTest.setTime(ex.getDateCollected());
		dateIniTreat.setTime(tc.getTreatmentPeriod().getIniDate());
		long testperiod = dateTest.getTimeInMillis()-dateIniTreat.getTimeInMillis();
		testperiod/=86400000;
		if (testperiod<=30) 
			return true;
			else return false;
	}
		
	private void calcPercentage(String colid, int rowid) {
		float total;
		switch (rowid) {
		case 1: case 2: case 3: case 4:{
			total = table1.getCellAsFloat("newcases", "row"+rowid)+table1.getCellAsFloat("relapses", "row"+rowid)+table1.getCellAsFloat("others", "row"+rowid);
			break;}
		default: {
			total = numcases;
			break;}
		}
		if (total == 0)
			return;
		
		float num = table1.getCellAsFloat(colid, "row"+rowid);
		
		table1.setValue(colid + "_perc", "row"+rowid, num * 100F / total);
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
	protected void addValueTable(PatientType ptype, int rowid) {
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
		table1.addIdValue(colkey, "row"+rowid, 1F);
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