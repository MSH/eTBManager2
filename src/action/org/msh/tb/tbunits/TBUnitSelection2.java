package org.msh.tb.tbunits;

import java.util.List;

import org.jboss.seam.Component;
import org.msh.tb.entities.Tbunit;

public class TBUnitSelection2 extends TBUnitSelection {

	public TBUnitSelection2(String clientId, boolean applyUserRestrictions, TBUnitType filter) {
		super(clientId, applyUserRestrictions, filter);
	}
	
	public TBUnitSelection2(String clientId) {
		super(clientId);
	}
	
	/**
	 * Change the TB Unit selected
	 * @param unit to be selected
	 */
	@Override
	public void setTbunit(Tbunit unit) {
		setSelected(unit);
	}
	
	@Override
	public void setSelected(Tbunit unit) {
		super.setSelected2(unit);
	}
	
	/**
	 * Return the {@link Tbunit} list from the administrative unit selected
	 * @return List of {@link Tbunit} instances
	 */
	@Override
	public List<Tbunit> getOptions() {
		UnitListFilter filter = new UnitListFilter();
		filter.setAdminUnitId( getAdminUnitId() );
		filter.setApplyHealthSystemRestriction(isApplyHealthSystemRestrictions());
		filter.setApplyUserRestrictions(isApplyUserRestrictions());
		filter.setType(type);
		return ((UnitListsManager)Component.getInstance("unitListsManager")).getUnits(filter);
	}
	
	@Override
	public Integer getAdminUnitId(){
		if(auselection.getUnitLevel5() != null){
			return auselection.getUnitLevel5().getId();
		}else if(auselection.getUnitLevel4() != null){
			return auselection.getUnitLevel4().getId();
		}else if(auselection.getUnitLevel3() != null){
			return auselection.getUnitLevel3().getId();
		}else if(auselection.getUnitLevel2() != null){
			return auselection.getUnitLevel2().getId();
		}else if(auselection.getUnitLevel1() != null){
			return auselection.getUnitLevel1().getId();
		}else{
			return null;
		}
	}
	
}
