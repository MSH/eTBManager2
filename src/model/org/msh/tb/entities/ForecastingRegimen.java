package org.msh.tb.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.validator.NotNull;

@Entity
public class ForecastingRegimen implements Serializable {
	private static final long serialVersionUID = 1810509102521927840L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;
	
	@ManyToOne
	@JoinColumn(name="FORECASTING_ID")
	private Forecasting forecasting;
	
	@ManyToOne
	@JoinColumn(name="REGIMEN_ID")
	@NotNull
	private Regimen regimen;

	@Transient
	private List<ForecastingRegimenResult> results = new ArrayList<ForecastingRegimenResult>();

	/**
	 * Percentage of new cases for this regimen
	 */
	private float percNewCases;
	

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Regimen getRegimen() {
		return regimen;
	}

	public void setRegimen(Regimen regimen) {
		this.regimen = regimen;
	}

	public float getPercNewCases() {
		return percNewCases;
	}

	public void setPercNewCases(float percNewCases) {
		this.percNewCases = percNewCases;
	}

	/**
	 * @param forecasting the forecasting to set
	 */
	public void setForecasting(Forecasting forecasting) {
		this.forecasting = forecasting;
	}

	/**
	 * @return the forecasting
	 */
	public Forecasting getForecasting() {
		return forecasting;
	}


	public ForecastingRegimenResult findResultByMonthIndex(int monthIndex) {
		for (ForecastingRegimenResult res: getResults()) {
			if (res.getMonthIndex() == monthIndex)
				return res;
		}
		return null;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		ForecastingRegimen other = (ForecastingRegimen) obj;
		if (regimen == null) {
			if (other.regimen != null)
				return false;
		} else if (!regimen.equals(other.regimen))
			return false;
		return true;
	}

	/**
	 * @return the results
	 */
	public List<ForecastingRegimenResult> getResults() {
		return results;
	}

}
