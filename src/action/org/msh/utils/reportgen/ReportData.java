package org.msh.utils.reportgen;

public class ReportData {

	private Object[] values;
	private long total;

	public ReportData(Object[] values, long total) {
		super();
		this.values = values;
		this.total = total;
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
