package org.msh.tb.az;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.tb.az.entities.TbCaseAZ;
import org.msh.tb.entities.ExamCulture;
import org.msh.tb.entities.ExamDST;
import org.msh.tb.entities.ExamDSTResult;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.HIVResult;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.LocalityType;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.IndicatorVerify;
import org.msh.tb.indicators.core.IndicatorTable;

@Name("report08az")
public class Report08AZ extends IndicatorVerify {
	private static final long serialVersionUID = -5703455596815108661L;

	private List<String> ageRange;
	private static final int[] ar = {0,4,13,14,17,24,29,34,39,44,49,54,59,64};

	private IndicatorTable table1000;
	private IndicatorTable table2100;
	private IndicatorTable table2110;
	private IndicatorTable table2120;
	private IndicatorTable table4000;
	private int[] mas3000;
	private int[] mas3001;


	private List<TbCaseAZ> lst;

	/*
	 * Override abstract method createIndicators()
	 * @see org.msh.tb.indicators.core.Indicator#createIndicators()
	 */
	@Override
	protected void createIndicators() {
		setGroupFields(null);
		setConsolidated(false);
		initTable1000();
		initTable2100();
		initTable2110();
		initTable2120();
		initTable4000();

		generateTables();
		sortAllLists();
	}

	/**
	 * Create columns and rows for table2120 
	 * @see org.msh.tb.indicators.core.Indicator#createIndicators()
	 */
	private void initTable2120() {
		IndicatorTable table = getTable2120();
		for (int i = 1; i < 7; i++) {
			table.addRow(Integer.toString(i), "row"+i);
		}

		for (int i = 1; i < 9; i++) {
			table.addColumn(Integer.toString(i), "col"+i);
		}
	}

	/**
	 * Create columns and rows for table2110 
	 * @see org.msh.tb.indicators.core.Indicator#createIndicators()
	 */
	private void initTable2110() {
		IndicatorTable table = getTable2110();
		for (int i = 1; i < 9; i++) {
			table.addRow(Integer.toString(i), "row"+i);
		}

		for (int i = 1; i < 7; i++) {
			table.addColumn(Integer.toString(i), "col"+i);
		}
	}

	/**
	 * Create columns and rows for table4000 
	 * @see org.msh.tb.indicators.core.Indicator#createIndicators()
	 */
	private void initTable4000() {
		IndicatorTable table = getTable4000();
		for (int i = 1; i < 7; i++) {
			table.addRow(Integer.toString(i), "row"+i);
		}

		for (int i = 1; i < 6; i++) {
			table.addColumn(Integer.toString(i), "col"+i);
		}
	}

	/**
	 * Create columns and rows for table2100 
	 * @see org.msh.tb.indicators.core.Indicator#createIndicators()
	 */
	private void initTable2100() {
		IndicatorTable table = getTable2100();
		for (int i = 1; i < 30; i++) {
			table.addRow(Integer.toString(i), "row"+i);
		}

		for (int i = 1; i < 9; i++) {
			table.addColumn(Integer.toString(i), "col"+i);
		}
	}


