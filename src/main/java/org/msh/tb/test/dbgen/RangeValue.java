package org.msh.tb.test.dbgen;

/**
 * Keeps a range of two integer numbers
 * @author Ricardo Memoria
 *
 */
public class RangeValue {
	private int iniValue;
	private int endValue;


	/**
	 * @return the iniValue
	 */
	public int getIniValue() {
		return iniValue;
	}
	/**
	 * @param iniValue the iniValue to set
	 */
	public void setIniValue(int iniValue) {
		this.iniValue = iniValue;
	}
	/**
	 * @return the endValue
	 */
	public int getEndValue() {
		return endValue;
	}
	/**
	 * @param endValue the endValue to set
	 */
	public void setEndValue(int endValue) {
		this.endValue = endValue;
	}

	@Override
	public String toString() {
		return "RangeValue [endValue=" + endValue + ", iniValue=" + iniValue
				+ "]";
	}
}
