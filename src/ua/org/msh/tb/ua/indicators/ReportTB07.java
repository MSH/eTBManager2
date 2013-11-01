package org.msh.tb.ua.indicators;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.entities.AgeRange;
import org.msh.tb.entities.CaseComorbidity;
import org.msh.tb.entities.ExamCulture;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.tb.entities.PrevTBTreatment;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.HIVResult;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.IndicatorVerify;
import org.msh.tb.indicators.core.IndicatorFilters;
import org.msh.tb.indicators.core.IndicatorTable;
import org.msh.tb.ua.entities.CaseDataUA;

@Name("reportTB07")
@Scope(ScopeType.CONVERSATION)
public class ReportTB07 extends IndicatorVerify<TbCase> {
	private static final long serialVersionUID = -8462692609433997419L;
	private static final String rowid = "row";

	private IndicatorTable table1000;
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

		generateTables();
		sortAllLists();
	}

	/**
	 * Create all tables of the report and generate indicators
	 */
	@Override
	protected void createTable() {
		table1000 = new IndicatorTable();
		table2000 = new IndicatorTable();
		table3000 = new IndicatorTable();
		table4000 = new IndicatorTable();
		table5000 = new IndicatorTable();
	}

	@Override
	protected void addToAllowing(TbCase tc){
		Map<String, List<ErrItem>>  verifyList = getVerifyList();
		List<PrevTBTreatment> ptb = getEntityManager().createQuery("select t from PrevTBTreatment t where t.tbcase.id="+tc.getId()).getResultList();
		if (ptb!=null)
			if (tc.getPatientType().equals(PatientType.RELAPSE) && ptb.size()==0)
				addToVerList(tc,2,0);
		if (tc.getTreatmentPeriod()!=null)
			if (tc.getState().equals(CaseState.ONTREATMENT))
				if (tc.getTreatmentPeriod().getEndDate().before(new Date()))
					addToVerList(tc,2,1);
	}
	
	private void addToTable(IndicatorTable tab, String gen, String micResult, String key){
		tab.addIdValue(micResult+"_"+key,rowid, 1F);
		if (tab!=getTable2000())
			tab.addIdValue(micResult+"_all",rowid, 1F);
		if (tab!=getTable4000())
			tab.addIdValue(gen,rowid, 1F);
		tab.addIdValue("all",rowid, 1F);
	}
	
	protected void generateTables() {
		List<TbCase> lst;
		setCounting(true);
		int count = ((Long)createQuery().getResultList().get(0)).intValue();
		if (count<=4000){
			initVerifList("verify.tb07.error",6,2,1);
			setCounting(false);
			setOverflow(false);
			lst = createQuery().getResultList();
			Iterator<TbCase> it = lst.iterator();
			Map<String, List<ErrItem>>  verifyList = getVerifyList();
			
			while(it.hasNext()){
				TbCase tc = it.next();
				CaseDataUA cd = (CaseDataUA) getEntityManager().find(CaseDataUA.class, tc.getId());  //AK, previous line will rise exception, if no result
				if (cd == null){
					addToVerList(tc,1,0);
					continue;
				}
				
				if (cd.getRegistrationCategory().getValue()==null){
					addToVerList(tc,1,0);
				}
				else
					if (cd.getRegistrationCategory().getValue().getId()>=938221 && cd.getRegistrationCategory().getValue().getId()<=938223)
					{
						String key=null;
						if (tc.getPatientType()==null)
							addToVerList(tc,1,1);
						else
							switch (tc.getPatientType()) {
								case NEW: key = "newcases"; 
								break;
								case RELAPSE: key = "relapses"; 
								break;
							default:
								key="other";
								break;
							}
						if (key!=null){
							if (tc.getResHIV()==null){
								addToVerList(tc,1,4);
							}
							else if (tc.getResHIV().size()==0)
								addToVerList(tc,1,4);
							if(tc.getInfectionSite() == null){
								tc.setInfectionSite(InfectionSite.PULMONARY);
							}
							if (tc.getInfectionSite().equals(InfectionSite.PULMONARY) || tc.getInfectionSite().equals(InfectionSite.BOTH)){
								if (MicroscopyIsNull(tc) && CultureIsNull(tc)) addToVerList(tc,1,5);

								String micResult=null;
								if (!MicroscopyIsNull(tc))
									if (rightMcTest(tc).getResult().isPositive())
										micResult = "positive";
									else micResult = "negative";

								String gen = tc.getPatient().getGender().toString().toLowerCase();
								if (micResult==null) addToVerList(tc,1,2);
								
									addToTable(getTable1000(), gen, micResult==null ? "negative" : micResult, key);
									//System.out.println(tc.getDisplayCaseNumber()+"|"+tc.getPatient().getFullName()+"|"+key+(micResult==null ? "negative" : micResult));
									if (micResult!=null)
										if (micResult.equals("positive") && key.equalsIgnoreCase("newcases")){
											addToTable(getTable2000(), gen+"_all", Integer.toString(roundToIniDate(tc.getPatientAge())), gen);
											//System.out.println(tc.getDisplayCaseNumber()+"|"+tc.getPatient().getFullName()+"|"+gen+"|"+tc.getPatient().getBirthDate()+"|"+tc.getDiagnosisDate()+"|"+tc.getPatientAge());
									}
								

								if (!CultureIsNull(tc)){
									if (rightCulTest(tc).getResult().isPositive() || micResult=="positive")
										micResult = "positive";
									else micResult = "negative";
								}
								else
									addToVerList(tc,1,3);
								//if (micResult!=null){
									addToTable(getTable3000(),gen,micResult==null ? "negative" : micResult,key);
									//if ("negative".equals(micResult))
										//System.out.println("|"+tc.getDisplayCaseNumber()+"|"+tc.getPatient().getFullName()+"|"+tc.getPatientType()+"|"+(tc.getTreatmentPeriod()!=null?tc.getTreatmentPeriod().getIniDate():"")+"|"+(rightMcTest(tc)!=null?rightMcTest(tc).getDateCollected():"")+"|"+(rightCulTest(tc)!=null?rightCulTest(tc).getDateCollected():""));
								//}
								//else
									//System.out.println("|"+tc.getDisplayCaseNumber()+"|"+tc.getPatient().getFullName()+"|"+tc.getPatientType()+"|"+(tc.getTreatmentPeriod()!=null?tc.getTreatmentPeriod().getIniDate():"")+"|"+(rightMcTest(tc)!=null?rightMcTest(tc).getDateCollected():"")+"|");
									
								
								if (!verifyList.get(getMessage("verify.errorcat1")).get(4).getCaseList().contains(tc))
									if (caseHasHIV(tc))
										addToTable(getTable4000(),gen,"pulmonary",key);
								addToAllowing(tc);
							}
							else{
								String gen = tc.getPatient().getGender().toString().toLowerCase();
								
								addToTable(getTable1000(),gen,"extrapulmonary",key);
								//System.out.println(tc.getDisplayCaseNumber()+"|"+tc.getPatient().getFullName()+"|"+key+"extrapulmonary");
								
								addToTable(getTable3000(),gen,"extrapulmonary",key);
								
								if (!verifyList.get(getMessage("verify.errorcat1")).get(4).getCaseList().contains(tc))
									if (caseHasHIV(tc))
										addToTable(getTable4000(),gen,"extrapulmonary",key);
								
								addToAllowing(tc);
							}
						if (!MicroscopyIsNull(tc))
							if (tc.getRegistrationDate()!=null && cd.getDateFirstVisitGMC()!=null)
								if (examGMCpos(tc,cd.getDateFirstVisitGMC()))
									getTable5000().addIdValue("col2",rowid, 1F);
						}else
							addToVerList(tc,1,1);
					}else
						addToVerList(tc,1,0);
			}
			IndicatorFilters filters = (IndicatorFilters)Component.getInstance("indicatorFilters");
			int numcases = filters.getNumcases();
			getTable5000().setValue("col1", rowid, (float)numcases);
			if (numcases != 0){
				Float val = getTable5000().getValue("col2", rowid);
				if (val==null) val = 0F;
				getTable5000().addIdValue("col3", rowid, val*100/numcases);
			}
			generateRepList(lst);
		}
		else 
			setOverflow(true);
	}

	private boolean caseHasHIV(TbCase tc) {
		/*if (tc.getResHIV().get(0).getResult().equals(HIVResult.POSITIVE))
			return true;*/
		boolean res = false;
		for (CaseComorbidity cc:tc.getComorbidities()){
			if (cc.getId()==938244){
				res = true;
				break;
			}
		}
		if (!res)
			if (tc.getResHIV().get(0).getResult().equals(HIVResult.POSITIVE))
				res = true;
		CaseDataUA cd = (CaseDataUA) getEntityManager().find(CaseDataUA.class, tc.getId());  //AK, previous line will rise exception, if no result
		if (cd.isHiv())
			res = true;
		return res;
	}

	/**
	 * @return true if exist positive result of microscopy, collected in GMC-units
	 * */
	private boolean examGMCpos(TbCase tc, Date gmc){
		for (ExamMicroscopy res: tc.getExamsMicroscopy()){
			if (res.getDateCollected().before(tc.getRegistrationDate()) && res.getDateCollected().after(gmc))
				if (res.getResult().isPositive())
					return true;
		}
		return false;
	}
	
	/**
	 * Round age to the lower boundary of the age-interval
	 * */
	private int roundToIniDate(int age){
		List<AgeRange> ageRanges = getAgeRangeHome().getItems();

		for (AgeRange r: ageRanges) {
			if (age>=r.getIniAge() && (age<=r.getEndAge()))
				return r.getIniAge();
		}
		return 0;
	}

	@Override
	protected String getHQLJoin() {
		return null;
	}


	@Override
	protected ExamMicroscopy rightMcTest(TbCase tc){
		return rightMcTestBeforeTreat(tc);
	}
	
	@Override
	protected ExamCulture rightCulTest(TbCase tc){
		return rightCulTestBeforeTreat(tc);
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
	 * @return the table1000
	 */
	public IndicatorTable getTable1000() {
		if (table1000 == null)
			createTable();
		return table1000;
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
	/**
	 * Clear all tables and verifyList
	 * */
	public void clear(){
		table1000 = null;
		table2000 = null;
		table3000 = null;
		table4000 = null;
		table5000 = null;
		setVerifyList(null);
	}
}
