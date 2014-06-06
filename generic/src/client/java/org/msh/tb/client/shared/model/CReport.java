/**
 * 
 */
package org.msh.tb.client.shared.model;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Store information about a saved report sent from the server to the client side 
 * @author Ricardo Memoria
 *
 */
public class CReport implements IsSerializable {

	private Integer id;
	private String title;
	private boolean myReport;

	// extra information sent when detailed data is loaded 
	private ArrayList<String> columnVariables;
	private ArrayList<String> rowVariables;
	private HashMap<String, String> filters;
	private boolean published;
	private boolean dashboard;

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
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
	 * @return the myReport
	 */
	public boolean isMyReport() {
		return myReport;
	}

	/**
	 * @param myReport the myReport to set
	 */
	public void setMyReport(boolean myReport) {
		this.myReport = myReport;
	}

	/**
	 * @return the columnVariables
	 */
	public ArrayList<String> getColumnVariables() {
		return columnVariables;
	}

	/**
	 * @param columnVariables the columnVariables to set
	 */
	public void setColumnVariables(ArrayList<String> columnVariables) {
		this.columnVariables = columnVariables;
	}

	/**
	 * @return the rowVariables
	 */
	public ArrayList<String> getRowVariables() {
		return rowVariables;
	}

	/**
	 * @param rowVariables the rowVariables to set
	 */
	public void setRowVariables(ArrayList<String> rowVariables) {
		this.rowVariables = rowVariables;
	}

	/**
	 * @return the filters
	 */
	public HashMap<String, String> getFilters() {
		return filters;
	}

	/**
	 * @param filters the filters to set
	 */
	public void setFilters(HashMap<String, String> filters) {
		this.filters = filters;
	}

	/**
	 * @return the published
	 */
	public boolean isPublished() {
		return published;
	}

	/**
	 * @param published the published to set
	 */
	public void setPublished(boolean published) {
		this.published = published;
	}

	/**
	 * @return the dashboard
	 */
	public boolean isDashboard() {
		return dashboard;
	}

	/**
	 * @param dashboard the dashboard to set
	 */
	public void setDashboard(boolean dashboard) {
		this.dashboard = dashboard;
	}
}
