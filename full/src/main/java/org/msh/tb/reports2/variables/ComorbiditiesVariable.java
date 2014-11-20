package org.msh.tb.reports2.variables;

import org.msh.reports.filters.FilterOperation;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.entities.enums.TbField;

/**
 * @author Ricardo Memoria
 *
 */
public class ComorbiditiesVariable extends FieldValueVariable {

	public ComorbiditiesVariable() {
		super("comorb", "TbField.COMORBIDITY", "casecomorbidity.comorbidity_id", TbField.COMORBIDITY);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs, int)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		def.join("casecomorbidity.case_id", "tbcase.id");
//		def.addJoin("casecomorbidity", "case_id", "tbcase", "id");
		super.prepareVariableQuery(def, iteration);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
	 */
	@Override
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper, Object value) {
		def.join("casecomorbidity.case_id", "tbcase.id");
//		def.addJoin("casecomorbidity", "case_id", "tbcase", "id");
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
