package org.msh.mdrtb.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.msh.utils.date.DateUtils;


@Entity
public class MedicineDispensing implements Serializable {
	private static final long serialVersionUID = 808211986483933780L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="UNIT_ID")
	private Tbunit tbunit;
	
	@Temporal(TemporalType.DATE)
	private Date iniDate;
	
	@Temporal(TemporalType.DATE)
	private Date endDate;
	
	@OneToMany(mappedBy="dispensing", cascade={CascadeType.ALL})
	private List<MedicineDispensingItem> items = new ArrayList<MedicineDispensingItem>();


	public Integer getMonth() {
		return (endDate == null? null: DateUtils.monthOf(endDate));
	}
	
	public Integer getYear() {
		return (endDate == null? null: DateUtils.yearOf(endDate));
	}

	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the items
	 */
	public List<MedicineDispensingItem> getItems() {
		return items;
	}

	/**
	 * @param items the items to set
	 */
	public void setItems(List<MedicineDispensingItem> items) {
		this.items = items;
	}

	/**
	 * @return the tbunit
	 */
	public Tbunit getTbunit() {
		return tbunit;
	}

	/**
	 * @param tbunit the tbunit to set
	 */
	public void setTbunit(Tbunit tbunit) {
		this.tbunit = tbunit;
	}

	/**
	 * @return the iniDate
	 */
	public Date getIniDate() {
		return iniDate;
	}

	/**
	 * @param iniDate the iniDate to set
	 */
	public void setIniDate(Date iniDate) {
		this.iniDate = iniDate;
	}

	/**
	 * @return the endDate
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
}
