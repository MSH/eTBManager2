package org.msh.tb.vi;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.treatment.DayInfo;
import org.msh.tb.cases.treatment.TreatmentCalendarHome;
import org.msh.tb.cases.treatment.TreatmentCalendarHome.PhaseInfo;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.TreatmentMonitoring;
import org.msh.tb.entities.enums.RegimenPhase;
import org.msh.tb.entities.enums.TreatmentDayOption;
import org.msh.tb.kh.entities.TreatmentMonitoringKH;
import org.msh.tb.vi.entities.TreatmentMonitoringVI;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;


/**
 * Support displaying of the treatment calendar for cambodia
 * @author Utkarsh Srivastava
 *
 */
@Name("treatmentCalendarHomeVI")
public class TreatmentCalendarHomeVI extends TreatmentCalendarHome{
	
	protected List<MonthInfo> months;
	private List<PhaseInfo> phases1;


	/**
	 * Return information about a regimen phase 
	 * @return list of {@link PhaseInfo} instances
	 */
	public List<PhaseInfo> getPhases1() {
		if (phases1 == null)
			createMonthList();
		return phases1;
	}	
	
	private PhaseInfo addPhase(Period period, RegimenPhase regimenPhase) {
		PhaseInfo phaseInfo = null;
		if (phases1.size() > 0)
			phaseInfo = phases1.get(phases1.size() - 1);
		
		if ((phaseInfo == null) || (!phaseInfo.getRegimenPhase().equals(regimenPhase))) {
			phaseInfo = new PhaseInfo();
			phaseInfo.setPeriod(period);
			phaseInfo.setRegimenPhase(regimenPhase);
			phases1.add(phaseInfo);
		}
		else {
			phaseInfo.getPeriod().setEndDate(period.getEndDate());
		}
		
		return phaseInfo;
	}	


	/**
	 * Fill calendar with dispensing information
	 */
	@Override
	protected void mountDispensing() {
		List<TreatmentMonitoringVI> lst = entityManager.createQuery("from TreatmentMonitoringVI c  " +
				"where c.tbcase.id = :id ")
				.setParameter("id", super.caseHome.getId())
				.getResultList();

		for (TreatmentMonitoringVI tm: lst) {
			int month 	= tm.getMonth();
			int year 	= tm.getYear();
			int weight 	= tm.getWeight();
			int numdays = DateUtils.daysInAMonth(year, month - 1);
			
			for(MonthInfo mi : months) {
				if(mi.getYear()==year && mi.getMonth()==month-1) {
					mi.setWeight(weight);
				}
			}

			
			
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
	 * Create the list of months for the list according to the initial and final date
	 * @param lst
	 * @param ini
	 * @param end
	 */
	protected void createMonths1(List<MonthInfo> lst, Period period) {
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

	/**
	 * Create list of months for the intensive phase and continuous phase 
	 */
	@Override
	protected void createMonthList() {
		months = new ArrayList<MonthInfo>();

		createPhasesList();

		for (PhaseInfo pi: phases1) {
			createMonths1(pi.getMonths(), pi.getPeriod());
		}
		
		mountPlanned();
		mountDispensing();
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
	 * Calculates the initial and final date of the intensive and continuous phase
	 */
	protected void createPhasesList() {
		TbCase tbcase = caseHome.getInstance();

		phases1 = new ArrayList<PhaseInfo>();

		Period p = tbcase.getIntensivePhasePeriod();
		if (p != null)
			addPhase(p, RegimenPhase.INTENSIVE);
		
		p = tbcase.getContinuousPhasePeriod();
		if (p != null)
			addPhase(p, RegimenPhase.CONTINUOUS);			
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
