package org.msh.tb.ua;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.AgeRange;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.tb.entities.PrevTBTreatment;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.HIVResult;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.core.IndicatorFilters;
import org.msh.tb.indicators.core.IndicatorTable;
import org.msh.tb.ua.entities.CaseDataUA;

@Name("reportTB07")
public class ReportTB07 extends IndicatorVerify {
	private static final long serialVersionUID = -8462692609433997419L;
	private static final String rowid = "row";

	private IndicatorTable table1000;
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
				if (!verifyList.get(getMessage("verify.errorcat2")).get(0).getCaseList().contains(tc))
					verifyList.get(getMessage("verify.errorcat2")).get(0).getCaseList().add(tc);
		if (tc.getTreatmentPeriod()!=null)
			if (tc.getState().equals(CaseState.ONTREATMENT))
				if (tc.getTreatmentPeriod().getEndDate().before(new Date()))
					if (!verifyList.get(getMessage("verify.errorcat2")).get(1).getCaseList().contains(tc))
						verifyList.get(getMessage("verify.errorcat2")).get(1).getCaseList().add(tc);
			
		if (!verifyList.get(getMessage("verify.errorcat2")).get(0).getCaseList().contains(tc) && !verifyList.get(getMessage("verify.errorcat2")).get(1).getCaseList().contains(tc))
			if (!verifyList.get(getMessage("verify.errorcat3")).get(0).getCaseList().contains(tc))
				verifyList.get(getMessage("verify.errorcat3")).get(0).getCaseList().add(tc);
	}
	
	
	protected void generateTables() {
		List<TbCase> lst;
		setCounting(true);
		int count = ((Long)createQuery().getSingleResult()).intValue();
		if (count<=4000){
			initVerifList("verify.tb07.error",5,2,1);
			setCounting(false);
			setOverflow(false);
			lst = createQuery().getResultList();
			Iterator<TbCase> it = lst.iterator();
			Map<String, List<ErrItem>>  verifyList = getVerifyList();
			
			while(it.hasNext()){
				TbCase tc = it.next();
				CaseDataUA cd = (CaseDataUA) getEntityManager().find(CaseDataUA.class, tc.getId());  //AK, previous line will rise exception, if no result
				if (cd == null){
					if (!verifyList.get(getMessage("verify.errorcat1")).get(0).getCaseList().contains(tc))
						verifyList.get(getMessage("verify.errorcat1")).get(0).getCaseList().add(tc);
					continue;
				}
				
				if (cd.getRegistrationCategory().getValue()==null){
					if (!verifyList.get(getMessage("verify.errorcat1")).get(0).getCaseList().contains(tc))
						verifyList.get(getMessage("verify.errorcat1")).get(0).getCaseList().add(tc);
				}
				else
					if (cd.getRegistrationCategory().getValue().getId()>=938221 && cd.getRegistrationCategory().getValue().getId()<=938223)
					{
						String key=null;
						switch (tc.getPatientType()) {
						case NEW: key = "newcases"; 
						break;
						case RELAPSE: key = "relapses"; 
						break;
						case AFTER_DEFAULT: case FAILURE_FT: case FAILURE_RT: key = "other";
						break;
						case OTHER:
							if (!verifyList.get(getMessage("verify.errorcat1")).get(1).getCaseList().contains(tc))
								verifyList.get(getMessage("verify.errorcat1")).get(1).getCaseList().add(tc);
							break;
						}
						if (key!=null){
							if (tc.getResHIV()==null){
								if (!verifyList.get(getMessage("verify.errorcat1")).get(4).getCaseList().contains(tc))
									verifyList.get(getMessage("verify.errorcat1")).get(4).getCaseList().add(tc);
							}
							else if (tc.getResHIV().size()==0)
								if (!verifyList.get(getMessage("verify.errorcat1")).get(4).getCaseList().contains(tc))
									verifyList.get(getMessage("verify.errorcat1")).get(4).getCaseList().add(tc);

							if (tc.getInfectionSite().equals(InfectionSite.PULMONARY) || tc.getInfectionSite().equals(InfectionSite.BOTH)){
								if (!MicroscopyIsNull(tc) || !CultureIsNull(tc)){

										String micResult=null;
										if (!MicroscopyIsNull(tc))
												if (rightMcTest(tc).getResult().isPositive())
													micResult = "positive";
												else micResult = "negative";

										if (micResult!=null){
											getTable1000().addIdValue(micResult+"_"+key,rowid, 1F);
											getTable1000().addIdValue(micResult+"_all",rowid, 1F);
											getTable1000().addIdValue(tc.getPatient().getGender().toString().toLowerCase(),rowid, 1F);
											getTable1000().addIdValue("all",rowid, 1F);
											if (micResult.equals("positive")){
												getTable2000().addIdValue(roundToIniDate(tc.getPatientAge())+"_"+tc.getPatient().getGender().toString().toLowerCase(),rowid, 1F);
												getTable2000().addIdValue(tc.getPatient().getGender().toString().toLowerCase()+"_all",rowid, 1F);
												getTable2000().addIdValue("all",rowid, 1F);
											}
										}
										else
											if (!verifyList.get(getMessage("verify.errorcat1")).get(2).getCaseList().contains(tc))
												verifyList.get(getMessage("verify.errorcat1")).get(2).getCaseList().add(tc);

										if (!CultureIsNull(tc)){
												if (rightCulTest(tc).getResult().isPositive() || micResult=="positive")
													micResult = "positive";
												else micResult = "negative";
											}
											else
												if (!verifyList.get(getMessage("verify.errorcat1")).get(3).getCaseList().contains(tc))
													verifyList.get(getMessage("verify.errorcat1")).get(3).getCaseList().add(tc);
										if (micResult!=null){
											getTable3000().addIdValue(micResult+"_"+key,rowid, 1F);
											getTable3000().addIdValue(micResult+"_all",rowid, 1F);
											getTable3000().addIdValue(tc.getPatient().getGender().toString().toLowerCase(),rowid, 1F);
											getTable3000().addIdValue("all",rowid, 1F);
										}

										

										if (!verifyList.get(getMessage("verify.errorcat1")).get(4).getCaseList().contains(tc))
											if (tc.getResHIV().get(0).getResult().equals(HIVResult.POSITIVE))
											{
												getTable4000().addIdValue("pulmonary_"+key, rowid, 1F);
												getTable4000().addIdValue("pulmonary_all", rowid, 1F);
												getTable4000().addIdValue("all",rowid, 1F);
											}
										addToAllowing(tc);
									}
							}
							else{
								getTable1000().addIdValue("extrapulmonary_"+key,rowid, 1F);
								getTable1000().addIdValue("extrapulmonary_all",rowid, 1F);
								getTable1000().addIdValue(tc.getPatient().getGender().toString().toLowerCase(),rowid, 1F);
								getTable1000().addIdValue("all",rowid, 1F);

								getTable3000().addIdValue("extrapulmonary_"+key,rowid, 1F);
								getTable3000().addIdValue("extrapulmonary_all",rowid, 1F);
								getTable3000().addIdValue(tc.getPatient().getGender().toString().toLowerCase(),rowid, 1F);
								getTable3000().addIdValue("all",rowid, 1F);

								if (!verifyList.get(getMessage("verify.errorcat1")).get(4).getCaseList().contains(tc))
									if (tc.getResHIV().get(0).getResult().equals(HIVResult.POSITIVE)){
										getTable4000().addIdValue("extrapulmonary_"+key, rowid, 1F);
										getTable4000().addIdValue("extrapulmonary_all", rowid, 1F);
										getTable4000().addIdValue("all",rowid, 1F);
									}
								addToAllowing(tc);
							}
						if (!MicroscopyIsNull(tc))
							if (tc.getRegistrationDate()!=null && cd.getDateFirstVisitGMC()!=null)
								if (examGMCpos(tc,cd.getDateFirstVisitGMC()))
									getTable5000().addIdValue("col2",rowid, 1F);
						}
					}
			}
			IndicatorFilters filters = (IndicatorFilters)Component.getInstance("indicatorFilters");
			int numcases = filters.getNumcases();
			getTable5000().setValue("col1", rowid, (float)numcases);
			if (numcases != 0)
				getTable5000().addIdValue("col3", rowid, getTable5000().getValue("col2", rowid)*100/numcases);
		}
		else 
			setOverflow(true);
	}

	private boolean examGMCpos(TbCase tc, Date gmc){
		for (ExamMicroscopy res: tc.getExamsMicroscopy()){
			if (res.getDateCollected().before(tc.getRegistrationDate()) && res.getDateCollected().after(gmc))
				if (res.getResult().isPositive())
					return true;
		}
		return false;
	}
	
	
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
}
