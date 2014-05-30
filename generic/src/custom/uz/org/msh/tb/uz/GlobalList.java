package org.msh.tb.uz;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.entities.enums.TbField;

@Name("globalLists_uz")
public class GlobalList {

	private static final PatientType patientTypes[] = {
		PatientType.NEW,
		PatientType.TRANSFER_IN,
		PatientType.RELAPSE,
		PatientType.AFTER_DEFAULT,
		PatientType.FAILURE_FT,
		PatientType.FAILURE_RT,
		PatientType.OTHER,
		PatientType.ANOTHER_TB
	};

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
		TbField.ANOTHERTB
	};

	public PatientType[] getPatientTypes() {
		return patientTypes;
	}
	
	public TbField[] getTbFields() {
		return tbfields;
	}
}
