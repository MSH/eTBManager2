package org.msh.tb.ua;

import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.TbCase;
import org.msh.tb.importexport.CaseExport;
import org.msh.tb.ua.entities.CaseDataUA;

@Name("caseExportUA")
public class CaseExportUA extends CaseExport {
	private static final long serialVersionUID = -6105790013808048231L;

	@Override
	protected String getHQLFrom() {
		if (isGeneratingCaseData())
			 return "from CaseDataUA dataUA join fetch dataUA.tbcase c";
		else return super.getHQLFrom();
	}

	@Override
	protected TbCase getCaseFromResultset(Object obj) {
		return ((CaseDataUA)obj).getTbcase();
	}

	@Override
	protected Object getDataFromResultset(Object obj) {
		return obj;
	}

	
}
