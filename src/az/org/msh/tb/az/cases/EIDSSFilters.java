package org.msh.tb.az.cases;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.faces.model.SelectItem;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.tbunits.TBUnitSelection;
import org.msh.utils.date.Period;

@Name("eidssFilters")
@Scope(ScopeType.PAGE)
public class EIDSSFilters {
	private String name = null;
	private Period regDate;
	private Period inDate;
	private Period sysDate;
	private Integer yearBirth;
	private String id = null;
	private String notifUnit = null;
	private String address = null;
	private TBUnitSelection tbunit;
	private boolean search = false;
	
	//private Period diagDate;
	private Integer bindMonth;
	private Integer bindYear;

	public List<SelectItem> getYears() {
		List<SelectItem> lst = new ArrayList<SelectItem>();
		Calendar c = Calendar.getInstance();
		int ano = c.get(Calendar.YEAR);

		SelectItem si = new SelectItem();
		si.setLabel("-");
		lst.add(si);

		for (int i = ano; i >= ano - 120; i--) {
			SelectItem it = new SelectItem();
			it.setLabel(Integer.toString(i));
			it.setValue(i);
			lst.add(it);
		}

		return lst;
	}

	public boolean someFieldNotEmpty(){
		boolean res = false;
		if (address!=null && !"".equals(address))
			res = true;
		if (id!=null && !"".equals(id))
			res = true;
		if (name!=null && !"".equals(name))
			res = true;
		if (notifUnit!=null && !"".equals(notifUnit))
			res = true;
		if (yearBirth!=null)
			res = true;
		if (inDate!=null)
			if (inDate.getIniDate()!=null || inDate.getEndDate()!=null)
				res = true;
		if (regDate!=null)
			if (regDate.getIniDate()!=null || regDate.getEndDate()!=null)
				res = true;
		if (sysDate!=null)
			if (sysDate.getIniDate()!=null || sysDate.getEndDate()!=null)
				res = true;
		if (tbunit!=null)
			if (tbunit.getAdminUnit() != null)
				res = true;
		return res;
	} 

	//===========GETTERS & SETTERS============
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the regDate
	 */
	public Period getRegDate() {
		if (regDate==null)
			regDate = new Period();
		return regDate;
	}
	/**
	 * @return the inDate
	 */
	public Period getInDate() {
		if (inDate==null)
			inDate = new Period();
		return inDate;
	}
	/**
	 * @return the sysDate
	 */
	public Period getSysDate() {
		if (sysDate==null)
			sysDate = new Period();
		return sysDate;
	}
	/**
	 * @return the yearBirth
	 */
	public Integer getYearBirth() {
		return yearBirth;
	}
	/**
	 * @param yearBirth the yearBirth to set
	 */
	public void setYearBirth(Integer yearBirth) {
		this.yearBirth = yearBirth;
	}
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the notifUnit
	 */
	public String getNotifUnit() {
		return notifUnit;
	}
	/**
	 * @param notifUnit the notifUnit to set
	 */
	public void setNotifUnit(String notifUnit) {
		this.notifUnit = notifUnit;
	}
	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}
	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}
	/**
	 * @return the tbunit
	 */
	public TBUnitSelection getTbunit() {
		if (tbunit==null)
			tbunit = new TBUnitSelection("unitid");
		return tbunit;
	}
	/**
	 * @param tbunit the tbunit to set
	 */
	public void setTbunit(TBUnitSelection tbunit) {
		this.tbunit = tbunit;
	}

	/**
	 * @param search the search to set
	 */
	public void setSearch(boolean search) {
		if (search==false) {
			this.search = search;
			return;
		}
		if (someFieldNotEmpty())
			this.search = search;
		else
			this.search = false;
	}
	
	/**
	 * @return the search
	 */
	public boolean isSearch() {
		return search;
	}

	public void cancelSearch(){
		setSearch(false);
	}
	
	public Integer getBindMonth() {
		return bindMonth;
	}

	public void setBindMonth(Integer bindMonth) {
		this.bindMonth = bindMonth;
	}

	public Integer getBindYear() {
		return bindYear;
	}

	public void setBindYear(Integer bindYear) {
		this.bindYear = bindYear;
	}
}
