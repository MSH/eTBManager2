package org.msh.tb.client.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;

public class CFilter implements IsSerializable {

	private String id;
	private String name;
	private String type;
	private boolean multiSels;

	private ArrayList<CItem> options;

	/**
	 * Constructor passing all filter data
	 * @param id
	 * @param name
	 * @param type
	 * @param options
	 */
	public CFilter(String id, String name, String type, ArrayList<CItem> options) {
		super();
		this.id = id;
		this.name = name;
		this.type = type;
		this.options = options;
	}

	
	/**
	 * Default constructor
	 */
	public CFilter() {
		super();
	}
	
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
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
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

	public boolean isMultiSels() {
		return multiSels;
	}

	public void setMultiSels(boolean multiSels) {
		this.multiSels = multiSels;
	}
}
