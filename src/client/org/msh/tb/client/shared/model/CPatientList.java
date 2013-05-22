package org.msh.tb.client.shared.model;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

public class CPatientList implements IsSerializable {

	private long recordCount;

	private ArrayList<CPatient> items;

	/**
	 * @return the recordCount
	 */
	public long getRecordCount() {
		return recordCount;
	}

	/**
	 * @param recordCount the recordCount to set
	 */
	public void setRecordCount(long recordCount) {
		this.recordCount = recordCount;
	}

	/**
	 * @return the items
	 */
	public ArrayList<CPatient> getItems() {
		return items;
	}

	/**
	 * @param items the items to set
	 */
	public void setItems(ArrayList<CPatient> items) {
		this.items = items;
	}
}
