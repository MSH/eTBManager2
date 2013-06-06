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
public class LabResultDiagVariable extends LabResultVariable {
	
	public LabResultDiagVariable(String id, String keylabel, String fieldName,
			Class<? extends Enum> enumClass, UnitType unitType) {
		super(id, keylabel, fieldName, enumClass, unitType);
	}

	/**
	 * Prepare the query with common declarations both in filter and variable
	 * @param def
	 */
	public void prepareQuery(SQLDefs def) {
		super.prepareQuery(def);

		String s[] = getFieldName().split("\\.");
		String tbl = s[0];
		def.addRestriction(tbl + ".dateCollected = (select min(aux.dateCollected) from " +
				s[0] + " aux where aux.case_id = tbcase.id " +
						"and aux.dateCollected <= tbcase.diagnosisDate)");
	}

}
