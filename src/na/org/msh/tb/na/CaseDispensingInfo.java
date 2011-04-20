package org.msh.tb.na;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


import org.msh.tb.na.entities.CaseDispensingDaysNA;
import org.msh.tb.na.entities.CaseDispensingNA;
import org.msh.utils.date.DateUtils;



/**
 * Keep temporary information during editing about a case and its dispensing days 
 * @author Utkarsh Srivastava
 *
 */
public class CaseDispensingInfo {

	private CaseDispensingNA caseDispensing;
	private CaseDispensingDaysNA dispensingDays;
	private List<org.msh.tb.na.WeekInfo> weeks = new ArrayList<org.msh.tb.na.WeekInfo>();
	private List<org.msh.tb.na.DayInfo> days = new ArrayList<DayInfo>();
	private org.msh.tb.na.WeekInfo selectedWeek;


	public CaseDispensingInfo(CaseDispensingNA caseDispensing) {
		super();
		this.caseDispensing = caseDispensing;
		dispensingDays = caseDispensing.getDispensingDays();
		if (dispensingDays == null)
			dispensingDays = new CaseDispensingDaysNA();
		mountDispensingDays();
	}
	
	/**
	 * Generate the list of treatment days with information about the dispensing days
	 */
	protected void mountDispensingDays() {
		int month = caseDispensing.getMonth() - 1;
		int year = caseDispensing.getYear();
		
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.MONTH, month);
		c.set(Calendar.YEAR, year);
		c.set(Calendar.DAY_OF_MONTH, 1);
		int weekday = c.get(Calendar.DAY_OF_WEEK);
		int numdays = DateUtils.daysInAMonth(year, month);

		Date iniDate = caseDispensing.getTbcase().getTreatmentPeriod().getIniDate();
		Date endDate = caseDispensing.getTbcase().getTreatmentPeriod().getEndDate();
		
		int day = 0;
		int count = 1;
		int weeknumber = 1;
		weeks = new ArrayList<WeekInfo>();
		while (true) {
			if (day + 1 > numdays)
				break;
			
			org.msh.tb.na.WeekInfo weekInfo = new WeekInfo();
			weekInfo.setWeek(weeknumber);
			weeks.add(weekInfo);

			for (int i = 1; i <= 7; i++) {
				if (count >= weekday) {
					day++;
					if (weekInfo.getIniDay() == 0)
						weekInfo.setIniDay(day);
				}
				
				if (day > numdays)
					break;
				org.msh.tb.na.DayInfo dd = new DayInfo();
				dd.setWeekDay(i);

				if ((day > 0) && (day <= numdays)) {
					dd.setSelected((dispensingDays.getDay(day)));
					dd.setDay(day);					
					weekInfo.setEndDay(day);

					c.set(Calendar.DAY_OF_MONTH, day);
					dd.setAvailable((!c.getTime().before(iniDate)) && (!c.getTime().after(endDate)));
				}
				else dd.setAvailable(false);

				weekInfo.getDays().add(dd);
				days.add(dd);
				count++;
			}
			weeknumber++;
		}
	}


	/**
	 * Update the dispensing days to the object dispensingDays of class {@link CaseDispensingDaysNA}, 
	 * making it ready to be persisted in the database
	 */
	public void updateDispensingDays() {
		if (weeks == null)
			return;
		
		for (WeekInfo weekInfo: weeks) {
			for (DayInfo day: weekInfo.getDays()) {
				if (day.isAvailable())
					dispensingDays.setDay(day.getDay(), day.getSelected());
			}
		}
		caseDispensing.setDispensingDays(dispensingDays);
		caseDispensing.updateTotalDays();
	}


	
	/**
	 * @return the caseDispensing
	 */
	public CaseDispensingNA getCaseDispensing() {
		return caseDispensing;
	}
	/**
	 * @param caseDispensing the caseDispensing to set
	 */
	public void setCaseDispensing(CaseDispensingNA caseDispensing) {
		this.caseDispensing = caseDispensing;
	}


	/**
	 * @return the weeks
	 */
	public List<WeekInfo> getWeeks() {
		return weeks;
	}

	/**
	 * @return the selectedWeek
	 */
	public WeekInfo getSelectedWeek() {
		return selectedWeek;
	}

	/**
	 * @param selectedWeek the selectedWeek to set
	 */
	public void setSelectedWeek(WeekInfo selectedWeek) {
		this.selectedWeek = selectedWeek;
	}

	/**
	 * @return the days
	 */
	public List<DayInfo> getDays() {
		return days;
	}

}
