package org.msh.tb.md;

import org.msh.tb.entities.enums.CaseClassification;

public class TBCaseImporting extends CaseImporting {

	@Override
	public CaseClassification getCaseClassification() {
		return CaseClassification.TB;
	}
}
