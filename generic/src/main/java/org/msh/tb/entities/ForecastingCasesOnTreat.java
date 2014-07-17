package org.msh.tb.entities;

import org.msh.utils.date.DateUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


/**
 * Hold the quantity of cases on treatment to be used as input for the forecasting calculations
 * @author Ricardo Memoria
 *
 */
@Entity
@Table(name="forecastingcasesontreat")
public class ForecastingCasesOnTreat implements Serializable {
	private static final long serialVersionUID = -4173150111908906789L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

	@ManyToOne
	@JoinColumn(name="FORECASTING_ID")
	private Forecasting forecasting;

	@ManyToOne
	@JoinColumn(name="REGIMEN_ID")
	private Regimen regimen;
	
	/**
	 * Month index based on the reference date of the forecasting, starting in 0 (present) to negative numbers (months
	 * in the past)    
	 */
	private int monthIndex;
	
	/**
	 * Number of cases for the monthIndex and regimen selected
	 */
	private Integer numCases;

	
	/**
	 * Return the current month based on the monthIndex and reference date of the forecasting
	 * @return
	 */
	public int getMonth() {
		Date dt = forecasting.getReferenceDate();

		if ((forecasting == null) || (dt == null))
			return 0;

		return DateUtils.monthOf( DateUtils.incMonths(dt, monthIndex) );
	}


	/**
	 * Return the current year based on the monthIndex and reference date of the forecasting
	 * @return
	 */
	public int getYear() {
		Date dt = forecasting.getReferenceDate();

		if ((forecasting == null) || (dt == null))
			return 0;

		return DateUtils.yearOf( DateUtils.incYears(dt, monthIndex) );
	}
	

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Forecasting getForecasting() {
		return forecasting;
	}

	public void setForecasting(Forecasting forecasting) {
		this.forecasting = forecasting;
	}

	public Regimen getRegimen() {
		return regimen;
	}

	public void setRegimen(Regimen regimen) {
		this.regimen = regimen;
	}

	public int getMonthIndex() {
		return monthIndex;
	}

	public void setMonthIndex(int monthIndex) {
		this.monthIndex = monthIndex;
	}

	public Integer getNumCases() {
		return numCases;
	}

	public void setNumCases(Integer numCases) {
		this.numCases = numCases;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + monthIndex;
		result = prime * result + ((regimen == null) ? 0 : regimen.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ForecastingCasesOnTreat other = (ForecastingCasesOnTreat) obj;
		if (monthIndex != other.monthIndex)
			return false;
		if (regimen == null) {
			if (other.regimen != null)
				return false;
		} else if (!regimen.equals(other.regimen))
			return false;
		return true;
	}

}
