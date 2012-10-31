package org.msh.tb.ua;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.ExamCulture;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.IndicatorVerify;
import org.msh.tb.indicators.core.IndicatorFilters;
import org.msh.tb.indicators.core.IndicatorMicroscopyResult;
import org.msh.tb.indicators.core.IndicatorTable;
import org.msh.tb.indicators.core.IndicatorTable.TableColumn;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;

@Name("reportTB10")
public class ReportTB10 extends IndicatorVerify<TbCase> {
	private static final long serialVersionUID = -8510290531301762778L;

	private IndicatorTable table2000;
	private IndicatorTable table1000;
	private List<Integer> num = new ArrayList<Integer>();
	
	
	public List<Integer> getNum() {
		return num;
	}

	public void setNum(List<Integer> num) {
		this.num = num;
	}

	/**
	 * Total number of cases in the indicator 
	 */
	private float [] numcases = new float [3];
	

	@Override
	protected void createIndicators() {
		initTable1000();
		initTable2000();
		
		setConsolidated(true);
		generateTables();
		sortAllLists();
		getIndicatorFilters().setInfectionSite(null);
	}
	

	public void getTotal2000() {
		List<TableColumn> cols = table2000.getColumns();
		
		for (int i = 1; i < cols.size(); i++) {
			num.add(cols.get(i).getTotal());			
		}
	}

	/**
	 * Initialize structure of table 1000 
	 */
	protected void initTable1000() {
		IndicatorTable table = getTable1000();
		
		String total = getMessage("uk_UA.reports.tb10.1.total");

		table.addColumn(getMessage("uk_UA.reports.tb11.1.headerN"), "N");
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
		table.setValue("N", "newcases", 1F);
		table.addRow(getMessage("uk_UA.reports.relapses"), "relapses");
		table.setValue("N", "relapses", 2F);
		table.addRow(getMessage("uk_UA.reports.other"), "others");
		table.setValue("N", "others", 3F);
	}


	/**
	 * Initialize structure of table 2000 
	 */
	protected void initTable2000() {
		IndicatorTable table = getTable2000();
		table.addColumn(getMessage("uk_UA.reports.tb11.1.headerN"), "N");
		
		// titles are not used in the displaying of the table
		table.addColumn("CaseState.DIED.TB", "1");
		table.addColumn("CaseState.DIED.OTHER_CAUSES", "2");
		table.addColumn("CaseState.TREATMENT_INTERRUPTION", "3");
		table.addColumn("CaseState.TRANSFERRED_OUT", "4");
		table.addColumn("CaseState.DIAGNOSTIC_CHANGED", "5");
		table.addColumn("CaseState.OTHER", "6");
		table.addColumn("NOT_DONE", "7");
		
		table.addRow(getMessage("uk_UA.reports.newcases"), "newcases");
		table.setValue("N", "newcases", 1F);
		table.addRow(getMessage("uk_UA.reports.relapses"), "relapses");
		table.setValue("N", "relapses", 2F);
		table.addRow(getMessage("uk_UA.reports.other"), "others");
		table.setValue("N", "others", 3F);
	}

	protected void generateTables() {
		List<TbCase> lst;
		setCounting(true);
		/*getIndicatorFilters().setInfectionSite(InfectionSite.PULMONARY);
		getIndicatorFilters().setMicroscopyResult(IndicatorMicroscopyResult.POSITIVE);
		setCondition("c.state >= " + CaseState.ONTREATMENT.ordinal()+" and c.patientType = " + PatientType.NEW.ordinal());
		numcases[0] = ((Long)createQuery().getSingleResult()).intValue();
		setCondition("c.state >= " + CaseState.ONTREATMENT.ordinal()+" and c.patientType = " + PatientType.RELAPSE.ordinal());
		numcases[1] = ((Long)createQuery().getSingleResult()).intValue();
		setCondition("c.state >= " + CaseState.ONTREATMENT.ordinal()+" and c.patientType in (0,1,2,3,4,5)");
		numcases[2] = ((Long)createQuery().getSingleResult()).intValue();
		numcases[2] -= numcases[0]+numcases[1];
		if (numcases[0]+numcases[1]+numcases[2] == 0)
			return;*/
		getIndicatorFilters().setMicroscopyResult(null);
		
		int count = ((Long)createQuery().getSingleResult()).intValue();
		if (count==0) return;
		if (count<=4000){
			initVerifList("verify.tb10.error",2,1,1);
			setCounting(false);
			setOverflow(false);
			lst = createQuery().getResultList();
			Iterator<TbCase> it = lst.iterator();
			while(it.hasNext()){
				TbCase tc = it.next();
				if (MicroscopyIsNull(tc)){
					addToVerList(tc,1,1);
				}
				else{
					if (tc.getTreatmentPeriod()==null){
						addToVerList(tc,1,0);
					}
					else	
					if (rightMcTest(tc).getResult().isPositive()){
						addValueTable(tc.getTreatmentPeriod().getIniDate(), tc.getDiagnosisDate(), tc.getPatientType(), tc.getState(), tc.getIniContinuousPhase(), firstNegMicro(tc), isNoExam(tc));
						
						addToAllowing(tc);
					}
				}
			}
			String rows[] = {"newcases", "relapses", "others"};
			String cols[] = {"month2", "month3", "month4"};
			
			for (String rowid: rows)
				for (String colid: cols) {
					calcPercentage(colid, rowid);
				}
			getTotal2000();
			generateRepList(lst);
		}
		else 
			setOverflow(true);
	}
	
