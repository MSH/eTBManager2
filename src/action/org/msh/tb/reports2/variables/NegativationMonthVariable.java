package org.msh.tb.reports2.variables;

import org.jboss.seam.international.Messages;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.reports2.VariableImpl;

/**
 * Variable of report indicator that generates a list of months of treatment
 * where the first exam (culture and/or microscopy) was negative
 * 
 * @author Ricardo Memoria
 *
 */
public class NegativationMonthVariable extends VariableImpl {

	private boolean culture;
	
	public NegativationMonthVariable(String id, boolean culture) {
		super(id, null, null);
		this.culture = culture;

		if (culture) {
			setKeylabel("manag.reportgen.var.cultureneg");
		}
		else {
			setKeylabel("manag.reportgen.var.micneg");
		}
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs, int)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		String tbl;
		if (culture)
			 tbl = "examculture";
		else tbl = "exammicroscopy";

		def.addField("timestampdiff(month, tbcase.initreatmentdate, " + tbl + ".dateCollected)");

		def.addJoin(tbl, "case_id", "tbcase", "id");
		def.addRestriction("tbcase.initreatmentdate is not null");
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getDisplayText(java.lang.Object)
	 */
	@Override
	public String getDisplayText(Object key) {
		if (key == null)
			return Messages.instance().get("manag.reportgen.beforetreat");

		if (key.equals(37L))
			return formatMessage("manag.reportgen.over", 36);

		return key.toString();
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#createKey(java.lang.Object)
	 */
	@Override
	public Object createKey(Object values) {
		Long month = ((Long)values) + 1;

		if (month < 1)
			return null;
		
		if (month > 36)
			month = 37L;

		return month;
	}

}
