package org.msh.tb.reports2.variables;

import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.ValueHandler;
import org.msh.reports.query.SQLDefs;

public class CaseItemDateVariable extends DateFieldVariable {


	public CaseItemDateVariable(String id, String label, String fieldName, boolean yearOnly) {
		super(id, label, fieldName, yearOnly);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs, int)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		String s[] = getFieldName().split("\\.");
		
		def.join(s[0] + ".case_id", "tbcase.id");
		super.prepareVariableQuery(def, iteration);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.variables.DateFieldVariable#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
	 */
	@Override
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper, ValueHandler value) {
		String s[] = getFieldName().split("\\.");
		
		def.join(s[0] + ".case_id", "tbcase.id");
		super.prepareFilterQuery(def, oper, value);
	}

}
