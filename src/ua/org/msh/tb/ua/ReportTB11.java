package org.msh.tb.ua;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.ExamDST;
import org.msh.tb.entities.ExamDSTResult;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.core.IndicatorTable;

@Name("reportTB11")
public class ReportTB11 extends IndicatorVerify {
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
		table1 = getTable1();
		initTable1();

		generateTables();
		sortAllLists();
		getIndicatorFilters().setInfectionSite(null);
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

	@Override
	protected void generateTables() {
		// calculate the number of cases
		getIndicatorFilters().setInfectionSite(InfectionSite.PULMONARY);
		setCounting(true);
		setCondition("c.state >= " + CaseState.ONTREATMENT.ordinal()+" and c.patientType = " + PatientType.NEW.ordinal());
		numcases[0] = ((Long)createQuery().getSingleResult()).floatValue();
		setCondition("c.state >= " + CaseState.ONTREATMENT.ordinal()+" and c.patientType = " + PatientType.RELAPSE.ordinal());
		numcases[1] = ((Long)createQuery().getSingleResult()).floatValue();
		setCondition("c.state >= " + CaseState.ONTREATMENT.ordinal()+" and c.patientType in (0,1,2,3,4,5)");
		numcases[2] = ((Long)createQuery().getSingleResult()).floatValue();
		numcases[2] -= numcases[0]+numcases[1];

		if (numcases[0]+numcases[1]+numcases[2] == 0)
			return;
		if (numcases[0]+numcases[1]+numcases[2]<=4000){
			initVerifList("verify.tb11.error",2,1,1);
			setCounting(false);
			setOverflow(false);

			List<TbCase> lst;

			/**
			 * Necessary order of substances 
			 */
			String [] sub = {"H","R","E","S"};

			setCondition("c.state >= " + CaseState.ONTREATMENT.ordinal()+" and c.patientType in (0,1,2,3,4,5)");

			lst = createQuery().getResultList();
			Iterator<TbCase> it = lst.iterator();

			while(it.hasNext()){
				TbCase tc = it.next();
				int res0=0;
				int res1=0;
				int res2=0;
				int res3=0;

				String colkey;
				switch (tc.getPatientType()) {
				case NEW: 
					colkey = "newcases";
					break;
				case RELAPSE: 
					colkey = "relapses";
					break;
				default: 
					colkey = "others";
				}

				if (MicroscopyIsNull(tc)){
					if (!getVerifyList().get(getMessage("verify.errorcat1")).get(0).getCaseList().contains(tc))
						getVerifyList().get(getMessage("verify.errorcat1")).get(0).getCaseList().add(tc);
				}
				else{
					table1.addIdValue(colkey, "row1", 1F);
					if (rightMcTest(tc).getResult().isPositive())
						table1.addIdValue(colkey, "row2", 1F);
				}
				if (CultureIsNull(tc)){
					if (!getVerifyList().get(getMessage("verify.errorcat1")).get(1).getCaseList().contains(tc))
						getVerifyList().get(getMessage("verify.errorcat1")).get(1).getCaseList().add(tc);
				}
				else{
					table1.addIdValue(colkey, "row3", 1F);
					if (rightCulTest(tc).getResult().isPositive())
						table1.addIdValue(colkey, "row4", 1F);
				}
				if (!MicroscopyIsNull(tc) || !CultureIsNull(tc)){
					addToAllowing(tc);
					ExamDST ex = rightDSTTest(tc);
					if (ex != null){
						for (int i = 9; i < 27; i++){
							switch (i) {
							case 9: case 14:case 15: case 17:{
								sub[0] = "H";
								sub[1] = "R"; 
								sub[2] = "E"; 
								sub[3] = "S";
								break;}

							case 10: {
								sub[0] = "R";
								sub[1] = "H"; 
								sub[2] = "E"; 
								sub[3] = "S"; 
								break;}
							case 11: {
								sub[0] = "E";
								sub[1] = "R"; 
								sub[2] = "H"; 
								sub[3] = "S";
								break;}
							case 12: {
								sub[0] = "S";
								sub[1] = "R"; 
								sub[2] = "E"; 
								sub[3] = "H";
								break;}
							case 16: {
								sub[0] = "H";
								sub[1] = "R"; 
								sub[2] = "S"; 
								sub[3] = "E";
								break;}
							case 21:{
								sub[0] = "H";
								sub[1] = "E"; 
								sub[2] = "S"; 
								sub[3] = "R";
								break;}
							case 25:{
								sub[0] = "R";
								sub[1] = "E"; 
								sub[2] = "S"; 
								sub[3] = "H";
								break;}

							case 19:{
								sub[0] = "H";
								sub[1] = "E"; 
								sub[2] = "R"; 
								sub[3] = "S";
								break;}
							case 20:{
								sub[0] = "H";
								sub[1] = "S"; 
								sub[2] = "R"; 
								sub[3] = "E";
								break;}
							case 23:{
								sub[0] = "R"; 
								sub[1] = "S";
								sub[2] = "H";
								sub[3] = "E"; 
								break;}
							case 24:{
								sub[0] = "R"; 
								sub[1] = "E";
								sub[2] = "H";
								sub[3] = "S"; 
								break;}
							case 26:{
								sub[0] = "E"; 
								sub[1] = "S";
								sub[2] = "H";
								sub[3] = "R"; 
								break;}
							default: 
								break;
							}
							
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
									table1.addIdValue(colkey, "row"+i, 1F); 	
								break;}
							//					3 substances must be resistant and 1 other must be not resistant 			
							case 9:case 10:case 11:case 12:{
								if (res0 == 1 && res1 !=1 && res2 !=1 && res3 !=1) 
									table1.addIdValue(colkey, "row"+i, 1F); 	
								break;}
							//					2 substances must be resistant and 2 other must be not resistant 
							case 14:case 19:case 20:case 23:case 24:case 26:{
								if (res0 == 1 && res1 ==1 && res2 !=1 && res3 !=1) 
									table1.addIdValue(colkey, "row"+i, 1F); 	
								break;}
							//					1 substances must be resistant and 3 other must be not resistant 			
							case 15:case 16:case 21:case 25:{
								if (res0 == 1 && res1 ==1 && res2 ==1 && res3 !=1) 
									table1.addIdValue(colkey, "row"+i, 1F); 	
								break;}
							//					4 substances must be resistant 			
							case 17:{
								if (res0 == 1 && res1 ==1 && res2 !=1 && res3 ==1) 
									table1.addIdValue(colkey, "row"+i, 1F); 	
								break;}
							}
						}
					}
				}
			}

