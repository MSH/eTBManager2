package org.msh.tb.client.reports.chart;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.Widget;

/**
 * Display the chart
 * @author Ricardo Memoria
 *
 */
public class ChartView extends Widget {

	private static int idCounter = 0;
	
	/**
	 * The selected chart
	 */
	private ChartType selectedChart;

	/**
	 * List of values that compose the chart
	 */
	private List<ChartSeries> series = new ArrayList<ChartSeries>();

	/**
	 * Set the chart title
	 */
	private String title;
	
	/**
	 * The subtitle of the chart
	 */
	private String subTitle;
	
	/**
	 * The title of the x-axis
	 */
	private String xAxisTitle;
	
	/**
	 * The title of the y-axis
	 */
	private String yAxisTitle;

	/**
	 * Represents the java-script object of the char
	 */
	private JavaScriptObject chartObject;
	
	/**
	 * List of labels defined in the series
	 */
	private List<String> labels = new ArrayList<String>();

	
	/**
	 * Show or hide the chart main title
	 */
	private boolean showTitle = true;
	
	private Element divElement;

	/**
	 * Default constructor
	 */
	public ChartView() {
		super();
		divElement = Document.get().createDivElement();
		setElement(divElement);
	}
	
	/**
	 * Clear the values of the chart 
	 */
	public void clear() {
		series.clear();
		labels.clear();
		if (chartObject != null) {
			destroyChart(chartObject);
			chartObject = null;
		}
	}

	
	/**
	 * Add a new series to the chart
	 * @param seriesTitle
	 * @return
	 */
	public ChartSeries addSeries(String seriesTitle) {
		ChartSeries ser = new ChartSeries(this);
		ser.setTitle(seriesTitle);
		series.add(ser);
		return ser;
	}

	
	/**
	 * Add a label included in a series. If the label already
	 * exists, so it's not included 
	 * @param label
	 */
	protected void addLabel(String label) {
		if (!labels.contains(label))
			labels.add(label);
	}


	/**
	 * Create the data to be updated in the chart
	 * @return
	 */
	protected JSONObject createChartData() {
		JSONObject chart = new JSONObject();
		JSONObject chartNode = new JSONObject();

//		chartNode.put("renderTo", new JSONString("divchart"));
		if (divElement.getId().isEmpty()) {
			idCounter++;
			divElement.setId("divchart" + Integer.toString(idCounter));
		}
		chartNode.put("renderTo", new JSONString(divElement.getId()));
		chartNode.put("type", new JSONString(selectedChart.getName()));
		chart.put("chart", chartNode);

		// add the title, if available
		if ((title != null) && (showTitle)) {
			chart.put("title", createTitle(title));
		}
		else {
			chart.put("title", new JSONString(""));
		}

		// add the subtitle, if available
		if (subTitle != null) {
			chart.put("subtitle", createTitle(subTitle));
		}

		JSONObject xAxis = new JSONObject();
		chart.put("xAxis", xAxis);
		
		JSONArray series = new JSONArray();
		chart.put("series", series);
		
		if (selectedChart == ChartType.CHART_PIE)
			 createPieChart(chart);
		else createChart(chart);
		
		return chart;
	}

	
	/**
	 * Generate a JSON object containing the title to be included in the chart
	 * @param title is the title to be displayed
	 * @return instance of the {@link JSONObject} to be included in the title node of the chart
	 */
	protected JSONObject createTitle(String title) {
		return jsonData("{text:'" + title + "'}");
	}
	

	/**
	 * Create a new JSON object from a string
	 * @param json the JSON representation in string format
	 * @return instance of {@link JSONObject} containing the JSON data
	 */
	public JSONObject jsonData(String json) {
		return new JSONObject(parseJson(json));
	}


