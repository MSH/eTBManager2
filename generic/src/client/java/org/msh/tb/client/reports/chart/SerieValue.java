package org.msh.tb.client.reports.chart;

public class SerieValue {
	private String label;
	private double value;
	public SerieValue(String label, double value) {
		super();
		this.label = label;
		this.value = value;
	}
	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * @return the value
	 */
	public double getValue() {
		return value;
	}
}

