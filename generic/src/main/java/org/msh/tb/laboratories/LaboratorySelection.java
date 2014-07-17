package org.msh.tb.laboratories;


import org.jboss.seam.Component;
import org.msh.tb.adminunits.AdminUnitSelector;
import org.msh.tb.entities.Laboratory;

import java.util.List;

public class LaboratorySelection extends AdminUnitSelector<Laboratory> {

//	private List<Laboratory> options;

	/**
	 * Default constructor
	 * @param clientId
	 */
	public LaboratorySelection(String clientId) {
		super(clientId);
	}


	/**
	 * @param clientId
	 * @param applyUserRestrictions
	 */
	public LaboratorySelection(String clientId, boolean applyUserRestrictions) {
		super(clientId, applyUserRestrictions);
	}


	/**
	 * Return list of laboratories to be selected by the user
	 * @return List of {@link Laboratory} instances
	 */
	public List<Laboratory> getOptions() {
		// create the filter
		LabListFilter filter = new LabListFilter();
		filter.setApplyHealthSystemRestrictions(isApplyHealthSystemRestrictions());
		filter.setApplyUserRestrictions(isApplyUserRestrictions());
		filter.setAdminUnitId( getAdminUnitId() );

		// return the list according to the filter
		return ((LabListsManager)Component.getInstance("labListsManager")).getLaboratories(filter);
	}

	
	/**
	 * Return the selected laboratory
	 * @return instance of {@link Laboratory}
	 */
	public Laboratory getLaboratory() {
		return getSelected();
	}
	
	
	/**
	 * Change the selected laboratory
	 * @param lab instance of {@link Laboratory}
	 */
	public void setLaboratory(Laboratory lab) {
		setSelected(lab);
	}

	
	@Override
	public void setSelected(Laboratory lab) {
		if (lab != null)
			 setAdminUnit(lab.getAdminUnit().getParentLevel1());
		else setAdminUnit(null);
		super.setSelected(lab);
	}
	
}