	/**
	 * Generate values for all tables
	 */
	@Override
	protected void generateTables() {
		IndicatorTable table = getTable1000();
		setCounting(true);
		int count = ((Long)createQuery().getSingleResult()).intValue();
		if (count<=4000){
			initVerifList("verify.tb08.error",4,4,1);
			setCounting(false);
			setOverflow(false);
			lst = createQuery().getResultList();
			Iterator<TbCaseAZ> it = lst.iterator();
			int rowid;
			int colid;
			mas3000 = getMas3000();
			mas3001 = getMas3001();
			int ind=0;
			while(it.hasNext()){
				TbCaseAZ tc = it.next();
				ind++;
				System.out.println(ind);
				if (ind==419)
					System.out.println("s");
				rowid = tc.getPatient().getGender().ordinal();
				if (tc.getPatientType().equals(PatientType.NEW)){
					colid = idAgeRange(tc.getAge());
					table.addIdValue("col1", "row"+(1+rowid), 1F);
					table.addIdValue("col"+colid, "row"+(1+rowid), 1F);
					if (isRural(tc)){
						table.addIdValue("col17","row"+(1+rowid) , 1F);
						table.addIdValue("col17","row"+(73+rowid) , 1F);
						table.addIdValue("col1", "row"+(73+rowid), 1F);
						table.addIdValue("col"+colid, "row"+(73+rowid), 1F);
					}
					addToTable2100(1,tc,false);

					if (tc.getInfectionSite().equals(InfectionSite.PULMONARY) || tc.getInfectionSite().equals(InfectionSite.BOTH)){
						table.addIdValue("col1", "row"+(3+rowid), 1F);
						table.addIdValue("col"+colid, "row"+(3+rowid), 1F);
						if (isRural(tc))table.addIdValue("col17","row"+(3+rowid) , 1F);
						addToTable2100(2,tc,false);

						if (indCarvity(tc)>-1)
						{
							table.addIdValue("col1", "row"+(23+rowid), 1F);
							table.addIdValue("col"+colid, "row"+(23+rowid), 1F);
							if (isRural(tc))table.addIdValue("col17","row"+(23+rowid) , 1F);
							addToTable2100(12,tc,false);
						}
						if (tc.getPulmonaryType() == null)
							addToVerList(tc,1,0);
						else
							if (tc.getPulmonaryType().getDisplayOrder()==null)
								addToVerList(tc,1,0);
							else{
								table.addIdValue("col1", "row"+(3+rowid+tc.getPulmonaryType().getDisplayOrder()*2), 1F);
								table.addIdValue("col"+colid, "row"+(3+rowid+tc.getPulmonaryType().getDisplayOrder()*2), 1F);
								if (isRural(tc))table.addIdValue("col17","row"+(3+rowid+tc.getPulmonaryType().getDisplayOrder()*2) , 1F);
								addToTable2100(2+tc.getPulmonaryType().getDisplayOrder(),tc,false);

								if (bkplus(tc)){
									table.addIdValue("col1", "row"+(25+rowid), 1F);
									table.addIdValue("col"+colid, "row"+(25+rowid), 1F);
									if (isRural(tc))table.addIdValue("col17","row"+(25+rowid), 1F);
									table.addIdValue("col1", "row"+(25+rowid+tc.getPulmonaryType().getDisplayOrder()*2), 1F);
									table.addIdValue("col"+colid, "row"+(25+rowid+tc.getPulmonaryType().getDisplayOrder()*2), 1F);
									if (isRural(tc))table.addIdValue("col17","row"+(25+rowid+tc.getPulmonaryType().getDisplayOrder()*2),1F);
									addToTable2100(13,tc,false);
									if (indCarvity(tc)>-1)
									{
										table.addIdValue("col1", "row"+(45+rowid), 1F);
										table.addIdValue("col"+colid, "row"+(45+rowid), 1F);
										if (isRural(tc))table.addIdValue("col17","row"+(45+rowid), 1F);
									}
								}
								if (tc.getInfectionSite().equals(InfectionSite.BOTH) && tc.getExtrapulmonaryType() == null)
									addToVerList(tc,2,3);
							}
					}
					else
					{
						table.addIdValue("col1", "row"+(47+rowid), 1F);
						table.addIdValue("col"+colid, "row"+(47+rowid), 1F);
						if (isRural(tc))table.addIdValue("col17","row"+(47+rowid), 1F);
						addToTable2100(14,tc,false);
						if (tc.getExtrapulmonaryType() != null){
							table.addIdValue("col1", "row"+(47+rowid+tc.getExtrapulmonaryType().getDisplayOrder()*2), 1F);
							table.addIdValue("col"+colid, "row"+(47+rowid+tc.getExtrapulmonaryType().getDisplayOrder()*2), 1F);
							if (isRural(tc))table.addIdValue("col17","row"+(47+rowid+tc.getExtrapulmonaryType().getDisplayOrder()*2), 1F);
							addToTable2100(14+tc.getExtrapulmonaryType().getDisplayOrder(),tc,false);
						}
						else
							addToVerList(tc,1,1);
					}
					if (tc.getAge()>=14 && tc.getAge()<=29)
						addToTable2100(27,tc,false);
				}
				else
				{
					addToTable2100(1,tc,true);

					if (tc.getInfectionSite().equals(InfectionSite.PULMONARY) || tc.getInfectionSite().equals(InfectionSite.BOTH)){
						addToTable2100(2,tc,true);
						addToTable2100(2+tc.getPulmonaryType().getDisplayOrder(),tc,true);
						if (indCarvity(tc)>-1)
							addToTable2100(12,tc,true);
						if (bkplus(tc))
							addToTable2100(13,tc,true);
						if (tc.getInfectionSite().equals(InfectionSite.BOTH) && tc.getExtrapulmonaryType() == null)
							addToVerList(tc,2,3);
					}
					else
					{
						addToTable2100(14,tc,true);
						addToTable2100(14+tc.getExtrapulmonaryType().getDisplayOrder(),tc,true);
					}
					if (tc.getAge()>=14 && tc.getAge()<=29)
						addToTable2100(27,tc,true);
				}
				addToRepList(tc);
				//table 2120
				addToTable2120(1+rowid, tc);
				if (posHIVResult(tc))
					addToTable2120(3+rowid, tc);
				if (hasART_HIV(tc))
					addToTable2120(5+rowid, tc);

				//table2110
				int resH = exDST("H",tc);
				int resR = exDST("R",tc);
				int resE = exDST("E",tc);
				int resS = exDST("S",tc);
				if (resH == 2 && resR == 2 && resE == 2 && resS == 2){
					addToTable2110(2,tc);
					//table 3000 begin
					if (tc.getPatientType().equals(PatientType.NEW)){
						if (rightMcTest(tc)!=null){
							if (rightMcTest(tc).getResult().isPositive())
							{
								mas3000[0]++;
								if (tc.getState().equals(CaseState.CURED)) mas3000[1]++;
								else if (tc.getState().equals(CaseState.TREATMENT_COMPLETED)) mas3000[2]++;
								else if (tc.getState().equals(CaseState.DIED) || tc.getState().equals(CaseState.DIED_NOTTB)) mas3000[3]++;
								else if (tc.getState().equals(CaseState.FAILED)) mas3000[4]++;
								else if (tc.getState().equals(CaseState.DEFAULTED)) mas3000[5]++;
								else if (tc.getState().equals(CaseState.TRANSFERRED_OUT)) mas3000[6]++;	
							}
						}
						else
							addToVerList(tc,1,3);
					}
					//table 3000 end
					else {
						//table 3001 begin
						mas3001[0]++;
						if (tc.getState().equals(CaseState.CURED)) mas3001[1]++;
						else if (tc.getState().equals(CaseState.TREATMENT_COMPLETED)) mas3001[2]++;
						else if (tc.getState().equals(CaseState.DIED) || tc.getState().equals(CaseState.DIED_NOTTB)) mas3001[3]++;
						else if (tc.getState().equals(CaseState.FAILED)) mas3001[4]++;
						else if (tc.getState().equals(CaseState.DEFAULTED)) mas3001[5]++;
						else if (tc.getState().equals(CaseState.TRANSFERRED_OUT)) mas3001[6]++;
						//table 3001 end
					}
				}
				
				if (resH != 1 && resR != 1 && resE != 1 && resS == 1) 
					addToTable2110(3,tc);
				if ((resH == 1 && resR != 1 && resE != 1 && resS != 1) ||
						(resH == 1 && resR != 1 && resE != 1 && resS == 1) ||	
						(resH == 1 && resR != 1 && resE == 1 && resS != 1) ||
						(resH == 1 && resR != 1 && resE == 1 && resS == 1))
					addToTable2110(4,tc);
				if ((resH != 1 && resR == 1 && resE != 1 && resS != 1) ||
						(resH != 1 && resR == 1 && resE != 1 && resS == 1) ||	
						(resH != 1 && resR == 1 && resE == 1 && resS != 1) ||
						(resH != 1 && resR == 1 && resE == 1 && resS == 1))
					addToTable2110(5,tc);
				if ((resH == 1 && resR == 1 && resE != 1 && resS != 1) ||
						(resH == 1 && resR == 1 && resE != 1 && resS == 1) ||	
						(resH == 1 && resR == 1 && resE == 1 && resS != 1) ||
						(resH == 1 && resR == 1 && resE == 1 && resS == 1))
				{
					addToTable2110(6,tc);
					if ((exDST("Km",tc) == 1 || exDST("Am",tc) == 1 || exDST("Cm",tc) == 1) && exDST("Ofx",tc) == 1) 
						addToTable2110(7,tc);
				}
				if (resH == 0 && resR == 0 && resE == 0 && resS == 0){
					addToTable2110(8,tc);
					addToVerList(tc,2,1);
				}
			}
		}
		else 
			setOverflow(true);
	}

