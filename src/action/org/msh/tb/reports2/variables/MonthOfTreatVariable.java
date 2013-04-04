package org.msh.tb.reports2.variables;

import org.msh.reports.query.SQLDefs;
import org.msh.tb.reports2.VariableImpl;

public class MonthOfTreatVariable extends VariableImpl {

	public MonthOfTreatVariable() {
		super("monthTreat", "manag.reportgen.var.monthtreat", null);
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def) {
		def.addField("timestampdiff(month, tbcase.initreatmentdate, tbcase.endtreatmentdate)");
		def.addRestriction("tbcase.endtreatmentdate is not null");
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#createKey(java.lang.Object)
	 */
	@Override
	public Object createKey(Object values) {
		Long val = ((Long)values) + 1;
		if (val > 36)
			val = 37L;
		return val;
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getDisplayText(java.lang.Object)
	 */
	@Override
	public String getDisplayText(Object key) {
		// if it's null, return undefined
		if (key == null)
			return super.getDisplayText(key);
		
		if (key.equals(37L))
			return "acima de 36";

		// if range is not found, return undefined 
		return super.getDisplayText(key);
	}

}
