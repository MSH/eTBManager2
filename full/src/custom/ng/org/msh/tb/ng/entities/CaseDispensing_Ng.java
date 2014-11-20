package org.msh.tb.ng.entities;

import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="casedispensing_ng")
public class CaseDispensing_Ng implements Serializable {
	private static final long serialVersionUID = -4617800123206697915L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;
	
	@ManyToOne
	@JoinColumn(name="CASE_ID")
	@NotNull
	private TbCaseNG tbcase;
	
	@Column(name="DISP_MONTH")
	private int month;
	
	@Column(name="DISP_YEAR")
	private int year;

	private int totalDays;

	@OneToOne
	@PrimaryKeyJoinColumn
	private CaseDispensingDays_Ng dispensingDays;

	public void updateTotalDays() {
		if (dispensingDays == null)
			new RuntimeException("No information about days dispensed in CaseInfo");
		totalDays = dispensingDays.getNumDispensingDays();
	}
	
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
	 * @return the tbcase
	 */
	public TbCaseNG getTbcase() {
		return tbcase;
	}

	/**
	 * @param tbcase the tbcase to set
	 */
	public void setTbcase(TbCaseNG tbcase) {
		this.tbcase = tbcase;
	}

	/**
	 * @return the month
	 */
	public int getMonth() {
		return month;
	}

	/**
	 * @param month the month to set
	 */
	public void setMonth(int month) {
		this.month = month;
	}

	/**
	 * @return the year
	 */
	public int getYear() {
		return year;
	}

	/**
	 * @param year the year to set
	 */
	public void setYear(int year) {
		this.year = year;
	}

	/**
	 * @return the dispensingDays
	 */
	public CaseDispensingDays_Ng getDispensingDays() {
		return dispensingDays;
	}

	/**
	 * @param dispensingDays the dispensingDays to set
	 */
	public void setDispensingDays(CaseDispensingDays_Ng dispensingDays) {
		this.dispensingDays = dispensingDays;
	}

	/**
	 * @return the totalDays
	 */
	public int getTotalDays() {
		return totalDays;
	}

	/**
	 * @param totalDays the totalDays to set
	 */
	public void setTotalDays(int totalDays) {
		this.totalDays = totalDays;
	}
}
