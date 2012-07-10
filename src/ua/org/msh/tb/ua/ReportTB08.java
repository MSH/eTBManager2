package org.msh.tb.ua;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.ExamCulture;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.ExtraOutcomeInfo;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.ValidationState;
import org.msh.tb.entities.enums.YesNoType;
import org.msh.tb.indicators.core.IndicatorTable;
import org.msh.tb.indicators.core.IndicatorTable.TableRow;
import org.msh.tb.ua.entities.CaseDataUA;
import org.msh.utils.date.Period;

@Name("reportTB08")

public class ReportTB08 extends IndicatorVerify {
	private static final long serialVersionUID = -1617171254497253851L;


	private IndicatorTable table1000;
	private IndicatorTable table2000;
	private IndicatorTable table3000;

	private HashSet<Integer> hashSet;

	private static final String statesIn = "(" + 
	CaseState.CURED.ordinal() + "," + 
	CaseState.TREATMENT_COMPLETED.ordinal() + "," +
	CaseState.DIED.ordinal() + "," +
	CaseState.FAILED.ordinal() + "," +
	CaseState.TREATMENT_INTERRUPTION.ordinal() + "," +
	CaseState.NOT_CONFIRMED.ordinal() + "," +
	CaseState.TRANSFERRED_OUT.ordinal() + ","+
	CaseState.ONTREATMENT.ordinal()+")";


	/* (non-Javadoc)
	 * @see org.msh.tb.indicators.core.Indicator#createIndicators()
	 */
	@Override
	protected void createIndicators() {
		initTable(getTable1000());
		initTable(getTable2000());
		initTable3000();
		hashSet = new HashSet<Integer>();
		hashSet.add(CaseState.CURED.ordinal());
		hashSet.add(CaseState.TREATMENT_COMPLETED.ordinal());
		hashSet.add(CaseState.DIED.ordinal());
		hashSet.add(CaseState.FAILED.ordinal());
		hashSet.add(CaseState.TREATMENT_INTERRUPTION.ordinal());
		hashSet.add(CaseState.NOT_CONFIRMED.ordinal());
		hashSet.add(CaseState.TRANSFERRED_OUT.ordinal());

		generateTables();
		sortAllLists();
		//System.out.println(verifyList.get(0).size()+", "+verifyList.get(1).size()+", "+verifyList.get(2).size()+", "+verifyList.get(3).size()+", "+verifyList.get(4).size());
		//generateTable1000();
		//generateTable2000();
		//generateTable3000();
	}



	private void addToAllowing(TbCase tc){
		Map<String, List<ErrItem>>  verifyList = getVerifyList();
		if (!verifyList.get(getMessage("verify.errorcat2")).get(0).getCaseList().contains(tc))
			if (tc.getState().equals(CaseState.CURED))
				if (!curedMc(tc) || !curedMcCul(tc))
					verifyList.get(getMessage("verify.errorcat2")).get(0).getCaseList().add(tc);
		if (!verifyList.get(getMessage("verify.errorcat2")).get(0).getCaseList().contains(tc))
			if (!verifyList.get(getMessage("verify.errorcat3")).get(0).getCaseList().contains(tc))
				verifyList.get(getMessage("verify.errorcat3")).get(0).getCaseList().add(tc);
	}


