package org.msh.tb.cases.treatment;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.TreatmentMonitoring;
import org.msh.tb.entities.enums.RegimenPhase;
import org.msh.tb.entities.enums.TreatmentDayOption;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Support displaying of the treatment calendar
 * @author Ricardo Memoria
 *
 */
@Name("treatmentCalendarHome")
public class TreatmentCalendarHome {

	@In(required=true)
	protected CaseHome caseHome;
	@In(create=true)
	protected EntityManager entityManager;

	private List<PhaseInfo> phases;
	private List<MonthInfo> months;
	private List<Integer> days;
	
	/**
	 * Return information about a regimen phase 
	 * @return list of {@link PhaseInfo} instances
	 */
	public List<PhaseInfo> getPhases() {
		if (phases == null)
			createMonthList();
		return phases;
	}


	/**
	 * Calculates the initial and final date of the intensive and continuous phase
	 */
	protected void createPhasesList() {
		TbCase tbcase = caseHome.getInstance();

		phases = new ArrayList<PhaseInfo>();

		Period p = tbcase.getIntensivePhasePeriod();
		if (p != null)
			addPhase(p, RegimenPhase.INTENSIVE);
		
		p = tbcase.getContinuousPhasePeriod();
		if (p != null)
			addPhase(p, RegimenPhase.CONTINUOUS);			
	}

	
	private PhaseInfo addPhase(Period period, RegimenPhase regimenPhase) {
		PhaseInfo phaseInfo = null;
		if (phases.size() > 0)
			phaseInfo = phases.get(phases.size() - 1);
		
		if ((phaseInfo == null) || (!phaseInfo.getRegimenPhase().equals(regimenPhase))) {
			phaseInfo = new PhaseInfo();
			phaseInfo.setPeriod(period);
			phaseInfo.setRegimenPhase(regimenPhase);
			phases.add(phaseInfo);
		}
		else {
			phaseInfo.getPeriod().setEndDate(period.getEndDate());
		}
		
		return phaseInfo;
	}
	
	
	/**
	 * Create list of months for the intensive phase and continuous phase 
	 */
	protected void createMonthList() {
		months = new ArrayList<MonthInfo>();

		createPhasesList();

		for (PhaseInfo pi: phases) {
			createMonths(pi.getMonths(), pi.getPeriod());
		}
		
		mountPlanned();
		mountDispensing();
	}


	/**
	 * Create the list of months for the list according to the initial and final date
	 * @param lst
	 * @param ini
	 * @param end
	 */
	protected void createMonths(List<MonthInfo> lst, Period period) {
		Calendar c = Calendar.getInstance();
		c.setTime(period.getIniDate());
		int iniMonth = c.get(Calendar.MONTH);
		int iniYear = c.get(Calendar.YEAR);
		
		c.setTime(period.getEndDate());
		int endMonth = c.get(Calendar.MONTH);
		int endYear = c.get(Calendar.YEAR);

		// calc the difference in number of months
		int dx = endMonth - iniMonth + ((endYear - iniYear) * 12);

		lst.clear();

		for (int i = 0; i <= dx; i++) {
			MonthInfo mi = new MonthInfo();
			mi.setMonth(iniMonth);
			mi.setYear(iniYear);
			mi.refreshDays(period.getIniDate(), period.getEndDate());
			
			lst.add(mi);
			months.add(mi);
			
			iniMonth++;
			if (iniMonth >= 12) {
				iniMonth = 0;
				iniYear++;
			}
		}
	}

	
	public List<Integer> getDays() {
		if (days == null) {
			days = new ArrayList<Integer>();
			for (int i=1; i<=31; i++)
				days.add(i);
		}
		return days;
	}

	
	/**
	 * Returns information about a specific day in the calendar
	 * @param dt - Date to retrieve information from
	 * @return DayInfo object
	 */
	public DayInfo getDay(Date dt) {
		Calendar c = Calendar.getInstance();
		c.setTime(dt);
		
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);

