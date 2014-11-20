package org.msh.tb.bd;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.bd.entities.enums.*;
import org.msh.tb.entities.enums.*;
import org.msh.tb.entities.enums.ReferredTo;
import org.msh.utils.date.DateUtils;

import java.util.ArrayList;
import java.util.List;


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
	
	@Factory("salaryRangeList")
	public SalaryRange[] getSalaryRangeList() {
		return SalaryRange.values();
	}
	
	@Factory("occupationList")
	public Occupation[] getOccupationList() {
		return Occupation.values();
	}

	
	@Factory("dotProvTypeList")
	public DotProvider[] getDotProvTypeList() {
		return DotProvider.values();
	}	
	
	@Factory("pulmonaryTypesBD")
	public PulmonaryTypesBD[]  getPulmonaryTypesBD(){
		return PulmonaryTypesBD.values();
	}

	private static final ReferredTo referredToTypes[] = {
		ReferredTo.PP,
		ReferredTo.GFS,
		ReferredTo.NON_PP,
		ReferredTo.SS,
		ReferredTo.VD, 
		ReferredTo.CV,
		ReferredTo.GOV,
		ReferredTo.PRIVATE_HOSP,
		ReferredTo.TB_PATIENT,
		ReferredTo.OTHER
	};		
	
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
		TbField.EXTRAPULMONARY_TYPES,
		TbField.ADJUSTMENT,
        TbField.MEDEXAM_DOTTYPE,
        TbField.MEDEXAM_REFTOTYPE
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
	
	private static final DrugResistanceType drugResistanceType[] = {
		DrugResistanceType.MONO_RESISTANCE,
		DrugResistanceType.POLY_RESISTANCE,
		DrugResistanceType.MULTIDRUG_RESISTANCE,
		DrugResistanceType.EXTENSIVEDRUG_RESISTANCE,
		DrugResistanceType.UNKNOWN,
	};
	
	private static final QuarterMonths quarter[] = {
		QuarterMonths.FIRST,
		QuarterMonths.SECOND,
		QuarterMonths.THIRD,
		QuarterMonths.FOURTH,
	};
	
	private static final SideEffectGrading sideEffectGradings[] = {
		SideEffectGrading.MILD,
		SideEffectGrading.MODERATE, 
		SideEffectGrading.SEVERE,
	};
	
	private static final SideEffectSeriousness sideEffectSeriousnesses[] = {
		SideEffectSeriousness.NONE,
		SideEffectSeriousness.HOSPITALIZED,
		SideEffectSeriousness.DEAD,
		SideEffectSeriousness.CONGENITAL_ANOMALY,
		SideEffectSeriousness.DISABILITY,
		SideEffectSeriousness.LIFE_THREATNING,
		SideEffectSeriousness.OTHER,
	};
	
	private static final SideEffectAction sideEffectActions[] = {
		SideEffectAction.NONE,
		SideEffectAction.DISCONTINUED,
		SideEffectAction.REDUCED,
		SideEffectAction.SWITCH,
		SideEffectAction.RE_CHALLENGE,
		SideEffectAction.OTHER,
	};
	
	private static final SideEffectOutcome sideEffectOutcomes[] = {
		SideEffectOutcome.UNKNOWN,
		SideEffectOutcome.RESOLVED,
		SideEffectOutcome.RESOLVING,
		SideEffectOutcome.SEQUEALE,
		SideEffectOutcome.NOT_RESOLVED,
		SideEffectOutcome.DEATH,
	};

    private static final PrevTBTreatmentOutcome prevTBTreatmentOutcomes[] = {
            PrevTBTreatmentOutcome.CURED,
            PrevTBTreatmentOutcome.COMPLETED,
            PrevTBTreatmentOutcome.FAILURE,
            PrevTBTreatmentOutcome.DEFAULTED,
            PrevTBTreatmentOutcome.SCHEME_CHANGED,
            PrevTBTreatmentOutcome.TRANSFERRED_OUT,
            PrevTBTreatmentOutcome.DELAYED_CONVERTER,
            PrevTBTreatmentOutcome.UNKNOWN,
            PrevTBTreatmentOutcome.OTHER
    };
	
	public TbField[] getTbFields() {
		return tbfields;
	}

	public ReferredTo[] getReferredtotypes() {
		return referredToTypes;
	}	
	
	
	public static final InfectionSite infectionSite[] =  {
		InfectionSite.PULMONARY,
		InfectionSite.EXTRAPULMONARY
	};
	
	@Factory("infectionSite_bd")
	public InfectionSite[] getInfectionSite() {
		return  infectionSite;
	}

	@Factory("sampleTypes_bd")
	public static SampleType[] getSampletype() {
		return sampleType;
	}

	@Factory("drugResistanceType_bd")
	public static DrugResistanceType[] getDrugresistancetype() {
		return drugResistanceType;
	}
	
	@Factory("quarter")
	public static QuarterMonths[] getQuarter(){
		return quarter;
	}
	
	@Factory("quarterYears")
	public static List<Integer> getQuarterYears(){
		int currYear = DateUtils.yearOf(DateUtils.getDate());
		List<Integer> years;
		
		years = new ArrayList<Integer>();
		for(int i = currYear ; i >= 2010 ; i--){
			years.add(i);
		}
	
		return years;
	}
	
	public static SideEffectGrading[] getSideEffectGradings(){
		return sideEffectGradings;
	}
	
	public static SideEffectSeriousness[] getSideEffectSeriousnesses(){
		return sideEffectSeriousnesses;
	}
	
	public static SideEffectAction[] getSideEffectActions(){
		return sideEffectActions;
	}

    public static SideEffectOutcome[] getSideEffectOutcomes(){
        return sideEffectOutcomes;
    }

    public static PrevTBTreatmentOutcome[] getPrevTBTreatmentOutcomes(){
        return prevTBTreatmentOutcomes;
    }
}
