package org.msh.tb.reports2.variables;

import org.msh.reports.query.SQLDefs;

/**
 * A variable for indicator org.msh.reports about the result
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

        def.join("patientsample.id", tbl + ".sample_id");

		def.addRestriction("patientsample.dateCollected = (select min(a2.dateCollected) from " +
				s[0] + " aux inner join patientsample a2 on a2.id=aux.sample_id where aux.case_id = tbcase.id " +
						"and a2.dateCollected <= tbcase.diagnosisDate)");

	}

}
