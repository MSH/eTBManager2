package org.msh.tb.na;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.tb.indicators.core.IndicatorFilters;
import org.msh.tb.indicators.core.IndicatorTable;
import org.msh.tb.indicators.core.IndicatorTable.TableColumn;
import org.msh.tb.na.TreatmentCalendarHome.PhaseInfo;
import org.msh.tb.na.entities.CaseDispensingNA;
import org.msh.utils.date.Period;

@Name("dotSupervisionIndicator")
public class DotSupervisionIndicator extends Indicator2D{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8097784775097691524L;
	
	@In(create=true) EntityManager entityManager;
	

	private List<CaseDispensingNA> dispensingList;
	private static final String successRateID = "SRID";  
	private static final String prescID = "PID";  
	private static final String dispID = "DID";  

	@Override
	protected void createIndicators() {
		IndicatorFilters filters = getIndicatorFilters();
		
		List<CaseDispensingNA> dispensingList = getDispensingList(filters.getIniMonth(), filters.getIniYear(), filters.getTbunitselection().getTbunit());
		
		String msg = getMessage("manag.ind.successrate");
		IndicatorTable table = getTable();
		table.addColumn(getMessage("cases.treat.presc"), prescID);
		table.addColumn(getMessage("cases.treat.disp"), dispID);
		TableColumn col = table.addColumn("DOT Completion Rate", successRateID);
//		col.setNumberPattern("#,###,##0");
		col.setHighlight(true);
		col.setRowTotal(false);
		
		HashMap<String, Float> dispInfo;
		Float disp;
		Float presc = null;
		Float percentage = 0F;
		for (Iterator<CaseDispensingNA> iterator = dispensingList.iterator(); iterator.hasNext();) {
			presc = null;
			CaseDispensingNA caseDispensingNA = (CaseDispensingNA) iterator.next();
			dispInfo = getPrescribedDays(caseDispensingNA.getTbcase());
			disp = (caseDispensingNA.getDispensingDays()!=null?new Float(caseDispensingNA.getDispensingDays().getNumDispensingDays()):null);
			presc = (dispInfo.get("TotalPrescribed")!=null?dispInfo.get("TotalPrescribed"):0F);
			if(disp!=null&&presc!=null)
				percentage = disp/presc*100;
			addValue(getMessage("cases.treat.disp"), caseDispensingNA.getTbcase().getPatient().getName(), disp);
			addValue(getMessage("cases.treat.presc"), caseDispensingNA.getTbcase().getPatient().getName(), presc);
			addValue(msg, caseDispensingNA.getTbcase().getPatient().getName(), percentage);
		}
	}
		 
	/**
	 * Return the information about the totaldispensed and total prescribed for the month and year.
	 * @param tbcase
	 * @return
	 */
	private HashMap<String, Float> getPrescribedDays(TbCase tbcase) {
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
	
	/**
	 * Return the list of cases and its dispensing information of a specific month/year
	 * @param tbunit
	 * @param year 
	 * @param month 
	 * @return List of {@link CaseDispensingNA} objects
	 */
	public List<CaseDispensingNA> getDispensingList(Integer month, Integer year, Tbunit tbunit) {
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

	/**
	 * Create the list of {@link CaseDispensingNA} objects
	 */
	protected void createCasesList(Integer month, Integer year, Tbunit tbunit) {
		Integer tbunitId = tbunit.getId();
		if ((month == null) || (year == null) || (tbunitId == null))
			return;
		
		dispensingList = new ArrayList<CaseDispensingNA>();
		
		// load the dispensing days information recorded in the database
		dispensingList = entityManager.createQuery("from CaseDispensingNA d join fetch d.tbcase c " +
				"join fetch c.patient p " +
				"where exists(select hu.id from TreatmentHealthUnit hu where hu.tbcase.id = c.id and hu.period.endDate = c.treatmentPeriod.endDate " +
				"and hu.tbunit.id = " + tbunitId + ") " +
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

	
	protected CaseDispensingNA findCaseDispensing(TbCase tbcase) {
		for (CaseDispensingNA info: dispensingList) {
			if (info!=null && info.getTbcase().getId().equals(tbcase.getId())) {
				return info;
			}
		}
		return null;
	}
}
