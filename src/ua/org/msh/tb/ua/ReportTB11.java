package org.msh.tb.ua;

import java.util.ArrayList;
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
	String [] cols = {"newcases", "relapses", "others"};
	
	/**
	 * Total number of cases in the indicator 
	 */
	private float [] numcases = new float [3];
	
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
		table.addColumn(getMessage("uk_UA.reports.tb11.1.headerN"), "N");
		
		for (int i = 1; i < 31; i++) {
			table.addRow(getMessage("uk_UA.reports.tb11.1.rowheader"+i), "row"+i);
			table.setValue("N", "row"+i, (float) i);
		}
		String total = getMessage("uk_UA.reports.tb11.1.total");
		
		table.addColumn(total, "newcases");
		table.addColumn("%", "newcases_perc");

		table.addColumn(total, "relapses");
		table.addColumn("%", "relapses_perc");

		table.addColumn(total, "others");
		table.addColumn("%", "others_perc");
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
	
	protected void generateTable1() {
		// calculate the number of cases
		getIndicatorFilters().setInfectionSite(InfectionSite.PULMONARY);
		
		numcases[0] = calcNumberOfCases("c.state >= " + CaseState.ONTREATMENT.ordinal()+" and c.patientType = " + PatientType.NEW.ordinal());
		numcases[1] = calcNumberOfCases("c.state >= " + CaseState.ONTREATMENT.ordinal()+" and c.patientType = " + PatientType.RELAPSE.ordinal());
		numcases[2] = calcNumberOfCases("c.state >= " + CaseState.ONTREATMENT.ordinal()+" and c.patientType in (0,1,2,3,4,5)");
		numcases[2] -= numcases[0]+numcases[1];
		List<TbCase> lst;
		
		String res = "";
		String resdst = "";
		String dstQuery = "";
		
		/**
		 * Necessary order of substances 
		 */
		String [] sub = {"H","R","E","S"};
		
		String examMethod = "ExamDST";
		res = ".result = 1"; 
		resdst = " in ('H','R','E','S')";
		
		if (numcases[0]+numcases[1]+numcases[2] == 0)
			return;
		setConsolidated(false);
		for (int i = 1; i < 31; i++){
			switch (i) {
			case 1: {
				examMethod = "ExamMicroscopy";
				res = ".result in (0,1,2,3,4)";
				dstQuery = "";
				break;}
			case 2: {
				examMethod = "ExamMicroscopy";
				res = ".result in (1,2,3,4)";
				dstQuery = "";
				break;}
			case 3: {
				examMethod = "ExamCulture";
				res = ".result in (0,1,2,3,4)";
				dstQuery = "";
				break;}
			case 4: {
				examMethod = "ExamCulture";
				res = ".result in (1,2,3,4)";
				dstQuery = "";
				break;}
			/*case 5: {
				examMethod = "ExamDST";
				resdst = " in ('H','R','E','S')"; //without ex"+res+" and
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				break;}*/
			case 6: {
				examMethod = "ExamDST";
				res = ".result = 2"; // 2 ~ Susceptible!
				resdst = " in ('H','R','E','S')"; 
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				break;}
			case 9: case 14:case 15: case 17:{
				sub[0] = "H";
				sub[1] = "R"; 
				sub[2] = "E"; 
				sub[3] = "S";
				res = ".result = 1"; 
				resdst = " in ('H','R','E','S')";
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				break;}
			
			case 10: {
				sub[0] = "R";
				sub[1] = "H"; 
				sub[2] = "E"; 
				sub[3] = "S"; 
				res = ".result = 1"; 
				resdst = " in ('H','R','E','S')";
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				break;}
			case 11: {
				sub[0] = "E";
				sub[1] = "R"; 
				sub[2] = "H"; 
				sub[3] = "S";
				res = ".result = 1"; 
				resdst = " in ('H','R','E','S')";
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				break;}
			case 12: {
				sub[0] = "S";
				sub[1] = "R"; 
				sub[2] = "E"; 
				sub[3] = "H";
				res = ".result = 1"; 
				resdst = " in ('H','R','E','S')";
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				break;}
			case 16: {
				sub[0] = "H";
				sub[1] = "R"; 
				sub[2] = "S"; 
				sub[3] = "E";
				res = ".result = 1"; 
				resdst = " in ('H','R','E','S')";
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				break;}
			case 21:{
				sub[0] = "H";
				sub[1] = "E"; 
				sub[2] = "S"; 
				sub[3] = "R";
				res = ".result = 1"; 
				resdst = " in ('H','R','E','S')";
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				break;}
			case 25:{
				sub[0] = "R";
				sub[1] = "E"; 
				sub[2] = "S"; 
				sub[3] = "H";
				res = ".result = 1"; 
				resdst = " in ('H','R','E','S')";
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				break;}
			
			case 19:{
				sub[0] = "H";
				sub[1] = "E"; 
				sub[2] = "R"; 
				sub[3] = "S";
				res = ".result = 1"; 
				resdst = " in ('H','R','E','S')";
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				break;}
			case 20:{
				sub[0] = "H";
				sub[1] = "S"; 
				sub[2] = "R"; 
				sub[3] = "E";
				res = ".result = 1"; 
				resdst = " in ('H','R','E','S')";
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				break;}
			case 23:{
				sub[0] = "R"; 
				sub[1] = "S";
				sub[2] = "H";
				sub[3] = "E"; 
				res = ".result = 1"; 
				resdst = " in ('H','R','E','S')";
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				break;}
			case 24:{
				sub[0] = "R"; 
				sub[1] = "E";
				sub[2] = "H";
				sub[3] = "S"; 
				res = ".result = 1"; 
				resdst = " in ('H','R','E','S')";
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				break;}
			case 26:{
				sub[0] = "E"; 
				sub[1] = "S";
				sub[2] = "H";
				sub[3] = "R"; 
				res = ".result = 1"; 
				resdst = " in ('H','R','E','S')";
				dstQuery = ".id in (select ex.exam.id from ExamDSTResult ex where ex"+res+" and ex.substance.id in (select sub.id from Substance sub where sub.abbrevName.name1 "+resdst+"))";
				break;}
			default: 
				break;
			}
		setHQLSelect("select c");
		
		setCondition("c.state >= " + CaseState.ONTREATMENT.ordinal()+" and c.patientType in (0,1,2,3,4,5) and exists(select e.id from "+examMethod+" e where e"+(dstQuery == ""? res : dstQuery)+" and e.tbcase.id = c.id and e.dateCollected = (select min(sp.dateCollected) from "+examMethod+" sp where sp.tbcase.id = c.id)) ");
		
		lst = createQuery().getResultList();
		Iterator<TbCase> it = lst.iterator();
		
		int res0=0;
		int res1=0;
		int res2=0;
		int res3=0;
		
		while(it.hasNext()){
			TbCase tc = it.next();
			res0=0;
			res1=0;
			res2=0;
			res3=0;
			switch (i) {
			// culture and microscopy
				case 1:case 2:case 3:case 4:{
					addValueTable(tc.getPatientType(),i);
					break;}
			//DST-tests without summary fields
				case 6:case 9:case 10:case 11:case 12:case 14:case 19:case 20:case 23:case 24:case 26:case 15:case 16:case 21:case 25:case 17:{
					ExamDST ex = rightDSTTest(tc);
					if (ex != null){
						for (ExamDSTResult exr: ex.getResults()) 
						{
							if (exr.getSubstance().getAbbrevName().getName1().equals(sub[0])) res0 = exr.getResult().ordinal();	
							if (exr.getSubstance().getAbbrevName().getName1().equals(sub[1])) res1 = exr.getResult().ordinal();	
							if (exr.getSubstance().getAbbrevName().getName1().equals(sub[2])) res2 = exr.getResult().ordinal();	
							if (exr.getSubstance().getAbbrevName().getName1().equals(sub[3])) res3 = exr.getResult().ordinal();	
						}					
						switch (i){
		//				    all susceptible
							case 6:{
								if (res0 == 2 && res1 == 2 && res2 == 2 && res3 == 2) 
									addValueTable(tc.getPatientType(),i); 	
								break;}
		//					3 substances must be resistant and 1 other must be not resistant 			
							case 9:case 10:case 11:case 12:{
								if (res0 == 1 && res1 !=1 && res2 !=1 && res3 !=1) 
									addValueTable(tc.getPatientType(),i); 	
								break;}
		//					2 substances must be resistant and 2 other must be not resistant 
							case 14:case 19:case 20:case 23:case 24:case 26:{
								if (res0 == 1 && res1 ==1 && res2 !=1 && res3 !=1) 
									addValueTable(tc.getPatientType(),i); 
								break;}
		//					1 substances must be resistant and 3 other must be not resistant 			
							case 15:case 16:case 21:case 25:{
								if (res0 == 1 && res1 ==1 && res2 ==1 && res3 !=1) 
									addValueTable(tc.getPatientType(),i); 
								break;}
		//					4 substances must be resistant 			
							case 17:{
								if (res0 == 1 && res1 ==1 && res2 !=1 && res3 ==1) 
									addValueTable(tc.getPatientType(),i); 
								break;}
						}
					}
					break;}
		}
		}
		}
		calcSummaryFields();
		for (int i = 1; i < 31; i++)
			for (String colid: cols) 
				calcPercentage(colid,i);	
}

	/**
	 * Calculate fields, which must be sums of some other fields
	*/
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
		//Row 7
		for (String colid: cols){
			total = table1.getCellAsFloat(colid, "row8");
			total += table1.getCellAsFloat(colid, "row13");
			total += table1.getCellAsFloat(colid, "row18");
			total += table1.getCellAsFloat(colid, "row22");
			total += table1.getCellAsFloat(colid, "row26");
			
			table1.addIdValue(colid, "row7", total);			
		}
		//Row 5
		for (String colid: cols){
			total = table1.getCellAsFloat(colid, "row6");
			total += table1.getCellAsFloat(colid, "row7");
			
			table1.addIdValue(colid, "row5", total);			
		}
		
	}
	
	/**
	 * Calculate sum of 4 next down located fields 
	*/
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
	
	/**
	 * @return DST-test, which is up to quality, namely first month of treatment and worst test from several 
	*/
	
	public ExamDST rightDSTTest(TbCase tc){
		Calendar dateTest = Calendar.getInstance();
		Calendar dateIniTreat = Calendar.getInstance();
		int res=0;
		ArrayList<ExamDST> lst = new ArrayList<ExamDST>();
		for (ExamDST ex: tc.getExamsDST()){
			dateTest.setTime(ex.getDateCollected());
			if (tc.getTreatmentPeriod()!=null)
				dateIniTreat.setTime(tc.getTreatmentPeriod().getIniDate());
			else
				dateIniTreat.setTime(tc.getDiagnosisDate());
			long testperiod = dateTest.getTimeInMillis()-dateIniTreat.getTimeInMillis();
			testperiod/=86400000;
			
			if (testperiod<=30) {
				res++;
				lst.add(ex);
			}	
		}
		
		switch (lst.size()) {
		case 0: 
			return null;
		case 1: 
			return lst.get(0);
		default:
			return WorstRes(lst);
		}
	}
	/**
	 * @return worst DST-test from several 
	*/
	private ExamDST WorstRes(List<ExamDST> lst) {
		int tmp = 0;
		int max = Integer.MIN_VALUE;
		ExamDST minEl = null;
		for (ExamDST el:lst){
			tmp = 0;
			tmp = el.getNumResistant();
			/*for (ExamDSTResult exr: el.getResults()){
				tmp += (exr.getResult().ordinal()==0 ? 4 : exr.getResult().ordinal());
			}*/
			if (tmp > max) {
				max = tmp; 
				minEl=el;
				}
		}
		return minEl;
	}
	
	private void calcPercentage(String colid, int rowid) {
		float total=0;
		switch (rowid) {
		case 2: case 4: case 5:{
			total = table1.getCellAsFloat(colid, "row"+(rowid-1));
			break;}
		case 1: case 3: {
			if (colid=="newcases") total = numcases[0];	
			if (colid=="relapses") total = numcases[1];
			if (colid=="others") total = numcases[2];
			break;}
		default:{
			total = table1.getCellAsFloat(colid, "row5");
			break;}
		}
		if (total == 0)
			return;
		
		float num = table1.getCellAsFloat(colid, "row"+rowid);
		
		table1.setValue(colid + "_perc", "row"+rowid, num * 100F / total);
	}
	
	
	/**
	 * Add a value to a specific cell in the table
	 * @param dtPType
	 * @param rowId
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
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.indicators.core.CaseHQLBase#getHQLJoin()
	 */
	protected String getHQLJoin() {
		return null;
	}
	
	/**
	 * Return table 1 report, from the original table object from {@link IndicatorTable}
	 * @return
	 */
	public IndicatorTable getTable1() {
		return getTable();
	}
}