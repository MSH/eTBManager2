package org.msh.tb.na;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.entities.enums.DotBy;
import org.msh.tb.entities.enums.TbField;
import org.msh.tb.entities.enums.YesNoType;
import org.msh.tb.na.entities.enums.Dot;
import org.msh.tb.na.entities.enums.DotOptions;
import org.msh.tb.na.entities.enums.SideEffectAction;
import org.msh.tb.na.entities.enums.SideEffectGrading;
import org.msh.tb.na.entities.enums.SideEffectOutcome;
import org.msh.tb.na.entities.enums.SideEffectSeriousness;


@Name("globalLists_na")
@BypassInterceptors
public class GlobalLists {

	private static final TbField tbfields[] = {
		TbField.COMORBIDITY,
		TbField.SIDEEFFECT,
		TbField.DST_METHOD,
		TbField.CULTURE_METHOD,
		TbField.CONTACTCONDUCT,
		TbField.CONTACTTYPE,
		TbField.XRAYPRESENTATION,
		TbField.PULMONARY_TYPES,
		TbField.EXTRAPULMONARY_TYPES,
		TbField.ART_REGIMEN
	};	
	
	public TbField[] getTbFields() {
		return tbfields;
	}	
	
	
	@Factory("dotOptions")
	public DotOptions[] getDotOptions() {
		return DotOptions.values();
	}
	
	@Factory("outcomeList")
	public SideEffectOutcome[] getOutcomeList() {
		return SideEffectOutcome.values();
	}	

	@Factory("gradeList")
	public SideEffectGrading[] getGradeList() {
		return SideEffectGrading.values();
	}	
	
	@Factory("actionList")
	public SideEffectAction[] getActionList() {
		return SideEffectAction.values();
	}	
	
	@Factory("seriousnessList")
	public SideEffectSeriousness[] getSeriousnessList() {
		return SideEffectSeriousness.values();
	}	

	public DotBy[] getDotByTypes() {
		return DotBy.values();
	}		
	
	public Dot[] getDot() {
		return Dot.values();
	}		
		

}
