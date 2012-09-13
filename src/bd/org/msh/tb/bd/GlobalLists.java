package org.msh.tb.bd;

import org.apache.commons.beanutils.PropertyUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.bd.entities.enums.BiopsyResult;
import org.msh.tb.bd.entities.enums.DotProvider;
import org.msh.tb.bd.entities.enums.Occupation;
import org.msh.tb.bd.entities.enums.PulmonaryTypesBD;
import org.msh.tb.bd.entities.enums.SalaryRange;
import org.msh.tb.bd.entities.enums.SkinTestResult;
import org.msh.tb.entities.enums.DrugResistanceType;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.entities.enums.ReferredTo;
import org.msh.tb.entities.enums.SampleType;
import org.msh.tb.entities.enums.TbField;


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
		ReferredTo.CV
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
		TbField.ADJUSTMENT
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
	
	
}
