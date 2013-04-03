package org.msh.tb.client.shared.model;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

public class CFilter implements IsSerializable {

	private String id;
	private String name;
	private CFilterType type;
	private ArrayList<CItem> options;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
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
	public CFilterType getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(CFilterType type) {
		this.type = type;
	}
	/**
	 * @return the options
	 */
	public ArrayList<CItem> getOptions() {
		return options;
	}
	/**
	 * @param options the options to set
	 */
	public void setOptions(ArrayList<CItem> options) {
		this.options = options;
	}
}
