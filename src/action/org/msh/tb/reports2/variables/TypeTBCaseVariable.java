package org.msh.tb.reports2.variables;

import org.jboss.seam.international.Messages;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.entities.enums.MedicineLine;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.reports2.VariableImpl;

public class TypeTBCaseVariable extends VariableImpl {

	private static final String KEY_NEW = "anew";
	private static final String KEY_1LINE = "line1";
	private static final String KEY_2LINE = "line2";


	public TypeTBCaseVariable() {
		super("tb_hist", "manag.reportgen.var.tbcasetype", null);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs, int)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		String key;
		switch (iteration) {
		// NEW PATIENT
		case 0:
			key = KEY_NEW;
			break;

		// PREVIOUSLY TREATED WITH 1ST LINE DRUGS
		case 1:
			key = KEY_1LINE;
			break;
		
		// PREVIOUSLY TREATED WITH 2ND LINE DRUGS
		case 2:
			key = KEY_2LINE;
			break;
			
		default:
			throw new IllegalArgumentException("Iteration not expected: " + iteration);
		}
		
		def.addField("'" + key + "'");
		addRestrictions(def, key);
	}

	
	/**
	 * Add common restrictions to variables and filters
	 * @param def
	 * @param key
	 */
	protected void addRestrictions(SQLDefs def, String key) {
		// NEW PATIENT
		if (KEY_NEW.equals(key)) {
			def.addRestriction("tbcase.patientType = " + PatientType.NEW.ordinal());
			return;
		}

		// PREVIOUSLY TREATED WITH 1ST LINE DRUGS
		if (KEY_1LINE.equals(key)) {
			def.addRestriction("tbcase.patientType != " + PatientType.NEW.ordinal());
			def.addRestriction("not exists(select * from prevtbtreatment pv " +
					  "inner join res_prevtbtreatment d1 on d1.prevtbtreatment_id = pv.id " +
					  "inner join substance c1 on c1.id = d1.substance_id " +
					  "where c1.line = " + MedicineLine.SECOND_LINE.ordinal() + " and pv.case_id = tbcase.id)");
			return;
		}
		
		// PREVIOUSLY TREATED WITH 2ND LINE DRUGS
		if (KEY_2LINE.equals(key)) {
			def.addRestriction("tbcase.patientType != " + PatientType.NEW.ordinal());
			def.addRestriction("exists(select * from prevtbtreatment pv " +
					  "inner join res_prevtbtreatment d1 on d1.prevtbtreatment_id = pv.id " +
					  "inner join substance c1 on c1.id = d1.substance_id " +
					  "where c1.line = " + MedicineLine.SECOND_LINE.ordinal() + " and pv.case_id = tbcase.id)");
			return;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
	 */
	@Override
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper,
			Object value) {
		addRestrictions(def, (String)value);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#createKey(java.lang.Object)
	 */
	@Override
	public Object createKey(Object values) {
		return super.createKey(values);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getIteractionCount()
	 */
	@Override
	public int getIteractionCount() {
		return 3;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#filterValueFromString(java.lang.String)
	 */
	@Override
	public Object filterValueFromString(String value) {
		if ((value == null) || ((!(KEY_1LINE.equals(value))) && (!(KEY_2LINE.equals(value))) && (!(KEY_NEW.equals(value)))) )
			throw new IllegalArgumentException("Wrong type for filter value: " + value);

		return value;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getDisplayText(java.lang.Object)
	 */
	@Override
	public String getDisplayText(Object key) {
		if (KEY_NEW.equals(key))
			return Messages.instance().get("manag.confmdrrep.new");

		if (KEY_1LINE.equals(key))
			return Messages.instance().get("manag.confmdrrep.prev12line");

		if (KEY_2LINE.equals(key))
			return Messages.instance().get("manag.confmdrrep.prev1line");

		return super.getDisplayText(key);
	}

}
