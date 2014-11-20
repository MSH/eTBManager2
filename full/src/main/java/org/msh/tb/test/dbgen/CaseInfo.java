package org.msh.tb.test.dbgen;

/**
 * Holds information about cases to be generated
 * @author Ricardo Memoria
 *
 */
public class CaseInfo implements BeanSerialize {
	private int year;
	private int numTBCases;
	private int numMDRTBCases;
	private int numTBGenerated;
	private int numMDRTBGenerated;


	/**
	 * Checks if all TB cases were generated
	 * @return
	 */
	public boolean isAllTBGenerated() {
		return numTBGenerated >= numTBCases;
	}
	
	/**
	 * Checks if all MDR-TB cases were generated
	 * @return
	 */
	public boolean isAllMDRTBGenerated() {
		return numMDRTBGenerated >= numMDRTBCases;
	}
	
	/**
	 * @return the year
	 */
	public int getYear() {
		return year;
	}
	/**
	 * @param year the year to set
	 */
	public void setYear(int year) {
		this.year = year;
	}
	/**
	 * @return the numTBCases
	 */
	public int getNumTBCases() {
		return numTBCases;
	}
	/**
	 * @param numTBCases the numTBCases to set
	 */
	public void setNumTBCases(int numTBCases) {
		this.numTBCases = numTBCases;
	}
	/**
	 * @return the numMDRTBCases
	 */
	public int getNumMDRTBCases() {
		return numMDRTBCases;
	}
	/**
	 * @param numMDRTBCases the numMDRTBCases to set
	 */
	public void setNumMDRTBCases(int numMDRTBCases) {
		this.numMDRTBCases = numMDRTBCases;
	}
	/**
	 * @return the numTBGenerated
	 */
	public int getNumTBGenerated() {
		return numTBGenerated;
	}
	/**
	 * @param numTBGenerated the numTBGenerated to set
	 */
	public void setNumTBGenerated(int numTBGenerated) {
		this.numTBGenerated = numTBGenerated;
	}
	/**
	 * @return the numMDRTBGenerated
	 */
	public int getNumMDRTBGenerated() {
		return numMDRTBGenerated;
	}
	/**
	 * @param numMDRTBGenerated the numMDRTBGenerated to set
	 */
	public void setNumMDRTBGenerated(int numMDRTBGenerated) {
		this.numMDRTBGenerated = numMDRTBGenerated;
	}
}
