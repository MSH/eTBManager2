package org.msh.tb.reports2.variables;

import org.msh.reports.filters.FilterOperation;
import org.msh.reports.query.SQLDefs;

public class LabResultVariable extends EnumFieldVariable {
	
	public static int UNIT_EXAMS = 10;

    private String examTable;
    private boolean joinPatientSample;

	public LabResultVariable(String id, String keylabel, String fieldName, Class<? extends Enum> enumClass, UnitType unitType) {
		super(id, keylabel, fieldName, enumClass);
		setUnitType(unitType);
        checkLabFields();
	}


    /**
     * Check if queried field belongs to the patient sample table. This must be a temporary solution,
     * because the fieldName may points to a field in a join table (example.: exammicroscopy.dateCollected)
     * but dateCollected belongs to the patientsample table.
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
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper,
			Object value) {
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

        if (joinPatientSample) {
            def.join("patientsample.id", s[0] + ".sample_id");
        }
	}

}
