package org.msh.tb.reports2.variables;

import org.msh.reports.query.SQLDefs;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.reports2.VariableImpl;

public class TreatOutcomeVariable extends VariableImpl {

	public TreatOutcomeVariable() {
		super("outcome", "manag.ind.outcome", "tbcase.state", CaseState.values());
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
