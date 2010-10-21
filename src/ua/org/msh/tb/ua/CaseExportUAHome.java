package org.msh.tb.ua;

import org.jboss.seam.annotations.Name;
import org.msh.tb.export.CaseExportHome;
import org.msh.tb.export.CaseIterator;

@Name("caseExportUAHome")
public class CaseExportUAHome extends CaseExportHome {
	private static final long serialVersionUID = -1749061457663113197L;

	private CaseExportUA caseExportUA;
	
	@Override
	protected CaseIterator getCaseIterator() {
		if (caseExportUA == null)
			caseExportUA = new CaseExportUA(getExcel());
		return caseExportUA;
	}

}
