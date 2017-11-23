package org.msh.tb.tbunits;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.adminunits.AdminUnitSelector;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.enums.UserView;

import java.util.List;


/**
 * Wrapper class to help selection of TB units in forms
 * @author Ricardo Memoria
 *
 */
@Name("tbunitselection")
@BypassInterceptors
public class TBUnitSelection extends AdminUnitSelector<Tbunit> {

	//private boolean applyUserRestrictions;
	protected TBUnitType type;
//	protected boolean applyHealthSystemRestrictions;
//	protected boolean ignoreReadOnlyRule;
	private boolean readOnly;

	/**
	 * Constructor with default arguments
	 * @param applyUserRestrictions
	 * @param type the type of unit to be displayed
	 */
	public TBUnitSelection(String clientId, boolean applyUserRestrictions, TBUnitType type) {
		super(clientId);
		setApplyHealthSystemRestrictions(true);
		setApplyUserRestrictions(applyUserRestrictions);
		this.type = type;
	}
	
	/**
	 * Default constructor
	 */
	public TBUnitSelection(String clientId) {
		super(clientId);
	}
	
	/**
	 * Return the TB Unit selected by the user
	 * @return instance of {@link Tbunit} class
	 */
	public Tbunit getTbunit() {
		return getSelected();
	}

	
	/**
	 * Change the TB Unit selected
	 * @param unit to be selected
	 */
	public void setTbunit(Tbunit unit) {
		setSelected(unit);
	}
	
	
	public void setSelected(Tbunit unit) {
		if (unit != null){
            setAdminUnit(unit.getAdminUnit());
//			setAdminUnit(unit.getAdminUnit().getParentLevel1());
        }
		super.setSelected(unit);
	}
	

	/**
	 * Checks if administrative unit level 1 is read only
	 * @return true if is read only, otherwise return false
	 */
	public boolean isLevel1ReadOnly() {
		return isReadOnly() || getAuselection().isLevel1ReadOnly();
	}


	/**
	 * Return the list of user administrative units realted to the user restrictions
	 * @return list of {@link AdministrativeUnit} instances
	 */
	public List<AdministrativeUnit> getParentUnits() {
		return getAuselection().getParentUnits();
	}

	
	/**
	 * Return the {@link Tbunit} list from the administrative unit selected
	 * @return List of {@link Tbunit} instances
	 */
	public List<Tbunit> getOptions() {
		UnitListFilter filter = new UnitListFilter();
		filter.setAdminUnitId( getAdminUnitId() );
		filter.setApplyHealthSystemRestriction(isApplyHealthSystemRestrictions());
		filter.setApplyUserRestrictions(isApplyUserRestrictions());
		filter.setType(type);
		return ((UnitListsManager)Component.getInstance("unitListsManager")).getUnits(filter);
	}

	
	/**
	 * Apply the restrictions of the user set in its profile (user view)
	 */
	protected void applyUserTBUnitRestriction() {
		UserWorkspace userWorkspace = (UserWorkspace)Component.getInstance("userWorkspace");
		if ((userWorkspace != null) && (userWorkspace.getView() == UserView.TBUNIT)) {
            setSelected(userWorkspace.getTbunit());
            this.readOnly = true;
        }
	}

	/** {@inheritDoc}
	 */
	@Override
	public void setApplyUserRestrictions(boolean applyUserRestrictions) {
		super.setApplyUserRestrictions(applyUserRestrictions);
		if (applyUserRestrictions)
			applyUserTBUnitRestriction();
	}


	/**
	 * @param type the filter to set
	 */
	public void setUnitType(TBUnitType type) {
		this.type = type;
	}


	/**
	 * @return the filter
	 */
	public TBUnitType getFilter() {
		return type;
	}
	
	public boolean isReadOnly() {
        return readOnly;
/*
		if (getSelected() == null) {
            return false;
        }

		UserWorkspace userWorkspace = (UserWorkspace)Component.getInstance("userWorkspace");
		return (userWorkspace.getView() == UserView.TBUNIT);
*/
	}

    /**
     * Set the read-only value to true
     * @param value
     */
    public void setReadOnly(boolean value) {
        readOnly = value;
    }

	/**
	 * @return the ignoreReadOnlyRule
	 */
/*
	public boolean isIgnoreReadOnlyRule() {
		return ignoreReadOnlyRule;
	}
*/

	/**
	 * @param ignoreReadOnlyRule the ignoreReadOnlyRule to set
	 */
/*
	public void setIgnoreReadOnlyRule(boolean ignoreReadOnlyRule) {
		this.ignoreReadOnlyRule = ignoreReadOnlyRule;
	}
*/
}
