package org.msh.tb.ng;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.entities.enums.DotBy;
import org.msh.tb.entities.enums.HIVResult;
import org.msh.tb.entities.enums.HIVResultNg;
import org.msh.tb.entities.enums.Nationality;
import org.msh.tb.entities.enums.PrevTBTreatmentOutcome;
import org.msh.tb.entities.enums.ReferredBy;
import org.msh.tb.entities.enums.ReferredTo;
import org.msh.tb.entities.enums.TbField;
import org.msh.tb.ng.entities.enums.Dot;
import org.msh.tb.ng.entities.enums.HealthFacility;
import org.msh.tb.ng.entities.enums.Qualification;


@Name("globalLists_ng")
@BypassInterceptors
public class GlobalLists {

	private final static Nationality[] nationalityTypes = {
		Nationality.NIGERIA,
		Nationality.OTHER
	};	
	
	private final static HIVResult[] hivResultNgList = {
		HIVResult.NEGATIVE,
		HIVResult.POSITIVE,
		HIVResult.NOTDONE
	};	
	
	@Factory("nationalitiesNg")
	public Nationality[] getNationalitiesNg() {
		return nationalityTypes;
	}
	
	@Factory("hivResultNg")
	public HIVResult[] gethivResultNg() {
		return hivResultNgList;
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
//		ReferredBy.VCT_CENTER,
//		ReferredBy.HIV_COMP_CARE_UNIT,
//		ReferredBy.STI_CLINIC,
//		ReferredBy.HOME_BASED_CARE,
//		ReferredBy.ANTENATAL_CLINIC,
//		ReferredBy.PRIVATE_SECTOR,
//		ReferredBy.PHARMACIST,
//		ReferredBy.SELF_REFERRAL,
//		ReferredBy.CONTACT_INVITATION,
//		ReferredBy.CHW
		ReferredBy.PUBLIC,
		ReferredBy.PRIVATE_SECTOR,
		ReferredBy.FBO
	};
	
	private final static ReferredTo[] refToTypes = {
//		ReferredTo.NUTRITION_SUPPORT,
//		ReferredTo.VCT_CENTER,
//		ReferredTo.HIV_COMP_CARE_UNIT,
//		ReferredTo.STI_CLINIC,
//		ReferredTo.HOME_BASED_CARE, 
//		ReferredTo.ANTENATAL_CLINIC,
//		ReferredTo.PRIVATE_SECTOR,
//		ReferredTo.NOT_REFERRED
		ReferredTo.PUBLIC,
		ReferredTo.PRIVATE_SECTOR,
		ReferredTo.FBO
		
	};	
	
	private static final TbField tbFields[] = {
		TbField.COMORBIDITY,
		TbField.SIDEEFFECT,
		TbField.DST_METHOD,
		TbField.CULTURE_METHOD,
		TbField.CONTACTCONDUCT,
		TbField.CONTACTTYPE,
		TbField.XRAYPRESENTATION,
		TbField.PULMONARY_TYPES,
		TbField.EXTRAPULMONARY_TYPES,
		TbField.ART_REGIMEN,
		TbField.RISK_GROUP
	};
	
	@Factory("tbFields.ng")
	public TbField[] getTbFields() {
		return tbFields;
	}
	
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
	
	@Factory("qualification_ng")
	public Qualification[] getQualification() {
		return Qualification.values();
	}
	
//	@Factory("healthfacility_ng")
//	public HealthFacility[] gethealthFacility() {
//		return HealthFacility.values();
//	}


}
