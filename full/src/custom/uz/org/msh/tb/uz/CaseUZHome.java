package org.msh.tb.uz;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.CaseEditingHome;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.uz.entities.TbCaseUZ;

@Name("caseUZHome")
public class CaseUZHome {

	@In CaseHome caseHome;
	@In(create=true) CaseEditingHome caseEditingHome;

	
	/**
	 * Save case data
	 * @return
	 */
	public String persist() {
		TbCaseUZ tbcase = (TbCaseUZ)caseHome.getInstance();
		
		if (tbcase.getPatientType() != PatientType.ANOTHER_TB)
			tbcase.getAnothertb().clear();
	
		String res;
		if (caseHome.isManaged())
			 res = caseEditingHome.saveEditing();
		else res = caseEditingHome.saveNew();
		
		if (!res.equals("persisted"))
			return res;
		
		return res;
	}
}
