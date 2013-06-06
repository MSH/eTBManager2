package org.msh.tb.reports2.variables;

import org.jboss.seam.international.Messages;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.query.SQLDefs;

public class CountingExamsVariable extends CountingVariable {

	private static final int KEY_MICROSCOPY = 1;
	private static final int KEY_XPERT = 2;
	private static final int KEY_CULTURE = 3;
	private static final int KEY_DST = 4;
	private static final int KEY_HIV = 5;
	
	
	public CountingExamsVariable() {
		super("examcount", "manag.reportgen.var.totalexams", UnitType.EXAMS_ALL);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.variables.CountingVariable#prepareVariableQuery(org.msh.reports.query.SQLDefs, int)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		def.addField("'" + Integer.toString(iteration + 1) + "'");
		addCommonRestrictions(def, iteration + 1);
	}

	/**
	 * Add restrictions commons to variable and filter
	 * @param iteration
	 */
	protected void addCommonRestrictions(SQLDefs def, int iteration) {
		switch (iteration) {
		case KEY_MICROSCOPY:
			def.addJoin("exammicroscopy", "case_id", "tbcase", "id");
			break;
		case KEY_XPERT:
			def.addJoin("examxpert", "case_id", "tbcase", "id");
			break;
		case KEY_CULTURE:
			def.addJoin("examculture", "case_id", "tbcase", "id");
			break;
		case KEY_DST:
			def.addJoin("examdst", "case_id", "tbcase", "id");
			break;
		case KEY_HIV:
			def.addJoin("examhiv", "case_id", "tbcase", "id");
			break;
		default:
			break;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.variables.CountingVariable#getDisplayText(java.lang.Object)
	 */
	@Override
	public String getDisplayText(Object key) {
		int iteration = (Integer)key;

		switch (iteration) {
		case KEY_MICROSCOPY: return Messages.instance().get("cases.exammicroscopy");
		case KEY_XPERT: return Messages.instance().get("cases.examxpert");
		case KEY_CULTURE: return Messages.instance().get("cases.examculture");
		case KEY_DST: return Messages.instance().get("cases.examdst");
		case KEY_HIV: return Messages.instance().get("cases.examhiv");
		}
		return super.getDisplayText(key);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#createKey(java.lang.Object)
	 */
	@Override
	public Object createKey(Object values) {
		return Integer.parseInt(values.toString());
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getIteractionCount()
	 */
	@Override
	public int getIteractionCount() {
		return 5;
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#isTotalEnabled()
	 */
	@Override
	public boolean isTotalEnabled() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
	 */
	@Override
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper, Object value) {
		int iteration = (Integer)value;
		addCommonRestrictions(def, iteration);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#filterValueFromString(java.lang.String)
	 */
	@Override
	public Object filterValueFromString(String value) {
		return Integer.parseInt(value);
	}


}
