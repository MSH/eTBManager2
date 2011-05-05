package org.msh.tb.ke;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.entities.enums.DotBy;
import org.msh.tb.entities.enums.HIVResultKe;
import org.msh.tb.entities.enums.Nationality;
import org.msh.tb.entities.enums.ReferredBy;
import org.msh.tb.entities.enums.ReferredTo;
import org.msh.tb.ke.entities.enums.Dot;


@Name("globalLists_ke")
@BypassInterceptors
public class GlobalLists {

	private final static Nationality[] nationalityTypes = {
		Nationality.KENYA,
		Nationality.BURUNDI,
		Nationality.ETHIOPIA,
		Nationality.RWANDA,
		Nationality.SOMALIA,
		Nationality.SUDAN,
		Nationality.TANZANIA,
		Nationality.UGANDA,
		Nationality.OTHER
	};	
	
	@Factory("nationalitiesKe")
	public Nationality[] getNationalitiesKe() {
		return nationalityTypes;
	}
	
	private final static HIVResultKe[] hivResult = {
		HIVResultKe.POSITIVE,
		HIVResultKe.NEGATIVE,
		HIVResultKe.ONGOING,
		HIVResultKe.DECLINED
	};	
	
	private final static HIVResultKe[] partnerHIVResult = {
		HIVResultKe.NO_PARTNER,
		HIVResultKe.POSITIVE,
		HIVResultKe.NEGATIVE,
		HIVResultKe.ONGOING,
		HIVResultKe.DECLINED
	};		
	
	@Factory("partnerHIVResult")
	public HIVResultKe[] getPartnerHIVResult() {
		return partnerHIVResult;
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
		
	@Factory("hivResults_ke")
	public HIVResultKe[] getHIVResults() {
		return hivResult;
	}	
	
	public DotBy[] getDotByTypes() {
		return DotBy.values();
	}		
	
	public Dot[] getDot() {
		return Dot.values();
	}		

}
