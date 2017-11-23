package org.msh.utils.reportgen.highchart;

import java.util.ArrayList;
import java.util.List;


public class ChartOptions {

	private Chart chart;
	private Title title;
	private XAxis xAxis;
	private YAxis yAxis;
	private List<Series> series;
	
	public class Chart {
		private String renderTo;
		private ChartType type;
		private Integer width;
		private boolean shadow;
		private boolean showAxes;
		/**
		 * @return the renderTo
		 */
		public String getRenderTo() {
			return renderTo;
		}
		/**
		 * @param renderTo the renderTo to set
		 */
		public void setRenderTo(String renderTo) {
			this.renderTo = renderTo;
		}
		/**
		 * @return the type
		 */
		public ChartType getType() {
			return type;
		}
		/**
		 * @param type the type to set
		 */
		public void setType(ChartType type) {
			this.type = type;
		}
		/**
		 * @return the width
		 */
		public Integer getWidth() {
			return width;
		}
		/**
		 * @param width the width to set
		 */
		public void setWidth(Integer width) {
			this.width = width;
		}
		/**
		 * @return the shadow
		 */
		public boolean isShadow() {
			return shadow;
		}
		/**
		 * @param shadow the shadow to set
		 */
		public void setShadow(boolean shadow) {
			this.shadow = shadow;
		}
		/**
		 * @return the showAxes
		 */
		public boolean isShowAxes() {
			return showAxes;
		}
		/**
		 * @param showAxes the showAxes to set
		 */
		public void setShowAxes(boolean showAxes) {
			this.showAxes = showAxes;
		}
	}
	
	public class Title {
		private String text;
		private Integer x;
		private Integer y;
		private String style;
		/**
		 * @return the text
		 */
		public String getText() {
			return text;
		}
		/**
		 * @param text the text to set
		 */
		public void setText(String text) {
			this.text = text;
		}
		/**
		 * @return the x
		 */
		public Integer getX() {
			return x;
		}
		/**
		 * @param x the x to set
		 */
		public void setX(Integer x) {
			this.x = x;
		}
		/**
		 * @return the y
		 */
		public Integer getY() {
			return y;
		}
		/**
		 * @param y the y to set
		 */
		public void setY(Integer y) {
			this.y = y;
		}
		/**
		 * @return the style
		 */
		public String getStyle() {
			return style;
		}
		/**
		 * @param style the style to set
		 */
		public void setStyle(String style) {
			this.style = style;
		}
	}
	
	public class XAxis {
		private AxisTitle title;
		private List<String> categories;

		/**
		 * @return the title
		 */
		public AxisTitle getTitle() {
			if (title == null)
				title = new AxisTitle();
			return title;
		}
		/**
		 * Return the list of categories
		 * @return
		 */
		public List<String> getCategories() {
			if (categories == null)
				categories = new ArrayList<String>();
			return categories;
		}		
	}
	
	/**
	 * @author Ricardo Memoria
	 *
	 */
	public class YAxis {
		private AxisTitle title;
		private List<String> categories;

		/**
		 * @return the title
		 */
		public AxisTitle getTitle() {
			if (title == null)
				title = new AxisTitle();
			return title;
		}
		
		/**
		 * Return the list of categories
		 * @return
		 */
		public List<String> getCategories() {
			if (categories == null)
				categories = new ArrayList<String>();
			return categories;
		}
	}
	
	/**
	 * @author Ricardo Memoria
	 *
	 */
	public class AxisTitle {
		private Integer margin;
		private Integer rotation;
		private String text;
		private String style;

		/**
		 * @return the margin
		 */
		public Integer getMargin() {
			return margin;
		}
		/**
		 * @param margin the margin to set
		 */
		public void setMargin(Integer margin) {
			this.margin = margin;
		}
		/**
		 * @return the rotation
		 */
		public Integer getRotation() {
			return rotation;
		}
		/**
		 * @param rotation the rotation to set
		 */
		public void setRotation(Integer rotation) {
			this.rotation = rotation;
		}
		/**
		 * @return the text
		 */
		public String getText() {
			return text;
		}
		/**
		 * @param text the text to set
		 */
		public void setText(String text) {
			this.text = text;
		}
		/**
		 * @return the style
		 */
		public String getStyle() {
			return style;
		}
		/**
		 * @param style the style to set
		 */
		public void setStyle(String style) {
			this.style = style;
		}
	}
	
	/**
	 * @return the chart
	 */
	public Chart getChart() {
		if (chart == null)
			chart = new Chart();
		return chart;
	}

	/**
	 * @return the title
	 */
	public Title getTitle() {
		if (title == null)
			title = new Title();
		return title;
	}

	/**
	 * @return the xAxis
	 */
	public XAxis getxAxis() {
		if (xAxis == null)
			xAxis = new XAxis();
		return xAxis;
	}

	/**
	 * @return the yAxis
	 */
	public YAxis getyAxis() {
		if (yAxis == null)
			yAxis = new YAxis();
		return yAxis;
	}

	/**
	 * @return the series
	 */
	public List<Series> getSeries() {
		if (series == null)
			series = new ArrayList<Series>();
		return series;
	}
	
	/**
	 * Add a new series to the chart
	 * @return
	 */
	public Series addSeries() {
		Series series = new Series();
		getSeries().add(series);
		return series;
	}
}
