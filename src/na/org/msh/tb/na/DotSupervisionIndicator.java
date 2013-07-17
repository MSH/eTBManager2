package org.msh.tb.na;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.TbCase;
import org.msh.tb.indicators.core.Indicator;
import org.msh.tb.indicators.core.IndicatorTable;
import org.msh.utils.date.Period;

@Name("dotSupervisionIndicator")

public class DotSupervisionIndicator extends Indicator{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8097784775097691524L;
	
	@In(create=true) EntityManager entityManager;
	
/*	private String groupFields;
	private List<CaseDispensingNA> dispensingList;
	private List<CaseDispensingNA> dOTdispensingListG;
*/
	// identification of the table columns
	private static final Integer COL_DAYS_PRESC= 1;
	private static final Integer COL_DAYS_TAKEN = 2;
	private static final Integer COL_DAYS_NOT_TAKEN = 3;
	private static final Integer COL_DAYS_SELF_ADMIN = 4;
	private static final Integer COL_COMPL_RATE = 5;

	@Override
	protected void createIndicators() {
		// initialize the selection
		setOutputSelected(false);
		setConsolidated(false);
		setHQLSelect("select c.id, c.patient.name, c.patient.middleName, c.patient.lastName, c.patient.recordNumber, c.caseNumber, "
				+ "c.treatmentPeriod, sum(d.totalDaysDot), sum(d.totalDaysSelfAdmin), sum(d.totalDaysNotTaken)");
		setGroupFields("c.id, c.patient.name, c.patient.middleName, c.patient.lastName, c.patient.recordNumber, c.caseNumber, "
				+ "c.treatmentPeriod");
		setOrderByFields("c.patient.name, c.patient.middleName, c.patient.lastName");
		setCondition("c.treatmentPeriod.iniDate is not null");

//		dispensingList = new ArrayList<CaseDispensingNA>();
		String hql = createHQL();
		List<Object[]> lst = getQueryResult(hql);

		// initialize the columns of the table
		IndicatorTable table = getTable();
		table.addColumn(getMessage("cases.treat.presc"), COL_DAYS_PRESC);
		table.addColumn(getMessage("cases.treat.disp.taken"), COL_DAYS_TAKEN);
		table.addColumn(getMessage("cases.treat.disp.nottaken"), COL_DAYS_NOT_TAKEN);
		table.addColumn(getMessage("cases.treat.disp.selfadmin"), COL_DAYS_SELF_ADMIN);
		table.addColumn(getMessage("cases.treat.disp.dotCompletionRate"), COL_COMPL_RATE);

		// mock objects to help compose patient name and number
		TbCase tbcase = new TbCase();
		Patient pat = new Patient();
		tbcase.setPatient(pat);

		// populate table
		for (Object[] vals: lst) {
			tbcase.setId( (Integer)vals[0] );
			pat.setName((String)vals[1]);
			pat.setMiddleName((String)vals[2]);
			pat.setLastName((String)vals[3]);
			Period treatPeriod = (Period)vals[6];

			float numDays = treatPeriod.getDays();
			Long daysDot = (Long)vals[7];
			Long daysSelfAdmin = (Long)vals[8];
			Long daysNotTaken = (Long)vals[9];

			table.addRow(pat.getFullName(), tbcase.getId());
			table.addIdValue(COL_DAYS_PRESC, tbcase.getId(), numDays);
			if (daysDot != null)
				table.addIdValue(COL_DAYS_TAKEN, tbcase.getId(), daysDot.floatValue());
			if (daysSelfAdmin != null)
				table.addIdValue(COL_DAYS_SELF_ADMIN, tbcase.getId(), daysSelfAdmin.floatValue());
			if (daysNotTaken != null)
				table.addIdValue(COL_DAYS_NOT_TAKEN, tbcase.getId(), daysNotTaken.floatValue());
		}
		
/*		IndicatorFilters filters = getIndicatorFilters();
		List<CaseDispensingNA> dOTDispensingList =getDOTDispensingList("d.id","exists(select hu.id from TreatmentHealthUnit hu where hu.tbcase.id = d.tbcase.id)");
		List<CaseDispensingNA> finaldOTDispensingList = getDispensingListPeriod(filters.getIniMonth(), filters.getIniYear(), filters.getTbunitselection().getTbunit(),dOTDispensingList);
		
		IndicatorTable table = getTable();
			table.addColumn(getMessage("cases.treat.presc"), prescID);
			table.addColumn(getMessage("cases.treat.disp.taken"), prescID);
			table.addColumn(getMessage("cases.treat.disp.nottaken"), prescID);
			table.addColumn(getMessage("cases.treat.disp.selfadmin"), prescID);
			table.addColumn(getMessage("cases.treat.disp.dotCompletionRate"), prescID);

		CaseDispensingNA caseDispensingNA = new CaseDispensingNA();
		HashMap<String, Float> dispInfo;
		Float dispTaken;
		Float dispNotTaken;
		Float dispNotSup;
		Float presc = null;
		Float percentage = 0F;
		for(int i =0; i < finaldOTDispensingList.size(); i++){
			presc = null;
			caseDispensingNA = entityManager.find(CaseDispensingNA.class, finaldOTDispensingList.get(i).getId());
			dispInfo = getPrescribedDays(caseDispensingNA.getTbcase());
			dispTaken = (caseDispensingNA.getDispensingDays()!=null?new Float(caseDispensingNA.getDispensingDays().getNumDispensingDaysDot()):null);
			dispNotTaken = (caseDispensingNA.getDispensingDays()!=null?new Float(caseDispensingNA.getDispensingDays().getNumDispensingDaysNotTaken()):null);
			dispNotSup = (caseDispensingNA.getDispensingDays()!=null?new Float(caseDispensingNA.getDispensingDays().getNumDispensingDaysNotSupervised()):null);
			presc = (dispInfo.get("TotalPrescribed")!=null?dispInfo.get("TotalPrescribed"):0F);
				if(dispTaken!=null&&presc!=null)
				//	percentage = (dispTaken + dispNotSup)/presc*100;
					//VR : Namibia customization - changing DOT % calculation (DOT Taken/Pres)
					percentage = (dispTaken)/presc*100;
		addValue(getMessage("cases.treat.presc"), caseDispensingNA.getTbcase().getPatient().getName(), presc);
		addValue(getMessage("cases.treat.disp.taken"), caseDispensingNA.getTbcase().getPatient().getName(), dispTaken);
		addValue(getMessage("cases.treat.disp.nottaken"), caseDispensingNA.getTbcase().getPatient().getName(), dispNotTaken);
		addValue(getMessage("cases.treat.disp.selfadmin"), caseDispensingNA.getTbcase().getPatient().getName(), dispNotSup);
		addValue(getMessage("cases.treat.disp.dotCompletionRate"), caseDispensingNA.getTbcase().getPatient().getName(), percentage);
		}
*/	}
	

/*	private HashMap<String, Float> getPrescribedDays(TbCase tbcase) {
		TreatmentCalendarHome treatmentCal = new TreatmentCalendarHome(tbcase, entityManager);	
		List<PhaseInfo> phases = treatmentCal.getPhases();
		HashMap<String, Float> dispInfo = new HashMap<String, Float>();
		for (PhaseInfo phaseInfo : phases) {
			for (MonthInfo monthInfo : phaseInfo.getMonths()) {
				if((monthInfo.getMonth()==getIndicatorFilters().getIniMonth()) && (monthInfo.getYear()==getIndicatorFilters().getIniYear())){
					dispInfo.put("TotalDispensed", new Float(monthInfo.getTotalDispensed()));
					dispInfo.put("TotalPrescribed", new Float(monthInfo.getTotalPrescribed()));
					return dispInfo;
				}
			}
		}		
		return null;
	}
*/	
	/**
	 * Return the list of cases and its dispensing information of a specific month/year
	 * @param tbunit
	 * @param year 
	 * @param month 
	 * @return List of {@link CaseDispensingNA} objects
	 */
/*	public List<CaseDispensingNA> getDispensingList(Integer month, Integer year, Tbunit tbunit) {
		List<CaseDispensingNA> filteredLst = new ArrayList<CaseDispensingNA>();
		if (dispensingList == null)
			createCasesList(month, year, tbunit);
		for(CaseDispensingNA caseDisp : dispensingList){
			Period treatPeriod = caseDisp.getTbcase().getTreatmentPeriod();
			Calendar c = Calendar.getInstance();
			c.set(year, month, 1);
			java.util.Date iniDt = c.getTime();
			c.set(Calendar.DAY_OF_MONTH, c.getMaximum(Calendar.DAY_OF_MONTH));
			java.util.Date endDt = c.getTime();
			Period indicatorPeriod = new Period(iniDt, endDt);
			if(treatPeriod.contains(indicatorPeriod)||treatPeriod.isIntersected(indicatorPeriod))
				filteredLst.add(caseDisp);
		}
		dispensingList = filteredLst;
		return dispensingList;
	}
*/

