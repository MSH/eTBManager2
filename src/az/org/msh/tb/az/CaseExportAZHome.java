package org.msh.tb.az;

import org.jboss.seam.annotations.Name;
import org.msh.tb.export.CaseExportHome;
import org.msh.tb.export.CaseIterator;

@Name("caseExportAZHome")
public class CaseExportAZHome extends CaseExportHome {
	private static final long serialVersionUID = -1749061457663113197L;

	private CaseExportAZ caseExportAZ;
	
	@Override
	protected CaseIterator getCaseIterator() {
		if (caseExportAZ == null)
			caseExportAZ = new CaseExportAZ(getExcel());
		return caseExportAZ;
	}

}