	/**
	 * Create the chart data for general charts but pie (the pie chart is handled
	 * in a different way)
	 * @param xAxis is a {@link JSONObject} to contain the data of the x-axis
	 * @param jSeries is a {@link JSONObject} to contain the series values
	 */
	private void createChart(JSONObject chart) {
		JSONObject xAxis = (JSONObject)chart.get("xAxis");
		JSONArray jSeries = (JSONArray)chart.get("series");

		int numLegends = 0;
		int numItems = 0;

		if (series.size() == 1) {
			ChartSeries ser = series.get(0);
			// create the categories to be displayed in the x-axis
			JSONArray cats = new JSONArray();
			int index = 0;
			for (SerieValue value: ser.getValues()) {
				cats.set(index++, new JSONString(value.getLabel()));
			}
			xAxis.put("categories", cats);
			numItems = index;

			// update the xAxis
			if (xAxisTitle != null)
				xAxis.put("title", createTitle(xAxisTitle));
			
			numLegends = 1;
			
			jSeries.set(0, createSerie(ser.getTitle(), ser.getValues(), true));
		}
		else {
			// create the categories to be displayed in the x-axis
			JSONArray cats = new JSONArray();
			int index = 0;
			for (ChartSeries serie: series) {
				cats.set(index++, new JSONString(serie.getTitle()));
			}
			xAxis.put("categories", cats);
			numItems = series.size();

			// create the values
			index = 0;
			for (String label: labels) {
				List<SerieValue> values = new ArrayList<SerieValue>();
				for (ChartSeries serie: series) {
					values.add(serie.getValueByLabel(label));
				}
				jSeries.set(index++, createSerie(label, values, false));
			}
			
			numLegends = index;
		}

		if ((numItems > 10) && (selectedChart != ChartType.CHART_BAR)) {
			xAxis.put("labels", jsonData("{rotation:-45,align:'right'}"));
		}

		int height;
		if (selectedChart == ChartType.CHART_BAR) {
			height = (numLegends * numItems * 3) + (numItems * 20) + 100;
			if (height < 400)
				height = 400;
		}
		else height = 400;
		
		Element elem = divElement;// DOM.getElementById("divchart");
		elem.getStyle().setHeight(height, Unit.PX); 
		
		// add legend
		String floatleg = numLegends > 3? "false":"true";
		String s = "{layout:'vertical',align:'right'," +
	            "verticalAlign:'top',x:0,y:1,floating:" + floatleg + ",borderWidth:1,backgroundColor:'#FFFFFF',shadow:true}";
		JavaScriptObject object = parseJson(s);
		chart.put("legend", new JSONObject(object));

		// add an yAxis displaying the total of cases
		if (yAxisTitle != null) { 
			s = "{title:{text:'" + yAxisTitle + "'}}";
			chart.put("yAxis", new JSONObject(parseJson(s)));
		}
	}

	
	/**
	 * Create a series of a high-chart by its name and a list of values 
	 * @param name
	 * @param values
	 * @return
	 */
	protected JSONObject createSerie(String name, List<SerieValue> values, boolean includeNames) {
		JSONObject jserie = new JSONObject();
		JSONArray data = new JSONArray();
		int index = 0;
		for (SerieValue val: values) {
			if ((val.getValue() > 0) || ((selectedChart != ChartType.CHART_PIE) && (val.getValue() == 0))) {
				if (includeNames) {
					JSONArray jval = new JSONArray();
					jval.set(0, new JSONString(val.getLabel()));
					jval.set(1, new JSONNumber(val.getValue()));
					data.set(index++, jval);
				}
				else data.set(index++, new JSONNumber(val.getValue()));
			}
		}
		jserie.put("name", new JSONString(name));
		jserie.put("data", data);
		return jserie;
	}


