package org.msh.tb.forecasting;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.jboss.seam.international.LocaleSelector;
import org.msh.tb.entities.Forecasting;
import org.msh.tb.entities.ForecastingCasesOnTreat;
import org.msh.tb.entities.ForecastingRegimen;
import org.msh.utils.date.DateUtils;


/**
 * Creates a table view for displaying information about number of cases that started 
 * treatment in past months
 * @author Ricardo Memoria
 *
 */
public class CasesRegimenTable {
	
	/**
	 * List of months in the format mm-yyyy ready for displaying 
	 * representing the columns of the table
	 */
	private List<String> months;
	
	/**
	 * Reference to the forecasting object 
	 */
	private Forecasting forecasting;
	
	/**
	 * Rows of the table
	 */
	private List<ItemRow> rows; 

	
	/**
	 * Initial month index used to generate the columns. 
	 * The first month is 0 and it goes down to negative values
	 */
	private int monthIndexIni;
	
	/**
	 * Set the number of months to be displayed
	 */
	private int numberOfMonths = 12;


	/**
	 * Constructor of the class
	 * @param forecasting
	 */
	public CasesRegimenTable(Forecasting forecasting) {
		super();
		this.forecasting = forecasting;
	}


	/**
	 * Move to the next period 
	 */
	public void moveNextPeriod() {
		monthIndexIni += numberOfMonths;
		if (monthIndexIni > 0) 
			monthIndexIni = 0;
		rows = null;
		months = null;
	}


	/**
	 * Move to the previous period
	 */
	public void movePreviousPeriod() {
		monthIndexIni -= numberOfMonths;
		rows = null;
		months = null;
	}
	
	/**
	 * Update the regimen table according to the forecasting configuration and
	 * the previous reference date, 
	 * @param oldRefDate is the previous reference date, if available, or the
	 * current reference date, if not available
	 */
	public void updateTable(Date oldRefDate) {
		rows = new ArrayList<ItemRow>();
		int betwRefDt = 0;
		if (oldRefDate != null) 
			betwRefDt = DateUtils.monthOf( DateUtils.getDatePart(forecasting.getReferenceDate()) ) - 
						DateUtils.monthOf( DateUtils.getDatePart(oldRefDate) );
		
		// remove unused instances of ForecastingCasesOnTreat objects
		int i = 0;
		while (i < forecasting.getCasesOnTreatment().size()) {
			ForecastingCasesOnTreat c = forecasting.getCasesOnTreatment().get(i);
			if (betwRefDt != 0) 
				c.setMonthIndex(c.getMonthIndex()-betwRefDt);
			if ((c.getNumCases() == null) || (c.getNumCases() <= 0) || (forecasting.findRegimen(c.getRegimen()) == null)) {
				forecasting.getCasesOnTreatment().remove(c);
			}
			else i++;
		}

		// update regimen list
		for (ForecastingRegimen reg: forecasting.getRegimens()) {
			ItemRow row = new ItemRow();
			row.setRegimen(reg);
			row.setMonths(new ArrayList<ForecastingCasesOnTreat>());
			rows.add(row);
			for (i = 1; i <= numberOfMonths; i++) {
				int index = monthIndexIni - numberOfMonths + i; 
				ForecastingCasesOnTreat c = forecasting.findCaseInfoByRegimen(reg.getRegimen(), index);
				if (c == null) {
					c = new ForecastingCasesOnTreat();
					c.setForecasting(forecasting);
					c.setMonthIndex(index);
					c.setRegimen(reg.getRegimen());
					forecasting.getCasesOnTreatment().add(c);
				}
				row.getMonths().add(c);
			}
		}
		
		Locale locale = LocaleSelector.instance().getLocale();
		SimpleDateFormat df = new SimpleDateFormat("MMM-yyy", locale);

		Date refDate = forecasting.getReferenceDate();
		months = new ArrayList<String>();
		for (i = 1; i <= numberOfMonths; i++) {
			int index = monthIndexIni - numberOfMonths + i;
			Date dt = DateUtils.incMonths(refDate, index);
			String s = df.format(dt);
			months.add(s);
		}
	}
	
	public int getMonthsBefore() {
		return (months == null? 0: months.size());
	}
	
	
	/**
	 * Represent a row of the table
	 * @author Ricardo Memoria
	 *
	 */
	public class ItemRow implements Serializable {
		private static final long serialVersionUID = 4906603502382637639L;
		
		private ForecastingRegimen regimen;
		private List<ForecastingCasesOnTreat> months;
		public ForecastingRegimen getRegimen() {
			return regimen;
		}
		public void setRegimen(ForecastingRegimen regimen) {
			this.regimen = regimen;
		}
		public List<ForecastingCasesOnTreat> getMonths() {
			return months;
		}
		public void setMonths(List<ForecastingCasesOnTreat> months) {
			this.months = months;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((regimen == null) ? 0 : regimen.hashCode());
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
			ItemRow other = (ItemRow) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (regimen == null) {
				if (other.regimen != null)
					return false;
			} else if (!regimen.equals(other.regimen))
				return false;
			return true;
		}
		private CasesRegimenTable getOuterType() {
			return CasesRegimenTable.this;
		}
	}


	public Forecasting getForecasting() {
		return forecasting;
	}
	
	public List<String> getMonths() {
		if (months == null)
			updateTable(forecasting.getReferenceDate());
		return months;
	}
	
	public List<ItemRow> getRows() {
		if (rows == null)
			updateTable(forecasting.getReferenceDate());
		return rows;
	}

	public int getMonthIndexIni() {
		return monthIndexIni;
	}

	public void setMonthIndexIni(int monthIndexIni) {
		this.monthIndexIni = monthIndexIni;
	}

	public int getNumberOfMonths() {
		return numberOfMonths;
	}

	public void setNumberOfMonths(int numberOfMonths) {
		this.numberOfMonths = numberOfMonths;
	}
}