	/**
	 * Add value to table 2110 in necessary column
	 */
	private void addToTable2110(int rowInd,TbCaseAZ tc){
		int colInd=0;
		if (tc.getPatientType().equals(PatientType.NEW)){
			if (tc.isPulmonary()) colInd = 1;
			else colInd = 2;
		}
		else {
			if (tc.getPatientType().equals(PatientType.RELAPSE)) colInd = 3;
			else if (tc.getPatientType().equals(PatientType.FAILURE_FT) || tc.getPatientType().equals(PatientType.FAILURE_RT)) colInd = 4;
			else if (tc.getPatientType().equals(PatientType.AFTER_DEFAULT)) colInd = 5;
			else if (tc.getPatientType().equals(PatientType.OTHER)) colInd = 6;
			else 
				System.out.println(tc.getPatientType().toString());
		}
		if (colInd!=0){
			table2110.addIdValue("col"+colInd, "row1", 1F);
			table2110.addIdValue("col"+colInd, "row"+rowInd, 1F);
		}
	}
	/**
	 * Return the result of DST-examination for necessary substance 
	 */
	private int exDST(String el,TbCase tc){
		ExamDST ex = rightDSTTest(tc);
		int res = 0;
		if (ex != null){
			for (ExamDSTResult exr: ex.getResults()) 
				if (exr.getSubstance().getAbbrevName().getName1().equals(el)) res = exr.getResult().ordinal();	
		}
		return res;
	}

