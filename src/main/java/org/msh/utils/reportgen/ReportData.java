package org.msh.utils.reportgen;

public class ReportData {

	private ReportQuery reportQuery;
	private Object[] values;
	private long total;

	public ReportData(ReportQuery reportQuery, Object[] values, long total) {
		super();
		this.reportQuery = reportQuery;
		this.values = values;
		this.total = total;
	}

	/**
	 * Return the value of a specific variable
	 * @param variable
	 * @return
	 */
	public Object getValue(Variable variable) {
		int index = reportQuery.getVariables().indexOf(variable);
		if (index == -1)
			throw new RuntimeException("Variable " + variable.getTitle() + " doesn't belong to report");
		return values[index];
	}
	
	/**
	 * Include a value to the total value
	 * @param value
	 * @return
	 */
	public void addTotal(long value) {
		total += value;
	}
	
	/**
	 * @return the values
	 */
	public Object[] getValues() {
		return values;
	}
	/**
	 * @param values the values to set
	 */
	public void setValues(Object[] values) {
		this.values = values;
	}
	/**
	 * @return the total
	 */
	public long getTotal() {
		return total;
	}
	/**
	 * @param total the total to set
	 */
	public void setTotal(long total) {
		this.total = total;
	}
}
