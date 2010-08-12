package org.msh.tb.indicators.core;


/**
 * Render the chart series to a javascript string representation in JSON format to be used in charts 
 * @author Ricardo Memoria
 *
 */
public class JSONDataRender {

	public static final int CHART_HBAR = 1;
	public static final int CHART_PIE = 2;

	private String title;
	private String topTitle;
	private int chartType = 1;
	private IndicatorSeries series;

	private String vals;
	private String labels;
	private int max;
	private int steps;
	
	/**
	 * Render the data generating a JSON string to be used in javascript client side
	 * @return
	 */
	public String render() {
		String s;
		switch (chartType) {
		case CHART_HBAR: s = renderHBar();
			break;
		case CHART_PIE: s = renderPie();
			break;
		default: s = "";
		}
		
		return "{" + s + "}";
	}

	
	/**
	 * Render data for a horizontal bar chart
	 * @return
	 */
	protected String renderHBar() {
		renderValues();

		String s = wrapElement("text", title) + "," +
		wrapElement("style", "{font-size: 18px; font-family: Verdana; text-align: center;}"); 

		s = wrapNode("title", s);

		// values
		String values = "{" +
			wrapElement("type", getChartId()) + "," +
			wrapElement("tip", "#val#") + "," + 
			wrapElement("colour", "#3D829D") +  "," +
			wrapElement("text", topTitle) + "," +
			wrapInteger("font-size", 12) + "," +
			wrapArray("values", vals) + "}";

		s = s + ", " + wrapArray("elements", values);

		// x axis
		s = s + "," + wrapNode("x_axis", 
				wrapElement("grid-colour", "#c0c0c0") + "," + 
				wrapInteger("min", 0) + "," +
				wrapElement("max", Integer.toString(max)) + "," +
				wrapElement("offset", "false") + "," +
				wrapElement("steps", Integer.toString(steps)));

		// y axis
		s = s + "," + wrapNode("y_axis", 
				wrapElement("grid-colour", "#c0c0c0") + "," + 
				wrapElement("offset", "false") + "," +
				wrapArray("labels", labels));

		s = s + "," + 
			wrapElement("bg_colour", "#fafcfa") + "," + 
			wrapNode("tooltip", 
			wrapInteger("mouse", 1));
		return s;
	}

	
	/**
	 * Render data for a pie chart
	 * @return
	 */
	protected String renderPie() {
		labels = "";
		for (IndicatorItem item: series.getItems()) {
			if (item.getValue() != 0) {
				if (!labels.isEmpty()) {
					labels = labels + ",";
				}
				labels = labels + "{" + 
					wrapInteger("value", item.getValue()) + "," +
					wrapElement("label", item.getKey()) + "}";				
			}
		}
		
		String s = wrapElement("text", title) + "," +
		wrapElement("style", "{font-size: 18px; font-family: Verdana; text-align: center;}"); 

		s = wrapNode("title", s);

		// values
		String values = "{" +
			wrapElement("type", getChartId()) + "," +
			wrapArray("colours", "\"#d01f3c\",\"#356aa0\",\"#C79810\",\"#759E14\",\"#BD0DBF\",\"#16E3D2\",\"#7B1BBE\",\"#8E9E9F\"") +  "," +
			wrapElement("alpha", "0.6") + "," +
			wrapInteger("border", 2) + "," +
			wrapInteger("animate", 1) + "," +
			wrapInteger("gradient-fill", 1) + "," +
			wrapInteger("start-angle", 35) + "," +
			wrapArray("values", labels) + "}";

		s = s + ", " + wrapArray("elements", values);
		return s;		
	}
	
	
	protected void renderValues() {
		labels = "";
		vals = "";
		max = 0;
		steps = 1;

		for (IndicatorItem item: series.getItems()) {
			if (!labels.isEmpty()) {
				labels = "," + labels;
				vals = vals + ",";
			}
			labels = "\"" + item.getKey() + "\"" + labels;
			vals += "{\"right\":" + item.getValue() + "}";
			
			if (item.getValue() > max)
				max = item.getValue();
		}
		if (max > 10)
			steps = Math.round(max / 5) + 1;
	}
	
	protected String wrapNode(String element, String nodeValues) {
		return wrap(element) + ":{" + nodeValues + "}";
	}
	
	protected String wrapArray(String element, String array) {
		return wrap(element) + ":[" + array + "]";
	}
	
	protected String wrapInteger(String element, int value) {
		return wrap(element) + ":" + Integer.toString(value);
	}
	
	protected String getChartId() {
		switch (chartType) {
		case 1: return "hbar";
		case 2: return "pie";
		default: return "bar";
		}
	}
	
	public String wrap(String elem) {
		return "\"" + elem + "\"";  
	}
	
	public String wrapElement(String elem, String value) {
		return wrap(elem) + ":" + wrap(value);
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
	 * @return the chartType
	 */
	public int getChartType() {
		return chartType;
	}
	/**
	 * @param chartType the chartType to set
	 */
	public void setChartType(int chartType) {
		this.chartType = chartType;
	}
	/**
	 * @return the series
	 */
	public IndicatorSeries getSeries() {
		return series;
	}
	/**
	 * @param series the series to set
	 */
	public void setSeries(IndicatorSeries series) {
		this.series = series;
	}
	/**
	 * @return the topTitle
	 */
	public String getTopTitle() {
		return topTitle;
	}

	/**
	 * @param topTitle the topTitle to set
	 */
	public void setTopTitle(String topTitle) {
		this.topTitle = topTitle;
	}
}
