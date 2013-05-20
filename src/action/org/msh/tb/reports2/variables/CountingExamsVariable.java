package org.msh.tb.reports2.variables;

import org.jboss.seam.international.Messages;
import org.msh.reports.query.SQLDefs;

public class CountingExamsVariable extends CountingVariable {

	public CountingExamsVariable() {
		super("examcount", "manag.reportgen.var.totalexams");
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.variables.CountingVariable#prepareVariableQuery(org.msh.reports.query.SQLDefs, int)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		def.addField("'" + Integer.toString(iteration + 1) + "'");

		switch (iteration) {
		case 0:
			def.addJoin("examculture", "case_id", "tbcase", "id");
			break;
		case 1:
			def.addJoin("exammicroscopy", "case_id", "tbcase", "id");
			break;
		case 2:
			def.addJoin("examdst", "case_id", "tbcase", "id");
			break;
		case 3:
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
		int iteration = Integer.parseInt(key.toString());
		switch (iteration) {
		case 1: return Messages.instance().get("cases.examculture");
		case 2: return Messages.instance().get("cases.exammicroscopy");
		case 3: return Messages.instance().get("cases.examdst");
		case 4: return Messages.instance().get("cases.examhiv");
		}
		return super.getDisplayText(key);
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
		return 4;
	}

}
