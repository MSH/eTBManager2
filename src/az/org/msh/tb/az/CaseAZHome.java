package org.msh.tb.az;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.az.entities.CaseSeverityMark;
import org.msh.tb.cases.CaseHome;

@Name("caseAZHome")
public class CaseAZHome {

	@In CaseHome caseHome;
	@In(create=true) CaseEditingAZHome caseEditingAZHome;
	@In(create=true) CaseSeverityMarksHome caseSeverityMarksHome;

	
	/**
	 * Save case data
	 * @return
	 */
	public String persist() {
		String res;
		if (caseHome.isManaged())
			 res = caseEditingAZHome.saveEditing();
		else res = caseEditingAZHome.saveNew();
		
		if (!res.equals("persisted"))
			return res;
		
		caseSeverityMarksHome.save();
		
		return res;
	}


	public List<CaseSeverityMark> getCaseSeverityMarks() {
		if (!caseSeverityMarksHome.isEditing())
			caseSeverityMarksHome.setEditing(true);
		
		return caseSeverityMarksHome.getSeverityMarks();
	}
	
	
}
