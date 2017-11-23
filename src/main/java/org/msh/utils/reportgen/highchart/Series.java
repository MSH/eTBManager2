package org.msh.utils.reportgen.highchart;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ricardo Memoria
 *
 */
public class Series {
	private List<Object> data;
	private String name;
	private ChartType type;
	private Integer xAxis;
	private Integer yAxis;

	public void addNewValue(Double value) {
		getData().add(value);
	}

	/**
	 * @return the data
	 */
	public List<Object> getData() {
		if (data == null)
			data = new ArrayList<Object>();
		return data;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
	 * @return the xAxis
	 */
	public Integer getxAxis() {
		return xAxis;
	}

	/**
	 * @param xAxis the xAxis to set
	 */
	public void setxAxis(Integer xAxis) {
		this.xAxis = xAxis;
	}

	/**
	 * @return the yAxis
	 */
	public Integer getyAxis() {
		return yAxis;
	}

	/**
	 * @param yAxis the yAxis to set
	 */
	public void setyAxis(Integer yAxis) {
		this.yAxis = yAxis;
	}
}