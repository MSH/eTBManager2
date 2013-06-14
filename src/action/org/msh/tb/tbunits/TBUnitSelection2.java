package org.msh.tb.tbunits;

import java.util.List;

import org.msh.tb.entities.Tbunit;

public class TBUnitSelection2 extends TBUnitSelection {

	public TBUnitSelection2(boolean applyUserRestrictions, TBUnitFilter filter) {
		super();
		setApplyUserRestrictions(applyUserRestrictions);
		setApplyHealthSystemRestrictions(true);
		this.filter = filter;
		ignoreReadOnlyRule = false;
	}
	
	public TBUnitSelection2() {
		ignoreReadOnlyRule = false;
	}
	
	public List<Tbunit> getOptions() {
		if (options == null) {
			options = new TbunitSelectionList();
			options.setApplyHealthSystemRestrictions(applyHealthSystemRestrictions);
			options.setRestriction(createHQLUnitFilter());
			options.setAdminUnit(getAuselection().getSelectedUnit());
		}
		
		if(auselection.getUnitLevel5() != null){
			options.setAdminUnit(auselection.getUnitLevel5());
		}else if(auselection.getUnitLevel4() != null){
			options.setAdminUnit(auselection.getUnitLevel4());
		}else if(auselection.getUnitLevel3() != null){
			options.setAdminUnit(auselection.getUnitLevel3());
		}else if(auselection.getUnitLevel2() != null){
			options.setAdminUnit(auselection.getUnitLevel2());
		}else if(auselection.getUnitLevel1() != null){
			options.setAdminUnit(auselection.getUnitLevel1());
		}
		
		return options.getUnits();
	}
	
}
