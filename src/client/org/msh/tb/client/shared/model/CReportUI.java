package org.msh.tb.client.shared.model;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

public class CReportUI implements IsSerializable {

	private ArrayList<CGroup> groups;

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


}
