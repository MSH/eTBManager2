package org.msh.tb.kh;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.entities.enums.DotBy;
import org.msh.tb.entities.enums.HIVResult;
import org.msh.tb.entities.enums.HIVResultNg;
import org.msh.tb.entities.enums.Nationality;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.entities.enums.ReferredBy;
import org.msh.tb.entities.enums.ReferredTo;
import org.msh.tb.entities.enums.SampleType;
import org.msh.tb.entities.enums.TbField;
import org.msh.tb.kh.entities.enums.Dot;
import org.msh.tb.kh.entities.enums.SideEffectGrading;


@Name("globalLists_kh")
@BypassInterceptors
public class GlobalLists {
	private final static Nationality[] nationalityTypes = {
		Nationality.CAMBODIA,
		Nationality.VIETNAM,
		Nationality.THAILAND,
		Nationality.LAO,
		Nationality.OTHER
	};	
	
	private final static HIVResult[] hivResultKhList = {
		HIVResult.NEGATIVE,
		HIVResult.POSITIVE,
		HIVResult.NOTDONE
	};	
	
	@Factory("nationalitiesKh")
	public Nationality[] getNationalitiesKh() {
		return nationalityTypes;
	}
	
	@Factory("hivResultKh")
	public HIVResult[] gethivResultKh() {
		return hivResultKhList;
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
	
	@Factory("partnerHIVResultKh")
	public HIVResultNg[] getPartnerHIVResult() {
		return partnerHIVResultNg;
	}	
	
	
	private final static ReferredBy[] refByTypes = {
		ReferredBy.HEALTHCENTER,
		ReferredBy.REFERRALHOSPITAL,
		ReferredBy.AIDSPROGRAM,
		ReferredBy.COMMUNITY,
		ReferredBy.SELF_REFERRAL,
		ReferredBy.PRIVATE_SECTOR,
		ReferredBy.OTHER
	};
	
	private final static ReferredTo[] refToTypes = {
		ReferredTo.PUBLIC
	};
	
	private final static DotBy[] dotByTypes = {
		DotBy.HOSPITAL,
		DotBy.AMBULATORY,
		DotBy.HOMECARE,
		DotBy.COMMUNITY,
		DotBy.NONDOT
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
		TbField.ADJUSTMENT,
		TbField.ART_REGIMEN,
		TbField.IDENTIFICATION
		
	};
	
	private static final PatientType patientTypes[] = {
		PatientType.NEW,
		PatientType.RAD,
		PatientType.RELAPSE,
		PatientType.FAILURE_CATI,
		PatientType.FAILURE_CATII,
		PatientType.TRANSFER_IN,
		PatientType.PREV_MDRTB,
		PatientType.MDRTB_CONTACT,
		PatientType.TB_HIV
	};
	
	private static final SampleType sampleType[] = {
		SampleType.SPUTUM,
		SampleType.PUS,
		SampleType.CSF,
		SampleType.URINE,
		SampleType.STOOL,
		SampleType.TISSUE,
		SampleType.OTHER
	};
	
	@Factory("gradeListKh")
	public SideEffectGrading[] getGradeList() {
		return SideEffectGrading.values();
	}
	
	private int[] comorb_years = {1,2,3,4,5,6,7,8,9,10};
	
	
	@Factory("tbFields.kh")
	public TbField[] getTbFields() {
		return tbFields;
	}
	
	public ReferredBy[] getRefByTypes() {
		return refByTypes;
	}
	
	public ReferredTo[] getRefToTypes() {
		return refToTypes;
	}
		
	@Factory("hivResults_kh")
	public HIVResultNg[] getHIVResults() {
		return hivResult;
	}	
	
	@Factory("dotBy_kh")
	public DotBy[] getDotByTypes() {
		return dotByTypes;
	}		
	
	public Dot[] getDot() {
		return Dot.values();
	}	
	
	public int[] getComorb_years() {
		return comorb_years;
	}
	
	public void setComorb_years(int[] comorb_years) {
		this.comorb_years = comorb_years;
	}

	@Factory("patientTypes_kh")
	public static PatientType[] getPatienttypes() {
		return patientTypes;
	}

	@Factory("sampleTypes_kh")
	public static SampleType[] getSampletype() {
		return sampleType;
	}
}

