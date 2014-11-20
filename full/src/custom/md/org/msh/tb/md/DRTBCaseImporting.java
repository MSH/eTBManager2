package org.msh.tb.md;

import org.msh.tb.entities.enums.CaseClassification;

public class DRTBCaseImporting extends CaseImporting {

	@Override
	public CaseClassification getCaseClassification() {
		return CaseClassification.DRTB;
	}
}
