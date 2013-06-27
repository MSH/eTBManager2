package org.msh.tb.bd;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.bd.entities.enums.Quarter;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.medicines.MedicineManStartHome;
import org.msh.tb.tbunits.TbUnitHome;

 
/**
 * Handle actions to include and remove a TB unit from the medicine module control in Bangladesh
 * @author Mauricio Santos
 *
 */
@Name("medicineManStartBDHome")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class MedicineManStartBDHome{

	@In(required=true) MedicineManStartHome medicineManStartHome;
	@In(create=true) TbUnitHome tbunitHome;
	
	private Quarter quarter;
	private Integer year;
	private List<Integer> years;
	
	/**
	 * Start the medicine management control, specifying the starting date of the control
	 * and initializing the stock in unit based on the medicines and batches selected by the user in the UI
	 * using as medManStartDate the first date of the selected quarter 
	 * @return
	 */
	public String startMedicineManagement() {
		if(quarter == null || year == null)
			return "error";
		
		GregorianCalendar date = new GregorianCalendar();
		date.set(year, quarter.getIniMonth(), quarter.getIniDay());
		medicineManStartHome.setStartDate(date.getTime());
		
		String s = medicineManStartHome.startMedicineManagement();
		
		Tbunit unit = medicineManStartHome.getUnit();
		tbunitHome.setInstance(unit);
		tbunitHome.getInstance().setLimitDateMedicineMovement(date.getTime());
		tbunitHome.persist();
		
		return s;
	}
	
	/**
	 * Load the list of years that is shown in the selection list
	 */
	private void loadYears(){
		if(years == null)
			years = new ArrayList<Integer>();
		years.add(2013);
		years.add(2012);
	}

	/**
	 * @return the quarter
	 */
	public Quarter getQuarter() {
		return quarter;
	}

	/**
	 * @param quarter the quarter to set
	 */
	public void setQuarter(Quarter quarter) {
		this.quarter = quarter;
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

	/**
	 * @return the years
	 */
	public List<Integer> getYears() {
		if(years == null || years.size() < 1)
			loadYears();
		return years;
	}

	/**
	 * @param years the years to set
	 */
	public void setYears(List<Integer> years) {
		this.years = years;
	}	
}