	/**
	 * Create the values of the chart and its legends to a pie chart
	 * @param xAxis
	 */
	private void createPieChart(JSONObject chart) {
		// get the chart standard colors
		JsArrayString colors = getChartColors();

		JSONArray jseries = (JSONArray)chart.get("series");
		// there is just one series, so it's a single pie
		if (series.size() == 1) {
			ChartSeries serie = series.get(0);
			jseries.set(0, createSerie(serie.getTitle(), serie.getValues(), true));
		}
		else {
			// it's a donut, so there are 2 main series
			String vals = "";
			// create the values of the inner pie
			for (ChartSeries serie: series) {
				double val = serie.getValueSum();
				if (val > 0) {
					if (!vals.isEmpty())
						vals += ",";
					vals += "['" + serie.getTitle() + "'," + serie.getValueSum() + "]";
				}
			}
			String s = "{name:'" + xAxisTitle + "',data:[" + vals + "],size:'60%'," +
					"dataLabels:{color:'white',distance:-30}" +
					"}";
			jseries.set(0, jsonData(s));

			// create the values of the outer pie
			s = "";
			int index = 0;
			int numColors = colors.length();
			for (ChartSeries serie: series) {
				if (serie.getValueSum() > 0.0) {
					for (SerieValue val: serie.getValues()) {
						if (val.getValue() > 0) {
							if (!s.isEmpty())
								s += ",";
							s += "{name:'" + val.getLabel() + "',y:" + val.getValue() + ",color:'" + colors.get(index % numColors) + "'}";
						}
					}
					index++;
				}
			}
			s = "{name:'" + yAxisTitle + "',data:[" + s + "],size:'80%',innerSize:'60%'}";
			jseries.set(1, new JSONObject(parseJson(s)));
		}
	}


	/**
	 * Update the chart applying the selected type and values
	 */
	public void update() {
		// generate chart data
		JSONObject chart = createChartData();

		// call native java-script to update the chart
		chartObject = createChart(chart.getJavaScriptObject());
	}

	/**
	 * Native java script function to create the chart and display its values
	 * @param options
	 */
	public static native JavaScriptObject createChart(JavaScriptObject options) /*-{
		return new $wnd.Highcharts.Chart(options);
	}-*/;

	
	/**
	 * Parse an string object to a json object 
	 * @param jsonstr
	 * @return
	 */
	protected static native JavaScriptObject parseJson(String jsonstr) /*-{
		return eval('(' + jsonstr + ')');
	}-*/;


	/**
	 * Get the standard colors of the highchart object
	 * @return
	 */
	protected static native JsArrayString getChartColors() /*-{
		return $wnd.Highcharts.getOptions().colors;
	}-*/;
	
	
	/**
	 * Destroy the chart, clearing the chart are
	 * @param obj
	 */
	protected static native void destroyChart(JavaScriptObject obj) /*-{
		obj.destroy();
	}-*/;

	/**
	 * @return the selectedChart
	 */
	public ChartType getSelectedChart() {
		return selectedChart;
	}


	/**
	 * @param selectedChart the selectedChart to set
	 */
	public void setSelectedChart(ChartType selectedChart) {
		this.selectedChart = selectedChart;
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
	 * @return the subTitle
	 */
	public String getSubTitle() {
		return subTitle;
	}


	/**
	 * @param subTitle the subTitle to set
	 */
	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}


	/**
	 * @return the xAxisTitle
	 */
	public String getxAxisTitle() {
		return xAxisTitle;
	}


	/**
	 * @param xAxisTitle the xAxisTitle to set
	 */
	public void setxAxisTitle(String xAxisTitle) {
		this.xAxisTitle = xAxisTitle;
	}


	/**
	 * @return the yAxisTitle
	 */
	public String getyAxisTitle() {
		return yAxisTitle;
	}


	/**
	 * @param yAxisTitle the yAxisTitle to set
	 */
	public void setyAxisTitle(String yAxisTitle) {
		this.yAxisTitle = yAxisTitle;
	}

	/**
	 * @return the showTitle
	 */
	public boolean isShowTitle() {
		return showTitle;
	}

	/**
	 * @param showTitle the showTitle to set
	 */
	public void setShowTitle(boolean showTitle) {
		this.showTitle = showTitle;
	}
}
