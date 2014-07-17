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
 * Wrapper class to help selection of TB units
 * @author Ricardo Memoria
 *
 */
@Name("tbunitselection")
@BypassInterceptors
public class TBUnitSelection extends AdminUnitSelector<Tbunit> {
	
//	private Tbunit tbunit;
	
	//private boolean applyUserRestrictions;
	protected TBUnitType type;
//	protected boolean applyHealthSystemRestrictions;
	protected boolean ignoreReadOnlyRule;
	
//	private UnitListFilter filter = new UnitListFilter();
	
//	protected TbunitSelectionList options;

//	private List<TbunitChangeListener> listeners;

	/**
	 * Constructor with default arguments
	 * @param applyUserRestrictions
	 * @param filter
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
/*		if (unit != null) 
			getAuselection().setSelectedUnit( unit.getAdminUnit().getParentLevel1() );
*/	}
	
	
	public void setSelected(Tbunit unit) {
		if (unit != null)
			setAdminUnit(unit.getAdminUnit().getParentLevel1());
		super.setSelected(unit);
	}
	
	/**
	 * Change the TB Unit selected
	 * @param unit to be selected
	 * @author A.M.
	 */
/*	public void setTbunitWithOptions(Tbunit unit) {
		if (this.tbunit == unit)
			return;
		
		if (unit != null) {
			getAuselection().setSelectedUnit( unit.getAdminUnit().getParentLevel1() );
//			options = null;
//			getOptions();
		}else{
			getAuselection().setSelectedUnit(null);
//			options = null;
		}
		this.tbunit = unit;
		notifyChange();
	}
*/
	
	/**
	 * Checks if administrative unit level 1 is read only
	 * @return true if is read only, otherwise return false
	 */
	public boolean isLevel1ReadOnly() {
		return getAuselection().isLevel1ReadOnly();
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
		if ((userWorkspace != null) && (userWorkspace.getView() == UserView.TBUNIT))
			setSelected(userWorkspace.getTbunit());
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
	 * @param filter the filter to set
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
		if(ignoreReadOnlyRule)
			return false;
		
		UserWorkspace userWorkspace = (UserWorkspace)Component.getInstance("userWorkspace");
		return (userWorkspace.getView() == UserView.TBUNIT);
	}

	/**
	 * @return the ignoreReadOnlyRule
	 */
	public boolean isIgnoreReadOnlyRule() {
		return ignoreReadOnlyRule;
	}

	/**
	 * @param ignoreReadOnlyRule the ignoreReadOnlyRule to set
	 */
	public void setIgnoreReadOnlyRule(boolean ignoreReadOnlyRule) {
		this.ignoreReadOnlyRule = ignoreReadOnlyRule;
	}

	/**
	 * @return the applyHealthSystemRestrictions
	 */
/*	public boolean isApplyHealthSystemRestrictions() {
		return filter.isApplyHealthSystemRestriction();
	}
*/
	/**
	 * @return the ignoreReadOnlyRule
	 */
/*	public boolean isIgnoreReadOnlyRule() {
		return ignoreReadOnlyRule;
	}
*/
	/**
	 * @param ignoreReadOnlyRule the ignoreReadOnlyRule to set
	 */
/*	public void setIgnoreReadOnlyRule(boolean ignoreReadOnlyRule) {
		this.ignoreReadOnlyRule = ignoreReadOnlyRule;
	}
*/
	/**
	 * @param applyHealthSystemRestrictions the applyHealthSystemRestrictions to set
	 */
/*	public void setApplyHealthSystemRestrictions(
			boolean applyHealthSystemRestrictions) {
		setApplyHealthSystemRestrictions(applyHealthSystemRestrictions);
	}
	
	public void changeListener(ValueChangeEvent evt) {
		System.out.println(evt.getComponent().getId());
	}
*/}
