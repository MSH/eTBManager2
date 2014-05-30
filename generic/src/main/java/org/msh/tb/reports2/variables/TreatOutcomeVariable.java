package org.msh.tb.reports2.variables;

import org.apache.commons.lang.ArrayUtils;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.entities.enums.CaseState;

/**
 * Define a variable that handles treatment outcomes
 * @author Ricardo Memoria
 *
 */
public class TreatOutcomeVariable extends EnumFieldVariable {

	public TreatOutcomeVariable() {
		super("outcome", "manag.ind.outcome", "tbcase.state", CaseState.class);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		super.prepareVariableQuery(def, iteration);
		def.addRestriction("tbcase.state > " + CaseState.TRANSFERRING.ordinal());
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.variables.EnumFieldVariable#getFilterOptions(java.lang.Object)
	 */
	@Override
	public Enum[] getEnumValues() {
		setOptionsExpression("#{globalLists.caseStates}");
		Enum[] opts = super.getEnumValues();
		int i = 0;
		while ( i < opts.length) {
			if (opts[i].ordinal() <= CaseState.TRANSFERRING.ordinal())
				opts = (Enum[])ArrayUtils.remove(opts, i);
			else i++;
		}
		return opts;
	}

}
