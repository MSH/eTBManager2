package org.msh.tb.reports2.variables;


import org.msh.reports.filters.FilterOperation;
import org.msh.reports.query.SQLDefs;

/**
 * Variable that handle number of exam tests by month/year or just year
 * 
 * @author Ricardo Memoria
 *
 */
public class LabExamDateVariable extends DateFieldVariable {

    private String examTable;
    private boolean joinPatientSample;

	public LabExamDateVariable(String id, String label, String fieldName,
			boolean yearOnly, UnitType unitType) {
		super(id, label, fieldName, yearOnly);
		setUnitType(unitType);
        checkLabFields();
	}

    /**
     * Check if queried field belongs to the patient sample table
     */
    private void checkLabFields() {
        String[] s = getFieldName().split("\\.");
        joinPatientSample = false;

        if (s.length == 2) {
            if (s[1].equalsIgnoreCase("dateCollected")) {
                setFieldName("patientsample." + s[1]);
                joinPatientSample = true;
            }
        }
        examTable = s[0];
    }


    /* (non-Javadoc)
     * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs, int)
     */
    @Override
    public void prepareVariableQuery(SQLDefs def, int iteration) {
        def.join(examTable + ".case_id", "tbcase.id");
        if (joinPatientSample) {
            def.join("patientsample.id", examTable + ".sample_id");
        }
        super.prepareVariableQuery(def, iteration);
    }

    /* (non-Javadoc)
     * @see org.msh.tb.reports2.variables.DateFieldVariable#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
     */
    @Override
    public void prepareFilterQuery(SQLDefs def, FilterOperation oper,
                                   Object value) {
        def.join(examTable + ".case_id", "tbcase.id");
        if (joinPatientSample) {
            def.join("patientsample.id", examTable + ".sample_id");
        }
        super.prepareFilterQuery(def, oper, value);
    }
}
