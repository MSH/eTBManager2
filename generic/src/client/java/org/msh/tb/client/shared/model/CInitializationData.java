package org.msh.tb.client.shared.model;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Data sent from the server in order to initialize the report in the client
 * side with dynamic data, like variables, filters, etc.
 * 
 * @author Ricardo Memoria
 *
 */
public class CInitializationData implements IsSerializable {

	/**
	 * List of variables and filters
	 */
	private ArrayList<CGroup> groups;
	
	/**
	 * The server current date
	 */
	private Date currentDate;
	
	/**
	 * List of available reports
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
	 * @return the reports
	 */
	public ArrayList<CReport> getReports() {
		return reports;
	}

	/**
	 * @param reports the reports to set
	 */
	public void setReports(ArrayList<CReport> reports) {
		this.reports = reports;
	}


}
