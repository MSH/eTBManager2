package org.msh.tb.na.entities;

import org.hibernate.validator.NotNull;
import org.msh.tb.entities.TbCase;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="casedispensingna")
public class CaseDispensingNA implements Serializable {
	private static final long serialVersionUID = -4617800123206697915L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;
	
	@ManyToOne
	@JoinColumn(name="CASE_ID")
	@NotNull
	private TbCase tbcase;
	
	@Column(name="DISP_MONTH")
	private int month;
	
	@Column(name="DISP_YEAR")
	private int year;

	private int totalDaysDot;
	
	private int totalDaysSelfAdmin;

	private int totalDaysNotTaken;

	@OneToOne
	@PrimaryKeyJoinColumn
	private CaseDispensingDaysNA dispensingDays;

	public void updateTotalDays() {
		if (dispensingDays == null)
			new RuntimeException("No information about days dispensed in CaseInfo");
		totalDaysDot = dispensingDays.getNumDispensingDaysDot();
		totalDaysSelfAdmin = dispensingDays.getNumDispensingDaysNotSupervised();
		totalDaysNotTaken = dispensingDays.getNumDispensingDaysNotTaken();
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
	public TbCase getTbcase() {
		return tbcase;
	}

	/**
	 * @param tbcase the tbcase to set
	 */
	public void setTbcase(TbCase tbcase) {
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
	public CaseDispensingDaysNA getDispensingDays() {
		return dispensingDays;
	}

	/**
	 * @param dispensingDays the dispensingDays to set
	 */
	public void setDispensingDays(CaseDispensingDaysNA dispensingDays) {
		this.dispensingDays = dispensingDays;
	}

	/**
	 * @return the totalDaysDot
	 */
	public int getTotalDaysDot() {
		return totalDaysDot;
	}

	/**
	 * @param totalDaysDot the totalDaysDot to set
	 */
	public void setTotalDaysDot(int totalDaysDot) {
		this.totalDaysDot = totalDaysDot;
	}

	/**
	 * @return the totalDaysSelfAdmin
	 */
	public int getTotalDaysSelfAdmin() {
		return totalDaysSelfAdmin;
	}

	/**
	 * @param totalDaysSelfAdmin the totalDaysSelfAdmin to set
	 */
	public void setTotalDaysSelfAdmin(int totalDaysSelfAdmin) {
		this.totalDaysSelfAdmin = totalDaysSelfAdmin;
	}

	/**
	 * @return the totalDaysNotTaken
	 */
	public int getTotalDaysNotTaken() {
		return totalDaysNotTaken;
	}

	/**
	 * @param totalDaysNotTaken the totalDaysNotTaken to set
	 */
	public void setTotalDaysNotTaken(int totalDaysNotTaken) {
		this.totalDaysNotTaken = totalDaysNotTaken;
	}
}
