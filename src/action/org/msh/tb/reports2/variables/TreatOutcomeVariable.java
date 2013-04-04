package org.msh.tb.reports2.variables;

import org.msh.reports.query.SQLDefs;
import org.msh.tb.entities.enums.CaseState;

public class TreatOutcomeVariable extends EnumFieldVariable {

	public TreatOutcomeVariable() {
		super("outcome", "manag.ind.outcome", "tbcase.state", CaseState.class);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def) {
		super.prepareVariableQuery(def);
		def.addRestriction("tbcase.state > " + CaseState.TRANSFERRING.ordinal());
	}

}
