package org.msh.tb.reports2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jboss.seam.international.Messages;

/**
 * Represent a group of variables and filters of a report. A group
 * may contain several filters and variables that share a common feature
 * and it's better to organize them in group in order to be displayed
 * in a way that makes more sense to the user
 * 
 * @author Ricardo Memoria
 *
 */
public class ReportGroup {

	private String messageKey;
	private List<VariableImpl> variables = new ArrayList<VariableImpl>();
	private List<VariableImpl> filters = new ArrayList<VariableImpl>();

	public ReportGroup(String messageKey) {
		super();
		this.messageKey = messageKey;
	}
	
	/**
	 * Sort the list of variables and filters by its label
	 */
	public void sortItems() {
		sortList(variables);
		sortList(filters);
	}
	
	/**
	 * Sort the names of the variables
	 * @param lst list to be sorted
	 */
	protected void sortList(List<VariableImpl> lst) {
		Collections.sort(variables, new Comparator<VariableImpl>() {
			@Override
			public int compare(VariableImpl v1, VariableImpl v2) {
				return v1.getLabel().compareTo(v2.getLabel());
			}
		});
	}
	
	/**
	 * Return the text to be displayed about the group name
	 * @return
	 */
	public String getDisplayText() {
		return Messages.instance().get(messageKey);
	}

	/**
	 * @return the messageKey
	 */
	public String getMessageKey() {
		return messageKey;
	}

	/**
	 * @param messageKey the messageKey to set
	 */
	public void setMessageKey(String messageKey) {
		this.messageKey = messageKey;
	}

	/**
	 * @return the variables
	 */
	public List<VariableImpl> getVariables() {
		return variables;
	}

	/**
	 * @param variables the variables to set
	 */
	public void setVariables(List<VariableImpl> variables) {
		this.variables = variables;
	}

	/**
	 * @return the filters
	 */
	public List<VariableImpl> getFilters() {
		return filters;
	}

	/**
	 * @param filters the filters to set
	 */
	public void setFilters(List<VariableImpl> filters) {
		this.filters = filters;
	}
	
}
