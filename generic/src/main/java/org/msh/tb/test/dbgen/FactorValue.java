package org.msh.tb.test.dbgen;

/**
 * Keeps information about a factor value in the format A/B
 * @author Ricardo Memoria
 *
 */
public class FactorValue {

	private int valueA;
	private int valueB;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Integer.toString(valueA) + "/" + Integer.toString(valueB);
	}
	/**
	 * @return the valueA
	 */
	public int getValueA() {
		return valueA;
	}
	/**
	 * @param valueA the valueA to set
	 */
	public void setValueA(int valueA) {
		this.valueA = valueA;
	}
	/**
	 * @return the valueB
	 */
	public int getValueB() {
		return valueB;
	}
	/**
	 * @param valueB the valueB to set
	 */
	public void setValueB(int valueB) {
		this.valueB = valueB;
	}
}
