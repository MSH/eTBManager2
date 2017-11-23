/**
 * 
 */
package org.msh.tb.tbunits;

/**
 * Information to the unit list manager on how to create 
 * the list of TB units 
 * @author Ricardo Memoria
 *
 */
public class UnitListFilter {
	
	private Integer adminUnitId;
	private TBUnitType  type;
	private boolean applyHealthSystemRestriction;
	private boolean applyUserRestrictions;


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
	 * @return the applyHealthSystemrestriction
	 */
	public boolean isApplyHealthSystemRestriction() {
		return applyHealthSystemRestriction;
	}
	/**
	 * @param applyHealthSystemrestriction the applyHealthSystemrestriction to set
	 */
	public void setApplyHealthSystemRestriction(
			boolean applyHealthSystemrestriction) {
		this.applyHealthSystemRestriction = applyHealthSystemrestriction;
	}
	/**
	 * @return the type
	 */
	public TBUnitType getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(TBUnitType type) {
		this.type = type;
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
	
	/** {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((adminUnitId == null) ? 0 : adminUnitId.hashCode());
		result = prime * result + (applyHealthSystemRestriction ? 1231 : 1237);
		result = prime * result + (applyUserRestrictions ? 1231 : 1237);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		UnitListFilter other = (UnitListFilter) obj;
		if (adminUnitId == null) {
			if (other.adminUnitId != null)
				return false;
		} else if (!adminUnitId.equals(other.adminUnitId))
			return false;
		if (applyHealthSystemRestriction != other.applyHealthSystemRestriction)
			return false;
		if (applyUserRestrictions != other.applyUserRestrictions)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

}
