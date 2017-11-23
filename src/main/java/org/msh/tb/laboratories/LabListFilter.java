/**
 * 
 */
package org.msh.tb.laboratories;

/**
 * Define filters to generate a list of laboratories
 * @author Ricardo Memoria
 *
 */
public class LabListFilter {

	private Integer adminUnitId;
	private boolean applyUserRestrictions;
	private boolean applyHealthSystemRestrictions;

	/**
	 * @return the adminUnitId
	 */
	public Integer getAdminUnitId() {
		return adminUnitId;
	}
	/**
	 * @param adminUnitId the adminUnitId to set
	 */
	public void setAdminUnitId(Integer adminUnitId) {
		this.adminUnitId = adminUnitId;
	}
	/**
	 * @return the applyUserRestrictions
	 */
	public boolean isApplyUserRestrictions() {
		return applyUserRestrictions;
	}
	/**
	 * @param applyUserRestrictions the applyUserRestrictions to set
	 */
	public void setApplyUserRestrictions(boolean applyUserRestrictions) {
		this.applyUserRestrictions = applyUserRestrictions;
	}
	/**
	 * @return the applyHealthSystemRestrictions
	 */
	public boolean isApplyHealthSystemRestrictions() {
		return applyHealthSystemRestrictions;
	}
	/**
	 * @param applyHealthSystemRestrictions the applyHealthSystemRestrictions to set
	 */
	public void setApplyHealthSystemRestrictions(
			boolean applyHealthSystemRestrictions) {
		this.applyHealthSystemRestrictions = applyHealthSystemRestrictions;
	}

	/** {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((adminUnitId == null) ? 0 : adminUnitId.hashCode());
		result = prime * result + (applyHealthSystemRestrictions ? 1231 : 1237);
		result = prime * result + (applyUserRestrictions ? 1231 : 1237);
		return result;
	}
	/** {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LabListFilter other = (LabListFilter) obj;
		if (adminUnitId == null) {
			if (other.adminUnitId != null)
				return false;
		} else if (!adminUnitId.equals(other.adminUnitId))
			return false;
		if (applyHealthSystemRestrictions != other.applyHealthSystemRestrictions)
			return false;
		if (applyUserRestrictions != other.applyUserRestrictions)
			return false;
		return true;
	}

}