	private Date firstNegMicro(TbCase tc){
		for (ExamMicroscopy ex:tc.getExamsMicroscopy()){
			if (ex.getResult().isNegative())
				return ex.getDateCollected();
		}
		return null;
	}
	
	private boolean isNoExam(TbCase tc){
		if (tc.getIniContinuousPhase()==null)
			return false;
			
		List<ExamMicroscopy> lst = new ArrayList<ExamMicroscopy>();
		lst.addAll(tc.getExamsMicroscopy());
		/*for (int i = 0; i < tc.getExamsMicroscopy().size(); i++) {
			lst.add(null);
		}
		Collections.copy(lst, tc.getExamsMicroscopy());*/
		Collections.sort(lst, new Comparator<ExamMicroscopy>() {
			public int compare(ExamMicroscopy o1, ExamMicroscopy o2) {
				Calendar d1 = Calendar.getInstance();
				Calendar d2 = Calendar.getInstance();
				d1.setTime(o1.getDateCollected());
				d2.setTime(o2.getDateCollected());

				return d2.compareTo(d1);
			}
		});
		for (ExamMicroscopy ex:tc.getExamsMicroscopy()){
			if (ex.getDateCollected().before(tc.getIniContinuousPhase()) || ex.getDateCollected().equals(tc.getIniContinuousPhase()))
				return false;
		}
		return true;
	}
	
	@Override
	protected ExamMicroscopy rightMcTest(TbCase tc){
		return rightMcTestDuring14daysTreat(tc);
	}
	
	@Override
	protected ExamCulture rightCulTest(TbCase tc){
		return rightCulTestDuring14daysTreat(tc);
	}
	
	@Override
	protected String getHQLMicroscopyCondition() {
		IndicatorFilters filters = getIndicatorFilters();
		if (filters.getMicroscopyResult() == null)
			return null;
		
		String cond = getHQLMicroscopyResultCondition(filters.getMicroscopyResult());

		return "exists(select ex.id from ExamMicroscopy ex where ex.result " + cond + " and ex.tbcase.id = c.id)";
	}
	/**
	 * Generate values of table 1000 querying the database based on the user filters 
	 */
	protected void generateTable1000() {
		// calculate the number of cases
		getIndicatorFilters().setInfectionSite(InfectionSite.PULMONARY);
		getIndicatorFilters().setMicroscopyResult(IndicatorMicroscopyResult.POSITIVE);
		numcases[0] = calcNumberOfCases("c.state >= " + CaseState.ONTREATMENT.ordinal()+" and c.patientType = " + PatientType.NEW.ordinal());
		numcases[1] = calcNumberOfCases("c.state >= " + CaseState.ONTREATMENT.ordinal()+" and c.patientType = " + PatientType.RELAPSE.ordinal());
		numcases[2] = calcNumberOfCases("c.state >= " + CaseState.ONTREATMENT.ordinal()+" and c.patientType in (0,1,2,3,4,5)");
		numcases[2] -= numcases[0]+numcases[1];
		if (numcases[0]+numcases[1]+numcases[2] == 0)
			return;

		String hqlMicroscopy = "(select min(e.dateCollected) from ExamMicroscopy e " +
				"where e.tbcase.id = c.id and e.result " + getHQLMicroscopyResultCondition(IndicatorMicroscopyResult.NEGATIVE)+")";
		// check if it has microscopy
		String hasMicr = "(select max(e.dateCollected) from ExamMicroscopy e where e.tbcase.id = c.id and (e.dateCollected <= c.iniContinuousPhase or c.iniContinuousPhase = null))";
		
		setGroupFields(null);
		setConsolidated(false);
		//setHQLSelect("select count(*) ");
		setHQLSelect("select c.treatmentPeriod.iniDate, c.diagnosisDate, c.patientType, c.state, c.iniContinuousPhase, " + 
				hqlMicroscopy +	", " + 
				hasMicr);
		setCondition("c.state >= " + CaseState.ONTREATMENT.ordinal()+" and c.patientType in (0,1,2,3,4,5) and exists(select e.id from ExamMicroscopy e where e.result in (1,2,3,4) and e.tbcase.id = c.id and e.dateCollected = (select min(sp.dateCollected) from ExamMicroscopy sp where sp.tbcase.id = c.id))");
		
		List<Object[]> lst = createQuery()
			.getResultList();

		for (Object[] vals: lst) {
			boolean noexam = vals[6] == null;
			addValueTable((Date)vals[0], (Date)vals[1], (PatientType)vals[2], (CaseState)vals[3], (Date)vals[4], (Date)vals[5], noexam);
		}
		
		String rows[] = {"newcases", "relapses", "others"};
		String cols[] = {"month2", "month3", "month4"};
		
		for (String rowid: rows)
			for (String colid: cols) {
				calcPercentage(colid, rowid);
			}
		getTotal2000();
	}

