package org.msh.tb.reports2.variables;

import org.msh.reports.query.SQLDefs;

/**
 * A variable for indicator reports about the result
 * of laboratory exams posted before or in the same day of 
 * the diagnosis date
 * 
 * @author Ricardo Memoria
 *
 */
public class LabResultDiagVariable extends EnumFieldVariable {

	public LabResultDiagVariable(String id, String keylabel, String fieldName, Class<? extends Enum> enumClass) {
		super(id, keylabel, fieldName, enumClass);
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
		
		String tbcase = def.getMasterTable().getAlias();

		String s[] = getFieldName().split("\\.");
		String tbl = def.addLeftJoin(s[0], "case_id", "tbcase", "id").getAlias();
		
		def.addRestriction(tbl + ".dateCollected = (select min(aux.dateCollected) from " +
				s[0] + " aux where aux.case_id = " + tbcase + ".id " +
						"and aux.dateCollected <= " + tbcase + ".diagnosisDate)");
	}

}