	/**
	 * Return the list of cases and its dispensing information of a specific month/year
	 * @param tbunit
	 * @param year 
	 * @param month 
	 * @return List of {@link CaseDispensingNA} objects
	 */
/*	public List<CaseDispensingNA> getDispensingListPeriod(Integer month, Integer year, Tbunit tbunit, List<CaseDispensingNA> dOTcasedispensingNA) {
		List<CaseDispensingNA> filteredLst = new ArrayList<CaseDispensingNA>();
		CaseDispensingNA caseDispensingNA = new CaseDispensingNA();
		for(int i =0; i < dOTcasedispensingNA.size(); i++){
			caseDispensingNA = entityManager.find(CaseDispensingNA.class, dOTcasedispensingNA.get(i));
			Period treatPeriod = caseDispensingNA.getTbcase().getTreatmentPeriod();
			Calendar c = Calendar.getInstance();
			c.set(year, month, 1);
			java.util.Date iniDt = c.getTime();
			c.set(Calendar.DAY_OF_MONTH, c.getMaximum(Calendar.DAY_OF_MONTH));
			java.util.Date endDt = c.getTime();
			Period indicatorPeriod = new Period(iniDt, endDt);
			if(treatPeriod.contains(indicatorPeriod)||treatPeriod.isIntersected(indicatorPeriod))
				filteredLst.add(caseDispensingNA);
		}
		dispensingList = filteredLst;
		return dispensingList;
	}
*/
	/**
	 * Create the list of {@link CaseDispensingNA} objects
	 */
/*	protected void createCasesList(Integer month, Integer year, Tbunit tbunit) {
		String tbunitFilter="";
		
		if(tbunit!=null)
			tbunitFilter = "and hu.tbunit.id = " + tbunit.getId() + ") ";
		else
			tbunitFilter = "";
			
			Integer tbunitId = tbunit.getId();
		dispensingList = new ArrayList<CaseDispensingNA>();
		
		// load the dispensing days information recorded in the database
		dispensingList = entityManager.createQuery("from CaseDispensingNA d join fetch d.tbcase c " +
				"join fetch c.patient p " +
				"where exists(select hu.id from TreatmentHealthUnit hu where hu.tbcase.id = c.id and hu.period.endDate = c.treatmentPeriod.endDate " +
				tbunitFilter+
				"and d.month = " + (month + 1) + " and d.year = " + year +
				" and c.state = " + CaseState.ONTREATMENT.ordinal())
				.getResultList();
		
		// load the remaining cases on treatment
		List<TbCase> lstcases = entityManager.createQuery("from TbCase c join fetch c.patient p " +
				"where exists(select hu.id from TreatmentHealthUnit hu where hu.tbcase.id = c.id and hu.period.endDate = c.treatmentPeriod.endDate " +
				"and hu.tbunit.id = " + tbunitId + ") " + 
				" and c.state = " + CaseState.ONTREATMENT.ordinal())
				.getResultList();
		for (TbCase tbcase: lstcases) {
			System.err.println("tbcase id == "+tbcase.getId()+"patient name == "+tbcase.getPatient().getName());
			CaseDispensingNA info = findCaseDispensing(tbcase);
			if (info == null) {
				CaseDispensingNA caseDispensing = new CaseDispensingNA();
				caseDispensing.setTbcase(tbcase);
				caseDispensing.setMonth(month + 1);
				caseDispensing.setYear(year);				
				dispensingList.add(caseDispensing);
			}
		}

		if (dispensingList.size() > 0) {
			// sort the list of cases by patient name
			for (CaseDispensingNA casedisp : dispensingList) {
				System.out.println("Case ID"+casedisp.getTbcase().getDisplayCaseNumber());
				System.out.println("Name "+casedisp.getTbcase().getPatient().getName());
				
			}
			Collections.sort(dispensingList, new Comparator<CaseDispensingNA>() {
				public int compare(CaseDispensingNA o1, CaseDispensingNA o2) {
					String name1 = o1.getTbcase().getPatient().getName();
					String name2 = o2.getTbcase().getPatient().getName();
					return name1.compareToIgnoreCase(name2);
				}				
			});
		}
	}
*/