	private void generateTables(){
		List<TbCase> lst;
		getIndicatorFilters().setInfectionSite(InfectionSite.PULMONARY);
		setCondition("c.state in " + statesIn);
		setConsolidated(true);
		setCounting(true);
		int ind=0;
		int count = ((Long)createQuery().getSingleResult()).intValue();
		if (count<=4000){
			initVerifList("verify.tb08.error",3,2,1);
			setCounting(false);
			setOverflow(false);
			lst = createQuery().getResultList();
			Iterator<TbCase> it = lst.iterator();
			Map<String, List<ErrItem>>  verifyList = getVerifyList();
			while(it.hasNext()){
				TbCase tc = it.next();
				//CaseDataUA cd = (CaseDataUA) getEntityManager().createQuery("select ua from CaseDataUA ua where ua.tbcase.id="+tc.getId()).getSingleResult();
				CaseDataUA cd = (CaseDataUA) getEntityManager().find(CaseDataUA.class, tc.getId());  //AK, previous line will rise exception, if no result
				if (cd == null){
					if (!verifyList.get(getMessage("verify.errorcat1")).get(0).getCaseList().contains(tc))
						verifyList.get(getMessage("verify.errorcat1")).get(0).getCaseList().add(tc);
					continue;
				}
				String key;
				switch (tc.getPatientType()) {
				case NEW: key = "new"; 
				break;
				case RELAPSE: key = "relapse"; 
				break;
				default: key = "other";
				break;
				}
				ind++;
				//System.out.println(ind);
				//if (ind==51)
				//System.out.println("asd");
				if (hashSet.contains(tc.getState().ordinal())){
					if (tc.getExamsMicroscopy()!=null || tc.getExamsCulture()!=null)
						if (tc.getExamsMicroscopy().size()!=0 || tc.getExamsCulture().size()!=0)
						{
							CaseState state = tc.getState();
							ExtraOutcomeInfo extraOutcome = cd.getExtraOutcomeInfo();


							Object col = state;

							if (state == CaseState.DIED) {
								if ((extraOutcome == ExtraOutcomeInfo.TB) || (extraOutcome == ExtraOutcomeInfo.OTHER_CAUSES))
									col = extraOutcome;
								else col = ExtraOutcomeInfo.OTHER_CAUSES; // default value
							}
							else if (state == CaseState.FAILED) {
								if ((extraOutcome == ExtraOutcomeInfo.CLINICAL_EXAM) ||
										(extraOutcome == ExtraOutcomeInfo.CULTURE_SMEAR) ||
										(extraOutcome == ExtraOutcomeInfo.TRANSFER_CATIV))
									col = extraOutcome;
								else col = state;
							}

							String micResult=null;
							if (tc.getExamsMicroscopy()!=null)
								if (tc.getExamsMicroscopy().size()!=0)
									if (tc.getExamsMicroscopy().get(0).getResult().isPositive())
										micResult = "_positive";
									else micResult = "_negative";

							if (col != null) {
								if (micResult!=null)
									if (!(micResult.equals("_negative") && state.equals(CaseState.CURED)))
										if (!state.equals(CaseState.FAILED))
										{
											getTable1000().addIdValue(col, key + micResult, 1F);
											getTable1000().addIdValue("TOTAL", key + micResult, 1F);
											addToAllowing(tc);
										}
										else 
											if (!verifyList.get(getMessage("verify.errorcat1")).get(2).getCaseList().contains(tc))
												verifyList.get(getMessage("verify.errorcat1")).get(2).getCaseList().add(tc);
								if (tc.getExamsCulture()!=null)
									if (tc.getExamsCulture().size()!=0){
										if (tc.getExamsCulture().get(0).getResult().isPositive() || micResult=="_positive")
											micResult = "_positive";
										else micResult = "_negative";
									}
								if (micResult!=null)
									if (!(micResult.equals("_negative") && state.equals(CaseState.CURED))){
										if (!state.equals(CaseState.FAILED))
										{
											getTable2000().addIdValue(col, key + micResult, 1F);
											getTable2000().addIdValue("TOTAL", key + micResult, 1F);
											addToAllowing(tc);
										}
										else 
											if (!verifyList.get(getMessage("verify.errorcat1")).get(2).getCaseList().contains(tc))
												verifyList.get(getMessage("verify.errorcat1")).get(2).getCaseList().add(tc);
									}
									else
										if (!verifyList.get(getMessage("verify.errorcat1")).get(0).getCaseList().contains(tc))
											verifyList.get(getMessage("verify.errorcat1")).get(0).getCaseList().add(tc);
							}


						}
				}
				else 
					if (tc.getState().equals(CaseState.ONTREATMENT)){
						if (tc.getTreatmentPeriod()!=null)
							if (tc.getTreatmentPeriod().getEndDate().before(new Date()))
								verifyList.get(getMessage("verify.errorcat1")).get(1).getCaseList().add(tc);
					}

				if (cd.getPulmonaryDestruction()==YesNoType.YES){
					getTable3000().addIdValue("col1", key, 1F);
					if (tc.getResXRay()!=null)
						if (tc.getResXRay().size()!=0)
							if (!tc.getResXRay().get(tc.getResXRay().size()-1).getDestruction())
								getTable3000().addIdValue("col2", key, 1F);	
				}
			}
			// calculate percentage
			getTable3000().getColumns().get(2).setNumberPattern("#,###,##0.00");
			for (TableRow row: getTable3000().getRows()) {
				Float val1 = getTable3000().getValue("col1", row.getId().toString());
				Float val2 = getTable3000().getValue("col2", row.getId().toString());
				float perc = 0;

				if ((val1 != null) && (val2 != null))
					perc = val2 / val1 * 100;
				getTable3000().addIdValue("col3", row.getId(), perc);
			}
		}
		else
		{
			//indicatorControllerUA.setExecuting(false);
			setOverflow(true);
		}

	}

