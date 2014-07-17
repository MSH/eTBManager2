package org.msh.tb.forecasting;

import org.jboss.seam.Component;
import org.jboss.seam.international.LocaleSelector;
import org.msh.tb.entities.Forecasting;
import org.msh.tb.entities.ForecastingMedicine;
import org.msh.tb.entities.ForecastingResult;
import org.msh.utils.date.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Store the results in a table format ready for displaying 
 * @author Ricardo Memoria
 *
 */
public class ForecastingResultTable {

	/**
	 * Reference to the forecasting object
	 */
	private Forecasting forecasting;

	/**
	 * Columns of the table
	 */
	private List<String> columns;
	
	/**
	 * Rows of the table
	 */
	private List<Row> rows;


	public void refresh() {
		columns = null;
		rows = null;
	}
	
	/**
	 * Create the table based on the results of the forecasting
	 */
	protected void createTable() {
		Forecasting forecasting = (Forecasting)Component.getInstance("forecasting");
		if (forecasting.getReferenceDate() == null)
			return;
		
		// create rows
		rows = new ArrayList<Row>();
		int numMonths = forecasting.getNumMonths();
		for (ForecastingMedicine med: forecasting.getMedicines()) {
			Row row = new Row();
			row.setMedicine(med);
			row.setResults(new ArrayList<ForecastingResult>());
			rows.add(row);
			for (int i = 0; i <= numMonths; i++) {
				ForecastingResult res = forecasting.findResult(med.getMedicine(), i);
				if (res == null) {
					res = new ForecastingResult();
					res.setMonthIndex(i);
					res.setMedicine(med.getMedicine());
					forecasting.getResults().add(res);
				}
//					throw new RuntimeException("Forecasting result was not found for " + med.getMedicine().toString() + " and monthIndex=" + Integer.toString(i));
				row.getResults().add(res);
			}
		}
		
		// create columns
		columns = new ArrayList<String>();
		Locale locale = LocaleSelector.instance().getLocale();
		SimpleDateFormat df = new SimpleDateFormat("MMM-yyy", locale);

		Date refDate = forecasting.getReferenceDate();
		for (int i = 0; i <= numMonths; i++) {
			Date dt = DateUtils.incMonths(refDate, i);
			String s = df.format(dt);
			columns.add(s);
		}
	}
	
	/**
	 * Row of the table
	 * @author Ricardo Memoria
	 *
	 */
	public class Row {
		private List<ForecastingResult> results;
		private ForecastingMedicine medicine;
	
		public List<ForecastingResult> getResults() {
			return results;
		}
		public void setResults(List<ForecastingResult> results) {
			this.results = results;
		}
		public ForecastingMedicine getMedicine() {
			return medicine;
		}
		public void setMedicine(ForecastingMedicine medicine) {
			this.medicine = medicine;
		}
	}

	/**
	 * Return forecasting in use
	 * @return instance of {@link Forecasting} class
	 */
	public Forecasting getForecasting() {
		return forecasting;
	}


	/**
	 * Return list of the columns ready for displaying
	 * @return List of String containing the title of each column
	 */
	public List<String> getColumns() {
		if (columns == null)
			createTable();
		return columns;
	}


	/**
	 * Return list of rows of the table
	 * @return List of {@link Row} objects
	 */
	public List<Row> getRows() {
		if (rows == null)
			createTable();
		return rows;
	}
}
