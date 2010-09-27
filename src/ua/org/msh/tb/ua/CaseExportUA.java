package org.msh.tb.ua;

import org.jboss.seam.annotations.Name;
import org.msh.tb.importexport.CaseExport;

@Name("caseExportUA")
public class CaseExportUA extends CaseExport {
	private static final long serialVersionUID = -6105790013808048231L;

	@Override
	protected String getHQLFrom() {
		return "from CaseDataUA dataUA join fetch dataUA.tbcase c";
	}

	
}