	private void calcPercentage(String colid, String rowid) {
		float total = 0;
		if (rowid=="newcases") total = numcases[0];	
		if (rowid=="relapses") total = numcases[1];
		if (rowid=="others") total = numcases[2];
		
		if (total == 0)
			return;
		
		float num = getTable1000().getCellAsFloat(colid, rowid);
		
		getTable1000().setValue(colid + "_perc", rowid, num * 100F / total);
	}

	
	/**
	 * Add a value to a specific cell in the table based on the dates of the case
	 * @param dtIniTreat
	 * @param dtDiagnos
	 * @param dtMicro
	 */
	protected void addValueTable(Date dtIniTreat, Date dtDiagnos, PatientType ptype, CaseState state, Date iniCont, Date dtMicro, boolean noexam) {
		if (dtIniTreat == null)
			dtIniTreat = dtDiagnos;

		// select row id
		String rowkey;
		String colkey = null;
		switch (ptype) {
		case NEW: 
			rowkey = "newcases";
			numcases[0]++;
			break;
		case RELAPSE: 
			rowkey = "relapses";
			numcases[1]++;
			break;
		default: 
			rowkey = "others";
			numcases[2]++;
		}
		if (!noexam){
			if (dtMicro != null){ 
				boolean goodMicro = false;
				if (iniCont==null) 
					goodMicro = true;
					else 
						if (dtMicro.before(iniCont)) 
							goodMicro = true;
				
				if (goodMicro){
					int negativeMonth = DateUtils.monthsBetween(dtIniTreat, dtMicro);
			
					if (negativeMonth <= 2 && ptype.equals(PatientType.NEW))
						table1000.addIdValue("month2", rowkey, 1F);
					
					if (negativeMonth <= 3)
						table1000.addIdValue("month3", rowkey, 1F);
					
					if (negativeMonth <= 4)
						if (!ptype.equals(PatientType.NEW))
							table1000.addIdValue("month4", rowkey, 1F);
						
					if (negativeMonth > (ptype.equals(PatientType.NEW) ? 3 : 4))
						table1000.addIdValue("others", rowkey, 1F);
					}
				else 
					noexam = true;
			}
			else table1000.addIdValue("others", rowkey, 1F);
			}
		// add to noexam in tab1000 and tab2000
		if (noexam)
		{
			table1000.addIdValue("noexams", rowkey, 1F);
			switch (state) {
			case DIED:
				colkey = "1";
				break;
			case DIED_NOTTB:
				colkey = "2";
				break;
			case TREATMENT_INTERRUPTION:
				colkey = "3";
				break;
			case TRANSFERRED_OUT:
				colkey = "4";
				break;
			case NOT_CONFIRMED:
				colkey = "5";
				break;
			default:
				colkey = "6";
				break;
			}
			table2000.addIdValue(colkey, rowkey, 1F);
			table2000.addIdValue("7",rowkey,1F);
			}
		
		table1000.addIdValue("numcases", rowkey, 1F);
	}

	/**
	 * Create all tables of the report and generate indicators
	 */
	@Override
	protected void createTable() {
		table1000 = new IndicatorTable();
		table2000 = new IndicatorTable();
	}
	
	public IndicatorTable getTable1000() {
		if (table1000 == null)
			createTable();
		return table1000;
	}
	
	public IndicatorTable getTable2000() {
		if (table2000 == null)
			createTable();
		return table2000;
	}

	private boolean ExamEveryMonth(TbCase tc){
		boolean[] em = new boolean[tc.getTreatmentPeriod().getMonths()];
				
		Calendar d1 = Calendar.getInstance();
		d1.setTime(tc.getTreatmentPeriod().getIniDate());
		d1.add(Calendar.DAY_OF_MONTH, -7);
		Calendar d2 = Calendar.getInstance();
		for (int i = 0; i < em.length; i++) {
			d2 = (Calendar) d1.clone();
			d2.add(Calendar.MONTH, 1);
			d2.add(Calendar.DAY_OF_MONTH, 14);
			
			Period p = new Period(d1.getTime(), d2.getTime());
			boolean ttt = false;
			if (tc.getExamsMicroscopy().size()!=0)
						for (ExamMicroscopy ex:tc.getExamsMicroscopy()){
							if (p.isDateInside(ex.getDateCollected())){
								ttt = true;
								break;
							}
						}
			if (ttt)
				em[i] = true;
			d1.add(Calendar.MONTH, 1);
			
		}
		if (tc.getPatientType().equals(PatientType.NEW)){
			if (em.length>=3)
				if (em[1] && em[2])
					return true;
		}
		else 
			if (em.length>=4)
				if (em[2] && em[3])
					return true;
			
		return false;
	}
	
	@Override
	protected void addToAllowing(TbCase tc) {
		if (!ExamEveryMonth(tc)){
			addToVerList(tc,2,0);
		}
	}

}
