package org.msh.tb.cases.dispensing;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.msh.etbm.commons.transactionlog.Operation;
import org.msh.etbm.commons.transactionlog.mapping.LogInfo;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.CaseSideEffect;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.TreatmentMonitoring;
import org.msh.tb.entities.enums.RoleAction;
import org.msh.tb.entities.enums.TreatmentDayOption;

import javax.persistence.EntityManager;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Handle registering dispensing days of a TB case
 * @author Ricardo Memoria
 *
 */
@Name("caseDispensingHome")
@Scope(ScopeType.CONVERSATION)
@LogInfo(roleName="CASE_TREAT", entityClass=TreatmentMonitoring.class)
public class CaseDispensingHome extends EntityHomeEx<TreatmentMonitoring>{

	@In(required=true)
	protected CaseHome caseHome;
	@In
	protected EntityManager entityManager;
	
	protected Integer month;
	protected Integer year;

	protected CaseDispensingInfo caseDispensingInfo;

	@Override
	public TreatmentMonitoring getInstance() {
		return caseDispensingInfo.getTreatmentMonitoring();
	}

	/**
	 * Save dispensing for the month/year of the selected case
	 * @return "dispensing-saved" if it was successfully saved
	 */
	@Transactional
	public String saveDispensing() {
		if (caseDispensingInfo == null)
			return "error";
		
		caseDispensingInfo.updateDispensingDays();

		TreatmentMonitoring treatMonitoring = caseDispensingInfo.getTreatmentMonitoring();
		entityManager.persist(treatMonitoring);
		entityManager.flush();

		//To save transactionlog on the tbcase that is beeing edited, so it will be sync with desktop
		super.initTransactionLog(RoleAction.NEW);
		super.getActionTX().setEntity(caseDispensingInfo.getTreatmentMonitoring().getTbcase());
		super.saveTransactionLog();

		return "dispensing-saved";
	}


	/**
	 * Return list of weeks
	 * @return
	 */
	public List<WeekInfo> getWeeks() {
		if (caseDispensingInfo == null)
			createDispensingInfo();
	
		return (caseDispensingInfo == null? null: caseDispensingInfo.getWeeks());
	}


	
	/**
	 * Load dispensing data and create week structure
	 */
	protected void createDispensingInfo() {
		if ((month == null) || (year == null))
			return;
		
		List<TreatmentMonitoring> lst = entityManager.createQuery("from TreatmentMonitoring d join fetch d.tbcase c " +
				"join fetch c.patient p " +
				"where c.id = #{caseHome.id} " +
				"and d.month = " + (month + 1) + " and d.year = " + year)
				.getResultList();

		if (lst.size() > 0)
			caseDispensingInfo = new CaseDispensingInfo(lst.get(0));
		else {
			TreatmentMonitoring treatMonitoring = new TreatmentMonitoring();
			treatMonitoring.setTbcase(caseHome.getInstance());
			treatMonitoring.setMonth(month + 1);
			treatMonitoring.setYear(year);
			
			caseDispensingInfo = new CaseDispensingInfo(treatMonitoring);
		}
	}

	/**
	 * Checks if medicine dispensing can be entered. Ceases future date entries.
	 */
	public boolean isDispensable(Integer day) {			
		Date currDate = getCurrentDate();
		Date dispDate = getDispDate(year,month,day);
		
		if(dispDate.before(currDate))
			return true;
		if(dispDate.compareTo(currDate)==0)
			return true;
		else 
			return false;		
	}
	
	/**
	 * fetches current date
	 */
	public Date getCurrentDate(){
		Calendar cal = Calendar.getInstance();
		String date_format = "yyyyMMdd";
		SimpleDateFormat sdf = new SimpleDateFormat(date_format);
		Date date = null;
		try {
			 date = (Date)sdf.parse(sdf.format(cal.getTime()));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}
	
	public Date getDispDate(int yr, int mon, int d){
		String month = null;
		String day = null;
		if((mon+1)/10==0){
			month="0".concat(Integer.toString(mon+1));
		}
		else
			month=Integer.toString(mon+1);
		if((d/10==0))
			day="0".concat(Integer.toString(d));
		else
			day=Integer.toString(d);
		String strDate = Integer.toString(yr).concat(month).concat(day);
		String date_format = "yyyyMMdd";
		SimpleDateFormat sdf = new SimpleDateFormat(date_format);
		Date date = null;
		try {
			date = (Date)sdf.parse(strDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}

	
	/**
	 * Return the options of each day in the calendar
	 * @return array of {@link TreatmentDayOption}
	 */
	public TreatmentDayOption[] getDayOptions() {
		return TreatmentDayOption.values();
	}
	
	/**
	 * @return the month
	 */
	public Integer getMonth() {
		return month;
	}
	/**
	 * @param month the month to set
	 */
	public void setMonth(Integer month) {
		this.month = month;
	}
	/**
	 * @return the year
	 */
	public Integer getYear() {
		return year;
	}
	/**
	 * @param year the year to set
	 */
	public void setYear(Integer year) {
		this.year = year;
	}

	@Override
	public String getLogEntityClass() {
		return TbCase.class.getSimpleName();
	}

	@Override
	protected Integer getLogEntityId() {
		return getInstance().getId();
	}

}
