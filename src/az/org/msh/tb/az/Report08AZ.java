package org.msh.tb.az;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.tb.az.entities.TbCaseAZ;
import org.msh.tb.entities.ExamCulture;
import org.msh.tb.entities.ExamDST;
import org.msh.tb.entities.ExamDSTResult;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.HIVResult;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.entities.enums.ValidationState;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.tb.indicators.core.IndicatorFilters;
import org.msh.tb.indicators.core.IndicatorTable;

@Name("report08az")
public class Report08AZ extends Indicator2D {
	private static final long serialVersionUID = -5703455596815108661L;

	private List<String> ageRange;
	private List<String> nameRows;
	private List<String> nameMKB;
	private static final int[] ar = {0,4,13,14,17,24,29,34,39,44,49,54,59,64};

	private IndicatorTable table1000;
	private IndicatorTable table2100;
	private IndicatorTable table2110;
	private IndicatorTable table2120;
	private IndicatorTable table4000;

	private List<TbCaseAZ> lst;

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

	}

	private void initTable2120() {
		IndicatorTable table = getTable2120();
		for (int i = 1; i < 7; i++) {
			table.addRow(Integer.toString(i), "row"+i);
		}

		for (int i = 1; i < 9; i++) {
			table.addColumn(Integer.toString(i), "col"+i);
		}
	}
	
	private void initTable2110() {
		IndicatorTable table = getTable2110();
		for (int i = 1; i < 9; i++) {
			table.addRow(Integer.toString(i), "row"+i);
		}

		for (int i = 1; i < 7; i++) {
			table.addColumn(Integer.toString(i), "col"+i);
		}
	}
	
	private void initTable4000() {
		IndicatorTable table = getTable4000();
		for (int i = 1; i < 7; i++) {
			table.addRow(Integer.toString(i), "row"+i);
		}

		for (int i = 1; i < 6; i++) {
			table.addColumn(Integer.toString(i), "col"+i);
		}
	}
	
	private void initTable2100() {
		IndicatorTable table = getTable2100();
		for (int i = 1; i < 30; i++) {
			table.addRow(Integer.toString(i), "row"+i);
		}

		for (int i = 1; i < 9; i++) {
			table.addColumn(Integer.toString(i), "col"+i);
		}
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

	/* (non-Javadoc)
	 * @see org.msh.tb.indicators.core.CaseHQLBase#getHQLJoin()
	 */
	protected String getHQLJoin() {
		return null;
	}

	protected String getHQLValidationState() {
		return "c.validationState = "+ValidationState.VALIDATED.ordinal();
	}

	private void generateTables() {
		IndicatorTable table = getTable1000();
		setHQLSelect("select c");
		lst = createQuery().getResultList();
		Iterator<TbCaseAZ> it = lst.iterator();
		int rowid;
		int colid;
		while(it.hasNext()){
			TbCaseAZ tc = it.next();
			rowid = tc.getPatient().getGender().ordinal();
			if (tc.getPatientType().equals(PatientType.NEW)){
				colid = idAgeRange(tc.getAge());

				table.addIdValue("col1", "row"+(1+rowid), 1F);
				table.addIdValue("col"+colid, "row"+(1+rowid), 1F);
				addToTable2100(1,tc,false);

				if (tc.getInfectionSite().equals(InfectionSite.PULMONARY)){
					table.addIdValue("col1", "row"+(3+rowid), 1F);
					table.addIdValue("col"+colid, "row"+(3+rowid), 1F);
					addToTable2100(2,tc,false);

					if (indCarvity(tc)>-1)
					{
						table.addIdValue("col1", "row"+(23+rowid), 1F);
						table.addIdValue("col"+colid, "row"+(23+rowid), 1F);
						addToTable2100(12,tc,false);
					}
					if (tc.getPulmonaryType() != null){
						table.addIdValue("col1", "row"+(3+rowid+tc.getPulmonaryType().getDisplayOrder()*2), 1F);
						table.addIdValue("col"+colid, "row"+(3+rowid+tc.getPulmonaryType().getDisplayOrder()*2), 1F);
						addToTable2100(2+tc.getPulmonaryType().getDisplayOrder(),tc,false);

						if (bkplus(tc)){
							table.addIdValue("col1", "row"+(25+rowid), 1F);
							table.addIdValue("col"+colid, "row"+(25+rowid), 1F);
							table.addIdValue("col1", "row"+(25+rowid+tc.getPulmonaryType().getDisplayOrder()*2), 1F);
							table.addIdValue("col"+colid, "row"+(25+rowid+tc.getPulmonaryType().getDisplayOrder()*2), 1F);
							addToTable2100(13,tc,false);
							if (indCarvity(tc)>-1)
							{
								table.addIdValue("col1", "row"+(45+rowid), 1F);
								table.addIdValue("col"+colid, "row"+(45+rowid), 1F);
							}
						}	
					}
				}
				else
				{
					table.addIdValue("col1", "row"+(47+rowid), 1F);
					table.addIdValue("col"+colid, "row"+(47+rowid), 1F);
					addToTable2100(14,tc,false);
					if (tc.getExtrapulmonaryType() != null){
						table.addIdValue("col1", "row"+(47+rowid+tc.getExtrapulmonaryType().getDisplayOrder()*2), 1F);
						table.addIdValue("col"+colid, "row"+(47+rowid+tc.getExtrapulmonaryType().getDisplayOrder()*2), 1F);
						addToTable2100(14+tc.getExtrapulmonaryType().getDisplayOrder(),tc,false);
					}
				}
				if (tc.getAge()>=14 && tc.getAge()<=29)
					addToTable2100(27,tc,false);
			}
			else
			{
				addToTable2100(1,tc,true);

				if (tc.getInfectionSite().equals(InfectionSite.PULMONARY)){
					addToTable2100(2,tc,true);
					addToTable2100(2+tc.getPulmonaryType().getDisplayOrder(),tc,true);
					if (indCarvity(tc)>-1)
						addToTable2100(12,tc,true);
					if (bkplus(tc))
						addToTable2100(13,tc,true);
				}
				else
				{
					addToTable2100(14,tc,true);
					addToTable2100(14+tc.getExtrapulmonaryType().getDisplayOrder(),tc,true);
				}
				if (tc.getAge()>=14 && tc.getAge()<=29)
					addToTable2100(27,tc,true);
			}
			addToTable2120(1+rowid, tc);
			if (posHIVResult(tc))
				addToTable2120(3+rowid, tc);
			if (hasART_HIV(tc))
				addToTable2120(5+rowid, tc);
			
			if (exDST("H",tc) == 2 && exDST("R",tc) == 2 && exDST("E",tc) == 2 && exDST("S",tc) == 2) 
				addToTable2110(2,tc);
			if (exDST("H",tc) != 1 && exDST("R",tc) != 1 && exDST("E",tc) != 1 && exDST("S",tc) == 1) 
				addToTable2110(3,tc);
			if ((exDST("H",tc) == 1 && exDST("R",tc) != 1 && exDST("E",tc) != 1 && exDST("S",tc) != 1) ||
				(exDST("H",tc) == 1 && exDST("R",tc) != 1 && exDST("E",tc) != 1 && exDST("S",tc) == 1) ||	
				(exDST("H",tc) == 1 && exDST("R",tc) != 1 && exDST("E",tc) == 1 && exDST("S",tc) != 1) ||
				(exDST("H",tc) == 1 && exDST("R",tc) != 1 && exDST("E",tc) == 1 && exDST("S",tc) == 1))
				addToTable2110(4,tc);
			if ((exDST("H",tc) != 1 && exDST("R",tc) == 1 && exDST("E",tc) != 1 && exDST("S",tc) != 1) ||
				(exDST("H",tc) != 1 && exDST("R",tc) == 1 && exDST("E",tc) != 1 && exDST("S",tc) == 1) ||	
				(exDST("H",tc) != 1 && exDST("R",tc) == 1 && exDST("E",tc) == 1 && exDST("S",tc) != 1) ||
				(exDST("H",tc) != 1 && exDST("R",tc) == 1 && exDST("E",tc) == 1 && exDST("S",tc) == 1))
				addToTable2110(5,tc);
			if ((exDST("H",tc) == 1 && exDST("R",tc) == 1 && exDST("E",tc) != 1 && exDST("S",tc) != 1) ||
				(exDST("H",tc) == 1 && exDST("R",tc) == 1 && exDST("E",tc) != 1 && exDST("S",tc) == 1) ||	
				(exDST("H",tc) == 1 && exDST("R",tc) == 1 && exDST("E",tc) == 1 && exDST("S",tc) != 1) ||
				(exDST("H",tc) == 1 && exDST("R",tc) == 1 && exDST("E",tc) == 1 && exDST("S",tc) == 1))
				{
					addToTable2110(6,tc);
					addToTable2110(7,tc);
					if (exDST("Km",tc) == 1 || exDST("Am",tc) == 1 || exDST("Cm",tc) == 1 || exDST("Ofx",tc) == 1) 
						addToTable2110(7,tc);
				}
			if (exDST("H",tc) == -1 && exDST("R",tc) == -1 && exDST("E",tc) == -1 && exDST("S",tc) == -1)
				addToTable2110(8,tc);
		}
	}

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
	
	private int exDST(String el,TbCase tc){
		ExamDST ex = rightDSTTest(tc);
		int res = -1;
		if (ex != null){
			for (ExamDSTResult exr: ex.getResults()) 
				if (exr.getSubstance().getAbbrevName().getName1().equals(el)) res = exr.getResult().ordinal();	
		}
		return res;
	}
	
	private void addToTable2100(int rowInd,TbCaseAZ tc, boolean extra){
		if (!tc.isReferToOtherTBUnit())
			if (tc.getAge()<=17)
			{
				if (!extra) table2100.addIdValue("col1", "row"+rowInd, 1F);
				table2100.addIdValue("col5", "row"+rowInd, 1F);
			}
			else
			{
				if (!extra) table2100.addIdValue("col2", "row"+rowInd, 1F);
				table2100.addIdValue("col6", "row"+rowInd, 1F);
			}
	}

	private boolean posHIVResult(TbCaseAZ tc){
		for (int i = 0; i < tc.getResHIV().size(); i++) {
			if (tc.getResHIV().get(i).getResult().equals(HIVResult.POSITIVE))
				return true;
		}
		return false;
	}
	private boolean hasART_HIV(TbCaseAZ tc){
		for (int i = 0; i < tc.getResHIV().size(); i++) {
			if (tc.getResHIV().get(i).getStartedARTdate()!=null)
				return true;
		}
		return false;
	}
	
	private void addToTable2120(int rowInd,TbCaseAZ tc){
		if (tc.getResHIV().size()!=0){
			if (tc.getAge()<=17)
			{
				if (tc.getPatientType().equals(PatientType.NEW))	
					table2120.addIdValue("col1", "row"+rowInd, 1F);
				else 
					if (tc.getPatientType().equals(PatientType.NEW) ||
							tc.getPatientType().equals(PatientType.AFTER_DEFAULT) ||
							tc.getPatientType().equals(PatientType.RELAPSE) ||
							tc.getPatientType().equals(PatientType.FAILURE_FT) ||
							tc.getPatientType().equals(PatientType.FAILURE_RT))
						table2120.addIdValue("col5", "row"+rowInd, 1F);
			}
			else 
			{
				if (tc.getPatientType().equals(PatientType.NEW))	
					table2120.addIdValue("col2", "row"+rowInd, 1F);
				else 
					if (tc.getPatientType().equals(PatientType.NEW) ||
							tc.getPatientType().equals(PatientType.AFTER_DEFAULT) ||
							tc.getPatientType().equals(PatientType.RELAPSE) ||
							tc.getPatientType().equals(PatientType.FAILURE_FT) ||
							tc.getPatientType().equals(PatientType.FAILURE_RT))
						table2120.addIdValue("col6", "row"+rowInd, 1F);
			}
		}
	}
	
	private int indCarvity(TbCaseAZ tc){
		for (int i = 0; i < tc.getSeverityMarks().size(); i++) {
			if (tc.getSeverityMarks().get(i).getSeverityMark().getId().intValue() == 939370)
				return i;
		}
		return -1;
	}

	private boolean bkplus(TbCase tc){
		boolean res = false;
		if (rightMcTest(tc)!=null)
			if (rightMcTest(tc).getResult().isPositive()) 
				res = true;
		if (rightCulTest(tc)!=null)
			if (rightCulTest(tc).getResult().isPositive()) 
				res = true;
		return res;

	}

	/**
	 * @return test, which is up to quality, namely first month of treatment and worst test from several 
	 */

	private ExamMicroscopy rightMcTest(TbCase tc){
		Calendar dateTest = Calendar.getInstance();
		Calendar dateIniTreat = Calendar.getInstance();
		int res=0;
		ArrayList<ExamMicroscopy> lst = new ArrayList<ExamMicroscopy>();
		for (ExamMicroscopy ex: tc.getExamsMicroscopy()){
			dateTest.setTime(ex.getDateCollected());
			if (tc.getTreatmentPeriod()!=null)
				dateIniTreat.setTime(tc.getTreatmentPeriod().getIniDate());
			else
				dateIniTreat.setTime(tc.getDiagnosisDate());
			Calendar regDt = Calendar.getInstance();
			regDt.setTime(tc.getRegistrationDate());
			long testperiod = dateTest.getTimeInMillis()-regDt.getTimeInMillis();
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
			return WorstMcRes(lst);
		}
	}

	private ExamCulture rightCulTest(TbCase tc){
		Calendar dateTest = Calendar.getInstance();
		Calendar dateIniTreat = Calendar.getInstance();
		int res=0;
		ArrayList<ExamCulture> lst = new ArrayList<ExamCulture>();
		for (ExamCulture ex: tc.getExamsCulture()){
			dateTest.setTime(ex.getDateCollected());
			if (tc.getTreatmentPeriod()!=null)
				dateIniTreat.setTime(tc.getTreatmentPeriod().getIniDate());
			else
				dateIniTreat.setTime(tc.getDiagnosisDate());
			Calendar regDt = Calendar.getInstance();
			regDt.setTime(tc.getRegistrationDate());
			long testperiod = dateTest.getTimeInMillis()-regDt.getTimeInMillis();
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
			return WorstCulRes(lst);
		}
	}

	private ExamDST rightDSTTest(TbCase tc){
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
			Calendar regDt = Calendar.getInstance();
			regDt.setTime(tc.getRegistrationDate());
			long testperiod = dateTest.getTimeInMillis()-regDt.getTimeInMillis();
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
			return WorstDSTRes(lst);
		}
	}
	
	/**
	 * @return worst DST-test from several 
	 */
	private ExamMicroscopy WorstMcRes(List<ExamMicroscopy> lst) {
		int tmp = 0;
		int max = Integer.MIN_VALUE;
		ExamMicroscopy minEl = null;
		for (ExamMicroscopy el:lst){
			tmp = 0;
			tmp = el.getResult().ordinal();
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

	private ExamCulture WorstCulRes(List<ExamCulture> lst) {
		int tmp = 0;
		int max = Integer.MIN_VALUE;
		ExamCulture minEl = null;
		for (ExamCulture el:lst){
			tmp = 0;
			tmp = el.getResult().ordinal();
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

	private ExamDST WorstDSTRes(List<ExamDST> lst) {
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

	private void initTable1000() {


		nameRows = new ArrayList<String>();
		nameRows.add(getMessage("manag.form8.table1000.rowheader1"));
		nameRows.add(getMessage("manag.form8.table1000.rowheader2"));
		for (int i = 1; i < 10; i++) {
			nameRows.add(getMessage("manag.form8.pulm"+i));
		}
		nameRows.add(getMessage("manag.form8.table1000.rowheader3"));
		nameRows.add(getMessage("manag.form8.table1000.rowheader4"));
		for (int i = 1; i < 10; i++) {
			nameRows.add(getMessage("manag.form8.pulm"+i));
		}
		nameRows.add(getMessage("manag.form8.table1000.rowheader5"));
		nameRows.add(getMessage("manag.form8.table1000.rowheader6"));
		for (int i = 1; i < 13; i++) {
			nameRows.add(getMessage("manag.form8.extrapulm"+i));
		}
		nameRows.add(getMessage("manag.form8.table1000.rowheader7"));



		nameMKB = new ArrayList<String>();
		nameMKB.add(getMessage("manag.form8.mkb.head1"));
		nameMKB.add(getMessage("manag.form8.mkb.head2"));
		for (int i = 1; i < 10; i++) {
			nameMKB.add(getMessage("manag.form8.mkb.pulm"+i));
		}
		nameMKB.add("");
		nameMKB.add(getMessage("manag.form8.mkb.head3"));
		for (int i = 1; i < 10; i++) {
			nameMKB.add(getMessage("manag.form8.mkb.pulm.plus"+i));
		}
		nameMKB.add("");
		nameMKB.add(getMessage("manag.form8.mkb.head4"));
		for (int i = 1; i < 12; i++) {
			nameMKB.add(getMessage("manag.form8.mkb.extrapulm"+i));
		}
		nameMKB.add("");
		nameMKB.add("");


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
		super.createTable();
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

	public List<String> getNameRows() {
		return nameRows;
	}

	public List<String> getNameMKB() {
		return nameMKB;
	}

	public String getIterateMessage(String key, int i){
		return getMessage(key+i);
	}
}
