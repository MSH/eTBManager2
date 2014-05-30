package org.msh.tb.client.shared.model;


import com.google.gwt.user.client.rpc.IsSerializable;

public class CGroup implements IsSerializable {

	private String name;
	private CFilter[] filters;
	private CVariable[] variables;

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
	 * @return the filters
	 */
	public CFilter[] getFilters() {
		return filters;
	}
	/**
	 * @param filters the filters to set
	 */
	public void setFilters(CFilter[] filters) {
		this.filters = filters;
	}
	/**
	 * @return the variables
	 */
	public CVariable[] getVariables() {
		return variables;
	}
	/**
	 * @param variables the variables to set
	 */
	public void setVariables(CVariable[] variables) {
		this.variables = variables;
	}
}
