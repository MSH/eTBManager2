package org.msh.tb.az;

 import org.jboss.seam.annotations.Name;
import org.msh.tb.export.CaseExport;
import org.msh.tb.export.ExcelCreator;

@Name("caseExportAZ")
public class CaseExportAZ extends CaseExport {
	private static final long serialVersionUID = -6105790013808048231L;

	public CaseExportAZ(ExcelCreator excel) {
		super(excel);
	}

	@Override
	protected String getHQLValidationState() {
		return null;
	}
}
