/**
 * 
 */
package org.msh.reports;


/**
 * Represent an element of the report, which may be a {@link Filter} or a {@link Variable}
 * 
 * @author Ricardo Memoria
 *
 */
public interface ReportElement {

	/**
	 * Internal ID of the element
	 * @return
	 */
	String getId();
	
	/**
	 * Display name of the report element
	 * @return
	 */
	String getLabel();

}
