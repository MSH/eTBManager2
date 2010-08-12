package org.msh.tb.in;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.enums.TbField;

@Name("inEnumList")
public class EnumList {
	
	private static final TbField tbfields[] = {
		TbField.PHYSICAL_EXAMS,
		TbField.DIAG_CONFIRMATION,
		TbField.DST_METHOD,
		TbField.CULTURE_METHOD,
		TbField.SMEAR_METHOD,
		TbField.SYMPTOMS,
		TbField.XRAYPRESENTATION,
		TbField.PULMONARY_TYPES,
		TbField.EXTRAPULMONARY_TYPES,
		TbField.COMORBIDITY,
		TbField.SIDEEFFECT,
		TbField.CONTACTCONDUCT,
		TbField.CONTACTTYPE
	};

	/**
	 * Returns list of TB fields available for drop down menus
	 * @return array of TbField enum
	 */
	@Factory("tbFields.in")
	public TbField[] getTbFields() {
		return tbfields;
	}
}