	private boolean curedMc(TbCase tc) {
		if (tc.getExamsMicroscopy()!=null)
			if (tc.getExamsMicroscopy().size()>2){
				List<ExamMicroscopy> lst = new ArrayList<ExamMicroscopy>();
				for (int i = 0; i < tc.getExamsMicroscopy().size(); i++) {
					lst.add(null);
				}
				Collections.copy(lst, tc.getExamsMicroscopy());
				Collections.sort(lst, new Comparator<ExamMicroscopy>() {
					public int compare(ExamMicroscopy o1, ExamMicroscopy o2) {
						Calendar d1 = Calendar.getInstance();
						Calendar d2 = Calendar.getInstance();
						d1.setTime(o1.getDateCollected());
						d2.setTime(o2.getDateCollected());

						return d2.compareTo(d1);
					}
				});
				if (tc.getTreatmentPeriod() != null){
					Calendar d2 = Calendar.getInstance();
					d2.setTime(tc.getTreatmentPeriod().getEndDate());
					d2.add(Calendar.DAY_OF_MONTH, 7);
					Calendar d1 = (Calendar) d2.clone();
					d1.add(Calendar.MONTH, -1);
					d1.add(Calendar.DAY_OF_MONTH, -9);
					Period p = new Period(d1.getTime(), d2.getTime());

					if (p.isDateInside(lst.get(0).getDateCollected()) 
							&& lst.get(0).getResult().isNegative()
							&& lst.get(1).getResult().isNegative()
							&& lst.get(2).getResult().isNegative())
						return true;
				}
				else
					if (!getVerifyList().get(getMessage("verify.errorcat2")).get(1).getCaseList().contains(tc))
						getVerifyList().get(getMessage("verify.errorcat2")).get(1).getCaseList().add(tc);

			}
		return false;
	}

