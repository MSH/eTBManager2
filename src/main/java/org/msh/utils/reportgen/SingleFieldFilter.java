package org.msh.utils.reportgen;

import java.util.List;

public class SingleFieldFilter implements Filter {

	private String title;
	private String id;
	private String field;
	private FilterInputType inputType;

	
	public SingleFieldFilter(String field) {
		super();
		this.id = field;
		this.field = field;
	}

	public SingleFieldFilter(String id, String field) {
		this.id = id;
		this.field = field;
	}
	
	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String createSQLCondition(Object value, ReportQuery reportQuery) {
		String p = reportQuery.createParameter(value);

		String alias = reportQuery.getMainTableAlias();
		return alias + "." + field + " = :" + p;
	}

	@Override
	public FilterInputType getInputType() {
		return inputType;
	}

	@Override
	public List<FilterOption> getOptions() {
		return null;
	}
	
	public String getField() {
		return field;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void setInputType(FilterInputType type) {
		inputType = type;
	}
}
