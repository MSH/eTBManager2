package org.msh.tb.ua;

import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.CaseEditingHome;
import org.msh.tb.tbunits.TBUnitSelection;

@Name("caseEditingUAHome")
public class CaseEditingUAHome extends CaseEditingHome {
	
	@Override
	public TBUnitSelection getTbunitselection() {
		if (super.getTbunitselection().isApplyHealthSystemRestrictions())
			super.getTbunitselection().setApplyHealthSystemRestrictions(false);
		return super.getTbunitselection();
	}

}
