package org.msh.tb.reports2.variables;

import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.ValueHandler;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.entities.enums.TbField;
import org.msh.tb.reports2.FilterType;

public class SideEffectVariable extends FieldValueVariable {

	public SideEffectVariable(String id) {
		super(id, "TbField.SIDEEFFECT", "casesideeffect.sideeffect_id", TbField.SIDEEFFECT);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs, int)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		// condition for the side effect join
		String tbl = def.table("tbcase").join("id", "casesideeffect.case_id").getAlias();
		def.addRestriction(tbl + ".id = (select min(aux.id) from casesideeffect aux " +
				"where aux.sideeffect_id = " + tbl + ".sideeffect_id and aux.case_id = " + tbl + ".case_id)");

		super.prepareVariableQuery(def, iteration);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.variables.FieldValueVariable#getFilterType()
	 */
	@Override
	public String getFilterType() {
		return FilterType.REMOTE_OPTIONS;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
	 */
	@Override
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper, ValueHandler value) {
		def.table("tbcase").join("id", "casesideeffect.case_id");
		super.prepareFilterQuery(def, oper, value);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#isTotalEnabled()
	 */
	@Override
	public boolean isTotalEnabled() {
		return false;
	}


}
