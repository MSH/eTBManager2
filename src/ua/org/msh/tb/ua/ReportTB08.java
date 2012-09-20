package org.msh.tb.ua;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.ExamCulture;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.ExtraOutcomeInfo;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.YesNoType;
import org.msh.tb.indicators.IndicatorVerify;
import org.msh.tb.indicators.core.IndicatorTable;
import org.msh.tb.indicators.core.IndicatorTable.TableRow;
import org.msh.tb.ua.entities.CaseDataUA;
import org.msh.utils.date.Period;

@Name("reportTB08")

public class ReportTB08 extends IndicatorVerify<TbCase> {
	private static final String NEGATIVE = "_negative";


	private static final String POSITIVE = "_positive";


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
		getIndicatorFilters().setInfectionSite(null);
	}


	@Override
	protected void addToAllowing(TbCase tc){
		if (tc.getState().equals(CaseState.CURED))
				if (!curedMc(tc) || !curedMcCul(tc))
					addToVerList(tc,2,0);
	}


	protected void generateTables(){
		List<TbCase> lst;
		getIndicatorFilters().setInfectionSite(InfectionSite.PULMONARY);
		setCondition("c.state in " + statesIn);
		setConsolidated(true);
		setCounting(true);
		int count = ((Long)createQuery().getSingleResult()).intValue();
		if (count<=4000){
			initVerifList("verify.tb08.error",7,4,1);
			setCounting(false);
			setOverflow(false);
			lst = createQuery().getResultList();
			Iterator<TbCase> it = lst.iterator();
			int ind=0;
			while(it.hasNext()){
				TbCase tc = it.next();
				ind++;
				System.out.println(ind);
				if (ind==9)
					System.out.println(ind);
				//CaseDataUA cd = (CaseDataUA) getEntityManager().createQuery("select ua from CaseDataUA ua where ua.tbcase.id="+tc.getId()).getSingleResult();
				CaseDataUA cd = (CaseDataUA) getEntityManager().find(CaseDataUA.class, tc.getId());  //AK, previous line will rise exception, if no result
				if (cd == null){
					addToVerList(tc,1,0);
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
				if (hashSet.contains(tc.getState().ordinal())){
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
						else {
							addToVerList(tc,1,2);
							continue; // unknown extra outcome if failed !!!!
						}
					}
					if (col != null) {
						//calculate 1000 and 2000
						String micResult = addTo1000(tc, key, col);
						addTo2000(tc, key, col, micResult);
					}
				}
				else 
					if (tc.getState().equals(CaseState.ONTREATMENT)){
						if (tc.getTreatmentPeriod()!=null)
							if (tc.getTreatmentPeriod().getEndDate().before(new Date()))
								addToVerList(tc,1,1);
							else
								addToVerList(tc,1,5);
					}

				if (cd.getPulmonaryDestruction()==YesNoType.YES){
					getTable3000().addIdValue("col1", key, 1F);
					if (tc.getResXRay()!=null)
						if (tc.getResXRay().size()!=0)
							if (tc.getResXRay().get(tc.getResXRay().size()-1).getDestruction() != null){
								if (!tc.getResXRay().get(tc.getResXRay().size()-1).getDestruction())
									getTable3000().addIdValue("col2", key, 1F);
							}
							else
								addToVerList(tc,1,6);
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
			generateRepList(lst);
		}
		else
		{
			//indicatorControllerUA.setExecuting(false);
			setOverflow(true);
		}

	}

	/**
	 * Add case to table 2000
	 * @param tc case
	 * @param patientType 
	 * @param col
	 * @param micResult microscope result calculated in addTo1000
	 */
	private void addTo2000(TbCase tc, String patientType, Object col, String micResult) {
		String culmicResult = null;
		// take result by culture
		if (!CultureIsNull(tc)){
			if (rightCulTest(tc).getResult().isPositive())
				culmicResult = POSITIVE;
			else culmicResult = NEGATIVE;
		}else{ // no culture, take by microscope
			culmicResult = micResult;
		}
		if (culmicResult!=null){
			getTable2000().addIdValue(col, patientType + culmicResult, 1F);
			getTable2000().addIdValue("TOTAL", patientType + culmicResult, 1F);
			addToAllowing(tc);
			if ((culmicResult.equals(NEGATIVE) && tc.getState().equals(CaseState.CURED)))
				addToVerList(tc,2,3);
		}
		else
			addToVerList(tc,1,4);
	}

	/**
	 * Add case to table 1000
	 * @param tc case
	 * @param patientType
	 * @param col
	 * @return TODO
	 */
	private String addTo1000(TbCase tc, String patientType, Object col) {
		String micResult=null;
		if (!MicroscopyIsNull(tc))
			if (rightMcTest(tc).getResult().isPositive())
				micResult = POSITIVE;
			else micResult = NEGATIVE;

		if (micResult!=null){
			getTable1000().addIdValue(col, patientType + micResult, 1F);
			getTable1000().addIdValue("TOTAL", patientType + micResult, 1F);
			addToAllowing(tc);
			if ((micResult.equals(NEGATIVE) && tc.getState().equals(CaseState.CURED)))
				addToVerList(tc,2,2);
		}
		else
			addToVerList(tc,1,3);
		return micResult;
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
					addToVerList(tc,2,1);

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
			addToVerList(tc,2,1);

		return false;	
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

}
