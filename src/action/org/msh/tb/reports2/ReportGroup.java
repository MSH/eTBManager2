package org.msh.tb.reports2;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.international.Messages;

public class ReportGroup {

	private String messageKey;
	private List<VariableImpl> variables = new ArrayList<VariableImpl>();
	private List<VariableImpl> filters = new ArrayList<VariableImpl>();

	public ReportGroup(String messageKey) {
		super();
		this.messageKey = messageKey;
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
