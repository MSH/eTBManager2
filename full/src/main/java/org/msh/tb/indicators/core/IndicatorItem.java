package org.msh.tb.indicators.core;

public class IndicatorItem {
	private String key;
	private int value;
	private IndicatorSeries series;

	/**
	 * Constructor
	 * @param series
	 */
	public IndicatorItem(IndicatorSeries series) {
		super();
		this.series = series;
	}

	public float getPerc() {
		Integer total = series.getTotal();
		if ((total == null) || (total == 0))
			 return 0;
		else return (float)value/(float)total * 100;
	}
	
	/**
	 * Return the key limited in length to be displayed in a chart
	 * @return
	 */
	public String getShortKey() {
		if ((key != null) && (key.length() > 20))
			 return key.substring(0, 19) + "...";
		else return key;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public IndicatorSeries getSeries() {
		return series;
	}
}