	private boolean curedMcCul(TbCase tc) {
		List<ExamMicroscopy> lst = new ArrayList<ExamMicroscopy>();
		if (tc.getExamsMicroscopy()!=null)
			if (tc.getExamsMicroscopy().size()>1){
				for (int i = 0; i < tc.getExamsMicroscopy().size(); i++) {
					lst.add(null);
				}
				Collections.copy(lst, tc.getExamsMicroscopy());
				Collections.sort(lst, new Comparator<ExamMicroscopy>() {
					public int compare(ExamMicroscopy o1, ExamMicroscopy o2) {
						Calendar d1 = Calendar.getInstance();
						Calendar d2 = Calendar.getInstance();
						d1.setTime(o1.getDateCollected());
						d2.setTime(o2.getDateCollected());

						return d2.compareTo(d1);
					}
				});
			}

		List<ExamCulture> lst2 = new ArrayList<ExamCulture>();
		if (tc.getExamsCulture()!=null)
			if (tc.getExamsCulture().size()>1){
				for (int i = 0; i < tc.getExamsCulture().size(); i++) {
					lst2.add(null);
				}
				Collections.copy(lst2, tc.getExamsCulture());
				Collections.sort(lst2, new Comparator<ExamCulture>() {
					public int compare(ExamCulture o1, ExamCulture o2) {
						Calendar d1 = Calendar.getInstance();
						Calendar d2 = Calendar.getInstance();
						d1.setTime(o1.getDateCollected());
						d2.setTime(o2.getDateCollected());

						return d2.compareTo(d1);
					}
				});
			}
		if (tc.getTreatmentPeriod() != null){
			Calendar d2 = Calendar.getInstance();
			d2.setTime(tc.getTreatmentPeriod().getEndDate());
			d2.add(Calendar.DAY_OF_MONTH, 7);
			Calendar d1 = (Calendar) d2.clone();
			d1.add(Calendar.MONTH, -1);
			d1.add(Calendar.DAY_OF_MONTH, -9);
			Period p = new Period(d1.getTime(), d2.getTime());

			if (lst.size()>1 && lst2.size()>1){
				if (((p.isDateInside(lst.get(0).getDateCollected()) 
						&& lst.get(0).getResult().isNegative()) ||
						(p.isDateInside(lst2.get(0).getDateCollected()) 
								&& lst2.get(0).getResult().isNegative()))	
								&& (lst.get(1).getResult().isNegative() || 
										lst2.get(1).getResult().isNegative()))
					return true;
			}
			else
				if (lst2.size()>1)
				{
					if (((p.isDateInside(lst2.get(0).getDateCollected()) 
							&& lst2.get(0).getResult().isNegative()))	
							&& (lst2.get(1).getResult().isNegative()))
						return true;
				}
				else
					if (lst.size()>1)
					{
						if (((p.isDateInside(lst.get(0).getDateCollected()) 
								&& lst.get(0).getResult().isNegative()))	
								&& (lst.get(1).getResult().isNegative()))
							return true;
					}
		}
		else 
			if (!getVerifyList().get(getMessage("verify.errorcat2")).get(1).getCaseList().contains(tc))
				getVerifyList().get(getMessage("verify.errorcat2")).get(1).getCaseList().add(tc);

		return false;	
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.indicators.core.CaseHQLBase#getHQLValidationState()
	 */
	protected String getHQLValidationState() {
		return "c.validationState = "+ValidationState.VALIDATED.ordinal();
	}

	/**
	 * Initialize table layout. Because both table 1000 and table 2000 share the same layout, they use a common
	 * method to initialize its layout
	 * @param table
	 */
	protected void initTable(IndicatorTable table) {
		table.addColumn(getMessage("CaseState.CURED"), CaseState.CURED);
		table.addColumn(getMessage("CaseState.TREATMENT_COMPLETED"), CaseState.TREATMENT_COMPLETED);
		table.addColumn(getMessage("uk_UA.ExtraOutcomeInfo.TB"), ExtraOutcomeInfo.TB);
		table.addColumn(getMessage("uk_UA.ExtraOutcomeInfo.OTHER_CAUSES"), ExtraOutcomeInfo.OTHER_CAUSES);
		table.addColumn(getMessage("uk_UA.ExtraOutcomeInfo.CULTURE_SMEAR"), ExtraOutcomeInfo.CULTURE_SMEAR);
		table.addColumn(getMessage("uk_UA.ExtraOutcomeInfo.CLINICAL_EXAM"), ExtraOutcomeInfo.CLINICAL_EXAM);
		table.addColumn(getMessage("uk_UA.ExtraOutcomeInfo.TRANSFER_CATIV"), ExtraOutcomeInfo.TRANSFER_CATIV);
		table.addColumn(getMessage("CaseState.TREATMENT_INTERRUPTION"), CaseState.TREATMENT_INTERRUPTION);
		table.addColumn(getMessage("CaseState.NOT_CONFIRMED"), CaseState.NOT_CONFIRMED);
		table.addColumn(getMessage("CaseState.TRANSFERRED_OUT"), CaseState.TRANSFERRED_OUT);
		table.addColumn(getMessage("global.total"), "TOTAL");

		String txtPositive = getMessage("global.positive");
		String txtNegative = getMessage("global.negative");

		table.addRow(txtPositive, "new_positive");
		table.addRow(txtNegative, "new_negative");

		table.addRow(txtPositive, "relapse_positive");
		table.addRow(txtNegative, "relapse_negative");

		table.addRow(txtPositive, "other_positive");
		table.addRow(txtNegative, "other_negative");
	}


	/**
	 * Initialize structure of table 3000
	 */
	protected void initTable3000() {
		table3000.addColumn(getMessage("uk_UA.reports.tb08.3.header2"), "col1");
		table3000.addColumn(getMessage("uk_UA.reports.tb08.3.header3"), "col2");
		table3000.addColumn(getMessage("uk_UA.reports.tb08.3.header4"), "col3");
		table3000.addRow(getMessage("PatientType.NEW"), "new");
		table3000.addRow(getMessage("PatientType.RELAPSE"), "relapse");
		table3000.addRow(getMessage("PatientType.OTHER"), "other");
	}

	/**
	 * Create all tables of the report and generate indicators
	 */
	@Override
	protected void createTable() {
		table1000 = new IndicatorTable();
		table2000 = new IndicatorTable();
		table3000 = new IndicatorTable();
	}

	/**
	 * Return table 1000 report
	 * @return
	 */
	public IndicatorTable getTable1000() {
		if (table1000 == null)
			createTable();
		return table1000;
	}


	/**
	 * Return table2000 report
	 * @return
	 */
	public IndicatorTable getTable2000() {
		if (table2000 == null)
			createTable();
		return table2000;
	}


	/**
	 * Return table3000 report
	 * @return
	 */
	public IndicatorTable getTable3000() {
		if (table3000 == null)
			createTable();
		return table3000;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.indicators.core.CaseHQLBase#getHQLJoin()
	 */
	@Override
	protected String getHQLFrom() {
		return "from TbCase c";
	}

	@Override
	protected String getHQLSelect() {
		if (isCounting()) 
			return "select count(*)";
		else
			return "select c";
	}

}