		MonthInfo mi = getMonth(month, year, day);
		if (mi != null) {
			DayInfo di = mi.getDays().get(day - 1);
			return di;
		}
		
		return null;
	}


	
	/**
	 * Returns information about a month of the calendar
	 * @param month - month to retrieve information
	 * @param year - year to retrieve information
	 * @return MonthInfo instance, or null if there is no information
	 */
	public MonthInfo getMonth(int month, int year, int day) {
		for (MonthInfo mi: months) {
			if ((mi.getMonth() == month) && (mi.getYear() == year)) {
				if ((day >= mi.getDayIni()) && (day <= mi.getDayEnd())) 
					return mi;
			}
		}
		return null;
	}
	
	/**
	 * Fill calendar with planned prescription days 
	 */
	protected void mountPlanned() {
		TbCase tbcase = caseHome.getInstance();
		Date inidt = tbcase.getTreatmentPeriod().getIniDate();
		Date enddt = tbcase.getTreatmentPeriod().getEndDate();
		
		if ((inidt == null) || (enddt == null))
			return;

		while (!inidt.after(enddt)) {
			DayInfo dayInfo = getDay(inidt);
			if (dayInfo != null) {
				if (tbcase.isDayPrescription(inidt))
					dayInfo.setPrescribed(true);
			}
			inidt = DateUtils.incDays(inidt, 1);
		}
	}
	
	/**
	 * Fill calendar with dispensing information
	 */
	protected void mountDispensing() {
		List<TreatmentMonitoring> lst = entityManager.createQuery("from TreatmentMonitoring c  " +
				"where c.tbcase.id = :id ")
				.setParameter("id", caseHome.getId())
				.getResultList();

		for (TreatmentMonitoring tm: lst) {
			int month = tm.getMonth();
			int year = tm.getYear();
			int numdays = DateUtils.daysInAMonth(year, month - 1);

			for (int day = 1; day <= numdays; day++) {
				TreatmentDayOption val = tm.getDay(day);
				if (val != TreatmentDayOption.NOT_TAKEN) {
					Calendar c = Calendar.getInstance();
					c.set(year, month - 1, day);
					DayInfo di = getDay(c.getTime());
					if (di != null) {
						di.setValue(val);
//						di.setDispensed(true);
						di.setTreated(true);
					}
				}
			}
		}		
	}
	
	
	/**
	 * Months of a regimen phase
	 * @author Ricardo Memoria
	 *
	 */
	public class PhaseInfo {
		private List<MonthInfo> months = new ArrayList<MonthInfo>();
		private RegimenPhase regimenPhase;
		private Period period = new Period();

		/**
		 * Is intensive phase ?
		 * @return true if is intensive phase
		 */
		public boolean isIntensivePhase() {
			return regimenPhase.equals(RegimenPhase.INTENSIVE); 
		}

		/**
		 * Is continuous phase ?
		 * @return true if so
		 */
		public boolean isContinuousPhase() {
			return regimenPhase.equals(RegimenPhase.CONTINUOUS); 
		}
		
		public List<MonthInfo> getMonths() {
			return months;
		}

		public Period getPeriod() {
			return period;
		}

		public void setPeriod(Period period) {
			this.period = period;
		}

		public RegimenPhase getRegimenPhase() {
			return regimenPhase;
		}

		public void setRegimenPhase(RegimenPhase regimenPhase) {
			this.regimenPhase = regimenPhase;
		}
		
		/**
		 * Count total days prescribed to patients for all phase 
		 * @return total days prescribed to patients for all phase 
		 */
		public int getTotalPresc(){
			int res=0;
			for (int i = 0; i < getMonths().size(); i++) {
				res += getMonths().get(i).getTotalPrescribed();
			}
			return res;
		}
		
		/**
		 * Count total medicine dispensing to patients for all phase 
		 * @return total medicine dispensing to patients for all phase 
		 */
		public int getTotalDisp(){
			int res=0;
			for (int i = 0; i < getMonths().size(); i++) {
				res += getMonths().get(i).getTotalDispensed();
			}
			return res;
		}
	}
}
