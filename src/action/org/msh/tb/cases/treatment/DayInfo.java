package org.msh.tb.cases.treatment;

/**
 * Stores information about an specific treatment day
 * @author Ricardo Lima
 *
 */
public class DayInfo {

	private boolean prescribed;
	private boolean dispensed;
	private boolean notused;
	private boolean treated;
	
	private MonthInfo monthInfo;

	

	public DayInfo(MonthInfo monthInfo) {
		super();
		this.monthInfo = monthInfo;
	}

	
	/**
	 * Returns if the day is in use or not (example, out of the treatment period)
	 * @return the notused
	 */
	public boolean isNotused() {
		return notused;
	}

	/**
	 * @param notused the notused to set
	 */
	public void setNotused(boolean notused) {
		this.notused = notused;
	}
	
	/**
	 * Returns if the day is planned for dispensing
	 * @return true - if it's planned prescribing
	 */
	public boolean isPrescribed() {
		return prescribed;
	}
	
	public void setPrescribed(boolean prescribed) {
		this.prescribed = prescribed;
	}
	
	/**
	 * Returns if there was medicine dispensing in the day
	 * @return true - if it was dispensed medicine
	 */
	public boolean isDispensed() {
		return dispensed;
	}
	public void setDispensed(boolean dispensed) {
		this.dispensed = dispensed;
		if (dispensed)
			 monthInfo.setDispensingDays(monthInfo.getDispensingDays() + 1);
		else monthInfo.setDispensingDays(monthInfo.getDispensingDays() - 1);
	}

	/**
	 * @param treated the treated to set
	 */
	public void setTreated(boolean treated) {
		this.treated = treated;
	}

	/**
	 * Returns if this day already is inside a dispensing period
	 * @return the treated
	 */
	public boolean isTreated() {
		return treated;
	}


	/**
	 * @return the monthInfo
	 */
	public MonthInfo getMonthInfo() {
		return monthInfo;
	}
}