			calcSummaryFields();
			for (int i = 1; i < 31; i++)
				for (String colid: cols) 
					calcPercentage(colid,i);	
		}
		else 
			setOverflow(true);
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


	/**
	 * Return table 1 report, from the original table object from {@link IndicatorTable}
	 * @return
	 */
	public IndicatorTable getTable1() {
		if (table1==null)
			table1 = new IndicatorTable();
		return table1;
	}

	private boolean everyDSTSubst(TbCase tc){
		ExamDST ex = rightDSTTest(tc);
		if (ex!=null)
		{
			boolean[] em = new boolean[4];
			for (ExamDSTResult exr: ex.getResults()) 
			{
				if (exr.getSubstance().getAbbrevName().getName1().equals("H")) em[0] = true;	
				if (exr.getSubstance().getAbbrevName().getName1().equals("R")) em[1] = true;	
				if (exr.getSubstance().getAbbrevName().getName1().equals("E")) em[2] = true;	
				if (exr.getSubstance().getAbbrevName().getName1().equals("S")) em[3] = true;	
			}
			for (boolean i:em)
				if (!i)
					return false;
			return true;
		}
		return false;
	}
	
	@Override
	protected void addToAllowing(TbCase tc) {
		Map<String, List<ErrItem>>  verifyList = getVerifyList();
		if (!everyDSTSubst(tc)){
			if (!verifyList.get(getMessage("verify.errorcat2")).get(0).getCaseList().contains(tc))
				verifyList.get(getMessage("verify.errorcat2")).get(0).getCaseList().add(tc);
		}
		if (!verifyList.get(getMessage("verify.errorcat2")).get(0).getCaseList().contains(tc))
			if (!verifyList.get(getMessage("verify.errorcat3")).get(0).getCaseList().contains(tc))
				verifyList.get(getMessage("verify.errorcat3")).get(0).getCaseList().add(tc);
		
	}

}