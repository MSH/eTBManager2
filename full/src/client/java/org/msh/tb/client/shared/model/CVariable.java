package org.msh.tb.client.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public class CVariable implements IsSerializable {

	private String id;
	private String name;
	
	/**
	 * Constructor passing the variable fields
	 * @param id
	 * @param name
	 */
	public CVariable(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	
	/**
	 * Default constructor
	 */
	public CVariable() {
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
}
