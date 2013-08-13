package org.msh.tb.bd.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.msh.tb.bd.entities.enums.QuarterMonths;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Tbunit;

/**
 * Store information about the quarterly upazilla report
 * @author Mauricio Santos
 *
 */
@Entity
@Table(name="quarterlyreportdetails")
public class QuarterlyReportDetailsBD {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	
	@Column(name="quarter")
	private QuarterMonths quarterMonth;
	private Integer year;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="UNIT_ID")
	private Tbunit tbunit;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="MEDICINE_ID")
	private Medicine medicine;
		
	private Integer outOfStock;

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the quarter
	 */
	public QuarterMonths getQuarterMonth() {
		return quarterMonth;
	}

	/**
	 * @param QuarterMonths the QuarterMonths to set
	 */
	public void setQuarterMonth(QuarterMonths quarter) {
		this.quarterMonth = quarter;
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
	 * @return the medicine
	 */
	public Medicine getMedicine() {
		return medicine;
	}

	/**
	 * @param medicine the medicine to set
	 */
	public void setMedicine(Medicine medicine) {
		this.medicine = medicine;
	}

	/**
	 * @return the outOfStock
	 */
	public Integer getOutOfStock() {
		return outOfStock;
	}

	/**
	 * @param outOfStock the outOfStock to set
	 */
	public void setOutOfStock(Integer outOfStock) {
		this.outOfStock = outOfStock;
	}
	
}
