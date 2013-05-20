package org.msh.tb.reports2.variables;

import org.jboss.seam.international.Messages;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.reports2.VariableImpl;

public class CountingVariable extends VariableImpl {

	public CountingVariable(String id, String keylabel) {
		super(id, keylabel, null);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs, int)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		def.addField("1");
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getDisplayText(java.lang.Object)
	 */
	@Override
	public String getDisplayText(Object key) {
		return Messages.instance().get("global.total");
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#isTotalEnabled()
	 */
	@Override
	public boolean isTotalEnabled() {
		return false;
	}

}
