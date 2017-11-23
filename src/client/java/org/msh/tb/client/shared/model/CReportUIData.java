package org.msh.tb.client.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.Date;

/**
 * Data sent from the server in order to initialize the report in the client
 * side with dynamic data, like variables, filters, etc.
 * 
 * @author Ricardo Memoria
 *
 */
public class CReportUIData implements IsSerializable {

	/**
	 * List of variables and filters
	 */
	private ArrayList<CGroup> groups;
	
	/**
	 * The server current date
	 */
	private Date currentDate;
	
	/**
	 * List of available org.msh.reports
	 */
	private ArrayList<CReport> reports;

	/**
	 * @return the groups
	 */
	public ArrayList<CGroup> getGroups() {
		return groups;
	}

	/**
	 * @param groups the groups to set
	 */
	public void setGroups(ArrayList<CGroup> groups) {
		this.groups = groups;
	}

	/**
	 * @return the currentDate
	 */
	public Date getCurrentDate() {
		return currentDate;
	}

	/**
	 * @param currentDate the currentDate to set
	 */
	public void setCurrentDate(Date currentDate) {
		this.currentDate = currentDate;
	}

	/**
	 * @return the org.msh.reports
	 */
	public ArrayList<CReport> getReports() {
		return reports;
	}

	/**
	 * @param reports the org.msh.reports to set
	 */
	public void setReports(ArrayList<CReport> reports) {
		this.reports = reports;
	}


}
