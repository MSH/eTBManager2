package org.msh.tb.reports2.variables;

import org.jboss.seam.international.Messages;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.reports2.VariableImpl;

public class SuspectConfirmedVariable extends VariableImpl {

	public SuspectConfirmedVariable() {
		super("diagtype", "DiagnosisType", "diagnosisType");
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs, int)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		if (iteration == 0) {
			def.addField("'susp'");
			def.addRestriction("(registrationDate < diagnosisDate or diagnosisDate is null)");
		}
		else {
			def.addField("'conf'");
			def.addRestriction("diagnosisDate is not null");
		}
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#createKey(java.lang.Object)
	 */
	@Override
	public Object createKey(Object values) {
		if ("susp".equals(values))
			return Messages.instance().get("DiagnosisType.SUSPECT");
		else return Messages.instance().get("DiagnosisType.CONFIRMED");
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getIteractionCount()
	 */
	@Override
	public int getIteractionCount() {
		return 2;
	}

}
