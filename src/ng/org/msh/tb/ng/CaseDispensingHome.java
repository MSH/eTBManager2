package org.msh.tb.ng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.ng.CaseDispensingInfo;
import org.msh.tb.ng.WeekInfo;
import org.msh.tb.ng.entities.CaseDispensing_Ng;
import org.msh.tb.ng.entities.TbCaseNG;



/**
 * Handle registering dispensing days of a TB case
 * @author Ricardo Memoria
 *
 */
@Name("caseDispensingHome_Ng")
@Scope(ScopeType.CONVERSATION)
public class CaseDispensingHome {

	@In(required=true) CaseHome caseHome;
	@In EntityManager entityManager;
	
	private Integer month;
	private Integer year;

	private CaseDispensingInfo caseDispensingInfo;

	
	/**
	 * Save dispensing for the month/year of the selected case
	 * @return "dispensing-saved" if it was successfully saved
	 */
	@Transactional
	public String saveDispensing() {
		if (caseDispensingInfo == null)
			return "error";
		
		caseDispensingInfo.updateDispensingDays();

		CaseDispensing_Ng caseDisp = caseDispensingInfo.getCaseDispensing();
		entityManager.persist(caseDisp);
			
		caseDisp.getDispensingDays().setId(caseDisp.getId());
		entityManager.persist(caseDisp.getDispensingDays());
		entityManager.flush();

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
		
		List<CaseDispensing_Ng> lst = entityManager.createQuery("from CaseDispensing_Ng d join fetch d.tbcase c " +
				"join fetch c.patient p " +
				"where c.id = #{caseHome.id} " +
				"and d.month = " + (month + 1) + " and d.year = " + year)
				.getResultList();

		if (lst.size() > 0)
			caseDispensingInfo = new CaseDispensingInfo(lst.get(0));
		else {
			CaseDispensing_Ng caseDispensing = new CaseDispensing_Ng();
			caseDispensing.setTbcase((TbCaseNG) caseHome.getInstance());
			caseDispensing.setMonth(month + 1);
			caseDispensing.setYear(year);
			
			caseDispensingInfo = new CaseDispensingInfo(caseDispensing);
		}
	}

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
	
}
