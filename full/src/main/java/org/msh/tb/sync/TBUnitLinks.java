/**
 * 
 */
package org.msh.tb.sync;

/**
 * Store the relation between units used in the system, and servers as a temporary
 * data to be stored in the XML of the desktop file to send information about 
 * these links
 * 
 * @author Ricardo Memoria
 *
 */
public class TBUnitLinks {

	private Integer unitId;
	private Integer authorizedId;
	private Integer firstLineSupplierId;
	private Integer secondLineSupplierId;

	public TBUnitLinks() {
		super();
	}
	
	public TBUnitLinks(Integer unitId, Integer authorizedId,
			Integer firstLineSupplierId, Integer secondLineSupplierId) {
		super();
		this.unitId = unitId;
		this.authorizedId = authorizedId;
		this.firstLineSupplierId = firstLineSupplierId;
		this.secondLineSupplierId = secondLineSupplierId;
	}
	
	
	/**
	 * @return the unitId
	 */
	public Integer getUnitId() {
		return unitId;
	}
	/**
	 * @param unitId the unitId to set
	 */
	public void setUnitId(Integer unitId) {
		this.unitId = unitId;
	}
	/**
	 * @return the authorizedId
	 */
	public Integer getAuthorizedId() {
		return authorizedId;
	}
	/**
	 * @param authorizedId the authorizedId to set
	 */
	public void setAuthorizedId(Integer authorizedId) {
		this.authorizedId = authorizedId;
	}
	/**
	 * @return the firstLineSupplierId
	 */
	public Integer getFirstLineSupplierId() {
		return firstLineSupplierId;
	}
	/**
	 * @param firstLineSupplierId the firstLineSupplierId to set
	 */
	public void setFirstLineSupplierId(Integer firstLineSupplierId) {
		this.firstLineSupplierId = firstLineSupplierId;
	}
	/**
	 * @return the secondLineSupplierId
	 */
	public Integer getSecondLineSupplierId() {
		return secondLineSupplierId;
	}
	/**
	 * @param secondLineSupplierId the secondLineSupplierId to set
	 */
	public void setSecondLineSupplierId(Integer secondLineSupplierId) {
		this.secondLineSupplierId = secondLineSupplierId;
	}
}
