package org.msh.tb.bd;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.mdrtb.entities.enums.TbField;
import org.msh.tb.bd.entities.enums.BiopsyResult;
import org.msh.tb.bd.entities.enums.DotProvider;
import org.msh.tb.bd.entities.enums.ReferredTo;
import org.msh.tb.bd.entities.enums.SkinTestResult;


@Name("globalLists_bd")
@BypassInterceptors
public class GlobalLists {
		
	@Factory("biopsyResults")
	public BiopsyResult[] getBiopsyResult() {
		return BiopsyResult.values();
	}	

	@Factory("skinTestResults")
	public SkinTestResult[] getSkinTestResult() {
		return SkinTestResult.values();
	}		
	
	@Factory("dotProvTypeList")
	public DotProvider[] getDotProvTypeList() {
		return DotProvider.values();
	}		
	
	
	@Factory("refToTypeList")
	public ReferredTo[] getRefToTypes() {
		return ReferredTo.values();
	}		
	
	private static final TbField tbfields[] = {
		TbField.COMORBIDITY,
		TbField.SIDEEFFECT,
		TbField.DST_METHOD,
		TbField.CULTURE_METHOD,
		TbField.BIOPSY_METHOD,
		TbField.CONTACTCONDUCT,
		TbField.CONTACTTYPE,
		TbField.XRAYPRESENTATION,
		TbField.PULMONARY_TYPES,
		TbField.EXTRAPULMONARY_TYPES
	};	
	
	
	public TbField[] getTbFields() {
		return tbfields;
	}	
	
}
