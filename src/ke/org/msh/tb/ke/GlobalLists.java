package org.msh.tb.ke;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.mdrtb.entities.enums.DotBy;
import org.msh.mdrtb.entities.enums.HIVResultKe;
import org.msh.mdrtb.entities.enums.ReferredBy;
import org.msh.mdrtb.entities.enums.ReferredTo;
import org.msh.tb.ke.entities.enums.Dot;


@Name("globalLists_ke")
@BypassInterceptors
public class GlobalLists {

	private final static ReferredBy[] refByTypes = {
		ReferredBy.VCT_CENTER,
		ReferredBy.HIV_COMP_CARE_UNIT,
		ReferredBy.STI_CLINIC,
		ReferredBy.HOME_BASED_CARE,
		ReferredBy.ANTENATAL_CLINIC,
		ReferredBy.PRIVATE_SECTOR,
		ReferredBy.PHARMACIST,
		ReferredBy.SELF_REFERRAL,
		ReferredBy.CONTACT_INVITATION,
		ReferredBy.CHW
	};
	
	private final static ReferredTo[] refToTypes = {
		ReferredTo.NUTRITION_SUPPORT,
		ReferredTo.VCT_CENTER,
		ReferredTo.HIV_COMP_CARE_UNIT,
		ReferredTo.STI_CLINIC,
		ReferredTo.HOME_BASED_CARE, 
		ReferredTo.ANTENATAL_CLINIC,
		ReferredTo.PRIVATE_SECTOR,
		ReferredTo.NOT_REFERRED
	};	
	
	public ReferredBy[] getRefByTypes() {
		return refByTypes;
	}
	
	public ReferredTo[] getRefToTypes() {
		return refToTypes;
	}	
		
	@Factory("hivResults_ke")
	public HIVResultKe[] getHIVResults() {
		return HIVResultKe.values();
	}	
	
	public DotBy[] getDotByTypes() {
		return DotBy.values();
	}		
	
	public Dot[] getDot() {
		return Dot.values();
	}		

}
