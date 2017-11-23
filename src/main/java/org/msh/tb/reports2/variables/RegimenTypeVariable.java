package org.msh.tb.reports2.variables;

import org.jboss.seam.international.Messages;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.FilterOption;
import org.msh.reports.filters.ValueHandler;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.reports2.VariableImpl;

import java.util.ArrayList;
import java.util.List;

public class RegimenTypeVariable extends VariableImpl {

	public RegimenTypeVariable() {
		super("reg_type", "regimen.type", null);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		def.select("tbcase.regimen_id is not null");
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

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
	 */
	@Override
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper, ValueHandler value) {
		if ("0".equals(value.asString()))
			 def.addRestriction("tbcase.regimen_id is null");
		else def.addRestriction("tbcase.regimen_id is not null");
		def.addRestriction("tbcase.initreatmentdate is not null");
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getFilterOptions(java.lang.Object)
	 */
	@Override
	public List<FilterOption> getFilterOptions(Object param) {
		List<FilterOption> options = new ArrayList<FilterOption>();
		options.add(new FilterOption("0", Messages.instance().get("regimens.individualized")));
		options.add(new FilterOption("1", Messages.instance().get("regimens.standard")));
		return options;
	}

	@Override
	public boolean isMultiSelection() {
		return false;
	}
}
