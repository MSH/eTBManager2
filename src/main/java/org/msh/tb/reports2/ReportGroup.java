package org.msh.tb.reports2;

import org.jboss.seam.international.Messages;
import org.msh.reports.ReportElement;
import org.msh.reports.filters.Filter;
import org.msh.reports.variables.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
	private List<Variable> variables = new ArrayList<Variable>();
	private List<Filter> filters = new ArrayList<Filter>();

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
	protected void sortList(List lst) {
		Collections.sort(variables, new Comparator<ReportElement>() {
			@Override
			public int compare(ReportElement v1, ReportElement v2) {
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
	public List<Variable> getVariables() {
		return variables;
	}

	/**
	 * @param variables the variables to set
	 */
	public void setVariables(List<Variable> variables) {
		this.variables = variables;
	}

	/**
	 * @return the filters
	 */
	public List<Filter> getFilters() {
		return filters;
	}

	/**
	 * @param filters the filters to set
	 */
	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}
	
}
