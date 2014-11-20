package org.msh.tb.reports2.variables;

import org.msh.reports.filters.FilterOperation;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.entities.enums.TbField;

public class LabMethodVariable extends FieldValueVariable {

	public LabMethodVariable(String id, String keylabel, String fieldName, TbField tbfield, UnitType unitType) {
		super(id, keylabel, fieldName, tbfield);
		setUnitType(unitType);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs, int)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		// add a join with the exam table to the tbcase table
		String s[] = getFieldName().split("\\.");
		def.table("tbcase").leftJoin("id", s[0] + ".case_id").getAlias();

		super.prepareVariableQuery(def, iteration);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
	 */
	@Override
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper,
			Object value) {
		// add a join with the exam table to the tbcase table
		String s[] = getFieldName().split("\\.");
		def.table("tbcase").leftJoin("id", s[0] + ".case_id").getAlias();

		super.prepareFilterQuery(def, oper, value);
	}

}
