package org.msh.tb.client.shared.model;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class CReportUI implements IsSerializable {

	private ArrayList<CGroup> groups;
	private Date currentDate;

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


}
