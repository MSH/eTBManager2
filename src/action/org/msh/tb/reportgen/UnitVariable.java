package org.msh.tb.reportgen;

import org.jboss.seam.international.Messages;
import org.msh.utils.reportgen.ReportQuery;
import org.msh.utils.reportgen.Variable;

public class UnitVariable implements Variable {

	private static final String fields[] = {"tbunit.name1"};
	
	@Override
	public String getTitle() {
		return Messages.instance().get("Tbunit");
	}

	@Override
	public String[] createSQLSelectFields(ReportQuery reportQuery) {
		reportQuery.addJoinMasterTable("tbunit", "id", "unit_id");
		return fields;
	}

	@Override
	public String[] createSQLGroupByFields(ReportQuery reportQuery) {
		return fields;
	}

	@Override
	public Object translateValues(Object[] value) {
		return null;
	}

	@Override
	public Object translateSingleValue(Object value) {
		return value.toString();
	}

	@Override
	public String getValueDisplayText(Object value) {
		String s = (value != null? value.toString(): "");
		if (s.length() > 30)
			s = s.substring(0, 29) + "...";
		return s;
	}

	@Override
	public Integer compareValues(Object val1, Object val2) {
		if ((val1 == null) && (val2 == null))
			return null;
		
		if (val1 == null)
			return -1;
		
		if (val2 == null)
			return 1;
		
		return val1.toString().compareTo(val2.toString());
	}

	@Override
	public boolean setGrouping(boolean value) {
		return false;
	}

	@Override
	public Object getGroupData(Object val1) {
		return null;
	}

}
