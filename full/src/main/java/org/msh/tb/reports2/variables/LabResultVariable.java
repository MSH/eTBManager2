package org.msh.tb.reports2.variables;

import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.ValueHandler;
import org.msh.reports.query.SQLDefs;

public class LabResultVariable extends EnumFieldVariable {
	
	public static int UNIT_EXAMS = 10;

	public LabResultVariable(String id, String keylabel, String fieldName, Class<? extends Enum> enumClass, UnitType unitType) {
		super(id, keylabel, fieldName, enumClass);
		setUnitType(unitType);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#createKey(java.lang.Object)
	 */
	@Override
	public Object createKey(Object value) {
		return super.createKey(value);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		super.prepareVariableQuery(def, iteration);
		prepareQuery(def);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.variables.EnumFieldVariable#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
	 */
	@Override
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper, ValueHandler value) {
		super.prepareFilterQuery(def, oper, value);
		prepareQuery(def);
	}
	
	/**
	 * Prepare the query with common declarations both in filter and variable
	 * @param def
	 */
	public void prepareQuery(SQLDefs def) {
		String s[] = getFieldName().split("\\.");
		def.table("tbcase").leftJoin("id", s[0] + ".case_id").getAlias();
	}

}