	/**
	 * Add value to table 2100 in necessary column
	 */
	private void addToTable2100(int rowInd,TbCaseAZ tc, boolean extra){
		int ifRural = 0;
		if (isRural(tc)) 
			ifRural = 2;
		if (!tc.isReferToOtherTBUnit())
			if (tc.getAge()<=17){
				if (!extra) table2100.addIdValue("col"+(1+ifRural), "row"+rowInd, 1F);
				table2100.addIdValue("col"+(5+ifRural), "row"+rowInd, 1F);
			}
			else{
				if (!extra) table2100.addIdValue("col"+(2+ifRural), "row"+rowInd, 1F);
				table2100.addIdValue("col"+(6+ifRural), "row"+rowInd, 1F);
			}
	}

	/**
	 * Return true, if hiv-result of case is positive
	 */
	private boolean posHIVResult(TbCaseAZ tc){
		if (tc.getResHIV().size()==0)
			addToVerList(tc,1,2);
		else
			for (int i = 0; i < tc.getResHIV().size(); i++) {
				if (tc.getResHIV().get(i).getResult().equals(HIVResult.POSITIVE))
					return true;
			}
		return false;
	}

	/**
	 * Return true, if patient was used the art treatment regimen
	 */
	private boolean hasART_HIV(TbCaseAZ tc){
		for (int i = 0; i < tc.getResHIV().size(); i++) {
			if (tc.getResHIV().get(i).getStartedARTdate()!=null)
				return true;
		}
		return false;
	}

	/**
	 * Add value to table 2120 in necessary column
	 */
	private void addToTable2120(int rowInd,TbCaseAZ tc){
		int ifRural = 0;
		if (isRural(tc)) 
			ifRural = 2;
		if (tc.getResHIV().size()!=0){
			if (tc.getAge()<=17)
			{
				if (tc.getPatientType().equals(PatientType.NEW))	
					table2120.addIdValue("col"+(1+ifRural), "row"+rowInd, 1F);
				else 
					if (tc.getPatientType().equals(PatientType.NEW) ||
							tc.getPatientType().equals(PatientType.AFTER_DEFAULT) ||
							tc.getPatientType().equals(PatientType.RELAPSE) ||
							tc.getPatientType().equals(PatientType.FAILURE_FT) ||
							tc.getPatientType().equals(PatientType.FAILURE_RT))
						table2120.addIdValue("col"+(5+ifRural), "row"+rowInd, 1F);
			}
			else 
			{
				if (tc.getPatientType().equals(PatientType.NEW))	
					table2120.addIdValue("col"+(2+ifRural), "row"+rowInd, 1F);
				else 
					if (tc.getPatientType().equals(PatientType.NEW) ||
							tc.getPatientType().equals(PatientType.AFTER_DEFAULT) ||
							tc.getPatientType().equals(PatientType.RELAPSE) ||
							tc.getPatientType().equals(PatientType.FAILURE_FT) ||
							tc.getPatientType().equals(PatientType.FAILURE_RT))
						table2120.addIdValue("col"+(6+ifRural), "row"+rowInd, 1F);
			}
		}
		else
			addToVerList(tc,1,2);
	}
	/**
	 * @return the Carvity index from SeverityMarks list of case
	 */
	private int indCarvity(TbCaseAZ tc){
		for (int i = 0; i < tc.getSeverityMarks().size(); i++) {
			if (tc.getSeverityMarks().get(i).getSeverityMark().getId().intValue() == 939370)
				return i;
		}
		return -1;
	}

