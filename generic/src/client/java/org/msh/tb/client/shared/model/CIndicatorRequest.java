package org.msh.tb.client.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Contains report data about the report to be requested by the client side
 * 
 * @author Ricardo Memoria
 *
 */
public class CIndicatorRequest implements IsSerializable {

	/**
	 * List of variables selected for the column
	 */
	private ArrayList<String> colVariables;
	
	/**
	 * List of variables selected for the row
	 */
	private ArrayList<String> rowVariables;
	
	/**
	 * List of filters and its selected value (in string format)
	 */
	private HashMap<String, String> filters;


	/**
	 * @return the colVariables
	 */
	public ArrayList<String> getColVariables() {
		return colVariables;
	}

	/**
	 * @param colVariables the colVariables to set
	 */
	public void setColVariables(ArrayList<String> colVariables) {
		this.colVariables = colVariables;
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

}
