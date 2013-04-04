package org.msh.tb.reports2.variables;

import org.jboss.seam.international.Messages;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.reports2.VariableImpl;

public class RegimenTypeVariable extends VariableImpl {

	public RegimenTypeVariable() {
		super("reg_type", "regimen.type", null);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def) {
		def.addField("tbcase.regimen_id is not null");
		def.addRestriction("tbcase.initreatmentdate is not null");
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getDisplayText(java.lang.Object)
	 */
	@Override
	public String getDisplayText(Object key) {
		// if it's null, return undefined
		if (key == null)
			return super.getDisplayText(key);

		if (key.toString().equals("0"))
			 return Messages.instance().get("regimens.individualized");
		else return Messages.instance().get("regimens.standard");
	}

	@Override
	public Object createKey(Object values) {
		return values;
	}


}