	/**
	 * Create the list of {@link CaseDispensingNA} objects
	 */
/*	public List<CaseDispensingNA> getDOTDispensingList(String fields, String condition) {
		setOutputSelected(false);
		setGroupFields(fields);
		setCondition(condition);
		dispensingList = new ArrayList<CaseDispensingNA>();
	//	dispensingList = createQuery().getResultList();
		String hql = createHQL();
		dispensingList = entityManager.createQuery(hql).getResultList();
		return dispensingList;
	}
	
	
	protected CaseDispensingNA findCaseDispensing(TbCase tbcase) {
		for (CaseDispensingNA info: dispensingList) {
			if (info!=null && info.getTbcase().getId().equals(tbcase.getId())) {
				return info;
			}
		}
		return null;
	}
*/
	
/*	@Override
	protected String getHQLSelect() {
		return "select d.id";
	}	
*/	
	/** {@inheritDoc}
	 */
	@Override
	protected String getHQLFrom() {
		return "from CaseDispensingNA d";
	}

	/** {@inheritDoc}
	 */
	@Override
	protected String getHQLJoin() {
		String joinStr = "inner join d.tbcase c ";
		setConsolidated(true);
		String s = super.getHQLJoin();
		setConsolidated(false);

		if (s != null)
			joinStr = joinStr.concat(s);
		return joinStr;
	}
	
	@Override
	protected String getPeriodCondition() {
		String s = "c.treatmentPeriod.iniDate >= #{indicatorFilters.iniDate}";
		return s;
	}
	
}