	/**
	 * @return true, if patient has positive bacterioscopy
	 */
	private boolean bkplus(TbCase tc){
		boolean res = false;
		ExamMicroscopy mc = rightMcTest(tc);
		if (mc!=null)
			if (mc.getResult().isPositive()) 
				res = true;
		ExamCulture ec = rightCulTest(tc);
		if (ec!=null)
			if (ec.getResult().isPositive()) 
				res = true;
		if (mc==null && ec==null) 
			addToVerList(tc,2,2);

		return res;

	}


	/**
	 * @return ind column of necessary age range in table 1000  
	 */
	private int idAgeRange(int age){
		int res = 0;
		for (int i = 1; i < ar.length; i++) {
			if ((age <= ar[i]) && (age >= ar[i-1]+1)) 
				res = i;
		}
		if (age >= ar[ar.length-1]+1)
			res = ar.length;
		return res+2;
	}

	/**
	 * Create columns and rows for table1000 
	 * @see org.msh.tb.indicators.core.Indicator#createIndicators()
	 */
	private void initTable1000() {
		IndicatorTable table = getTable1000();
		for (int i = 1; i < 75; i++) {
			table.addRow(Integer.toString(i), "row"+i);
		}
		for (int i = 1; i < 18; i++) {
			table.addColumn(Integer.toString(i), "col"+i);
		}
	}

	/**
	 * Create all tables of the report and generate indicators
	 */
	@Override
	protected void createTable() {
		table1000 = new IndicatorTable();
		table2100 = new IndicatorTable();
		table2110 = new IndicatorTable();
		table2120 = new IndicatorTable();
		table4000 = new IndicatorTable();
	}

	public List<String> getAgeRange() {
		if (ageRange == null)
			createAgeRange();
		return ageRange;
	}

	public void createAgeRange(){
		ageRange = new ArrayList<String>();
		String elem;
		for (int i = 0; i < ar.length; i++) {
			if (i == 0) elem = "< 1";
			else {
				elem = Integer.toString(ar[i-1]+1);
				if (ar[i-1]+1 != ar[i])	elem += "-"+Integer.toString(ar[i]);
			}
			ageRange.add(elem);
		}
		elem = ">= "+(ar[ar.length-1]+1);
		ageRange.add(elem);
	}

	public IndicatorTable getTable1000() {
		if (table1000 == null)
			createTable();
		return table1000;
	}

	public IndicatorTable getTable2100() {
		if (table2100 == null)
			createTable();
		return table2100;
	}

	public IndicatorTable getTable2110() {
		if (table2110 == null)
			createTable();
		return table2110;
	}

	public IndicatorTable getTable2120() {
		if (table2120 == null)
			createTable();
		return table2120;
	}

	public IndicatorTable getTable4000() {
		if (table4000 == null)
			createTable();
		return table4000;
	}

	public String getIterateMessage(String key, int i){
		return getMessage(key+i);
	}

	public int[] getMas3000() {
		if (mas3000==null)
			mas3000 = new int[7];
		return mas3000;
	}

	public int[] getMas3001() {
		if (mas3001==null)
			mas3001 = new int[7];
		return mas3001;
	}

	private boolean isRural(TbCase c){
		//	if 0 -urban, other - rural
		boolean result=true;
		if (c.getNotifAddress().getLocalityType()!=null){
			if (c.getNotifAddress().getLocalityType()==LocalityType.URBAN)
				result=false;
		}
		return result;

	}

	@Override
	protected void addToAllowing(TbCase tc) {
		if (tc.getNotifAddress().getLocalityType()==null)
			addToVerList(tc,2,0);
	}
}
