package org.msh.tb.entities;

import org.msh.utils.date.DateUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


/**
 * Hold the quantity of new cases to be used in the forecasting calculations
 * @author Ricardo Memoria
 *
 */
@Entity
@Table(name="forecastingnewcases")
public class ForecastingNewCases implements Serializable {
	private static final long serialVersionUID = -2647549548670640152L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

	@ManyToOne
	@JoinColumn(name="FORECASTING_ID")
	private Forecasting forecasting;
	
	/**
	 * Number of new cases
	 */
	private int numNewCases;
	
	/**
	 * Month of forecasting, starting in 0 (month of reference date in forecasting) and moving up to positive numbers 
	 * indicating months in the future
	 */
	private int monthIndex;

	
	public Date getIniDate() {
		return DateUtils.incMonths( forecasting.getReferenceDate(), monthIndex );
	}
	
	public int getMonth() {
		return DateUtils.monthOf( DateUtils.incMonths( forecasting.getReferenceDate(), monthIndex ) );
	}
	
	public int getYear() {
		return DateUtils.yearOf( DateUtils.incMonths( forecasting.getReferenceDate(), monthIndex ) );
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

	public int getNumNewCases() {
		return numNewCases;
	}

	public void setNumNewCases(int numNewCases) {
		this.numNewCases = numNewCases;
	}

	public int getMonthIndex() {
		return monthIndex;
	}

	public void setMonthIndex(int monthIndex) {
		this.monthIndex = monthIndex;
	}

}
