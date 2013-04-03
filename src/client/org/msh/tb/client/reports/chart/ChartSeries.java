package org.msh.tb.client.reports.chart;

import java.util.ArrayList;
import java.util.List;

/**
 * Contain a list of values for a chart series
 * @author Ricardo Memoria
 *
 */
public class ChartSeries {

	private ChartView view;
	private String title;
	private List<SerieValue> values = new ArrayList<SerieValue>();
	
	public ChartSeries(ChartView view) {
		this.view = view;
	}
	
	/**
	 * Add a new value to the series
	 * @param label
	 * @param value
	 */
	public void addValue(String label, Double value) {
		if (value == null)
			value = 0.0;
		values.add(new SerieValue(label, value));
		view.addLabel(label);
	}

	
	/**
	 * Get a value by its label
	 * @param label
	 * @return an instance of the {@link SerieValue} class containing the value associated to the label
	 */
	public SerieValue getValueByLabel(String label) {
		for (SerieValue val: values)
			if (val.getLabel().equals(label))
				return val;
		return new SerieValue(label, 0.0);
	}
	
	
	/**
	 * Calculate the sum of the values in the series
	 * @return the sum of values in double type
	 */
	public double getValueSum() {
		double sum = 0;
		for (SerieValue val: values)
			sum += val.getValue();
		return sum;
	}



	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}


	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}


	/**
	 * @return the values
	 */
	public List<SerieValue> getValues() {
		return values;
	}
}
