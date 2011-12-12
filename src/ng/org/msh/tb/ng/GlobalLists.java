package org.msh.tb.ng;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.entities.enums.DotBy;
import org.msh.tb.entities.enums.HIVResultNg;
import org.msh.tb.entities.enums.Nationality;
import org.msh.tb.entities.enums.ReferredBy;
import org.msh.tb.entities.enums.ReferredTo;
import org.msh.tb.ng.entities.enums.Dot;


@Name("globalLists_ng")
@BypassInterceptors
public class GlobalLists {

	private final static Nationality[] nationalityTypes = {
		Nationality.NIGERIA,
		Nationality.OTHER
	};	
	
	@Factory("nationalitiesNg")
	public Nationality[] getNationalitiesNg() {
		return nationalityTypes;
	}
	
	private final static HIVResultNg[] hivResult = {
		HIVResultNg.POSITIVE,
		HIVResultNg.NEGATIVE,
		HIVResultNg.ONGOING,
		HIVResultNg.DECLINED
	};	
	
	private final static HIVResultNg[] partnerHIVResultNg = {
		HIVResultNg.NO_PARTNER,
		HIVResultNg.POSITIVE,
		HIVResultNg.NEGATIVE,
		HIVResultNg.ONGOING,
		HIVResultNg.DECLINED
	};		
	
	@Factory("partnerHIVResultNg")
	public HIVResultNg[] getPartnerHIVResult() {
		return partnerHIVResultNg;
	}	
	
	
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
		
	@Factory("hivResults_ng")
	public HIVResultNg[] getHIVResults() {
		return hivResult;
	}	
	
	public DotBy[] getDotByTypes() {
		return DotBy.values();
	}		
	
	public Dot[] getDot() {
		return Dot.values();
	}		

}
