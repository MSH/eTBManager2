package org.msh.tb.reportgen;

import org.jboss.seam.international.Messages;
import org.msh.utils.reportgen.ReportQuery;
import org.msh.utils.reportgen.Variable;

public class RoleVariable implements Variable {

	private static final String fields[] = {"userrole.messageKey"};
	
	@Override
	public String getTitle() {
		return Messages.instance().get("UserRole");
	}

	@Override
	public String[] createSQLSelectFields(ReportQuery reportQuery) {
		reportQuery.addJoinMasterTable("userrole", "id", "role_id");
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
		return value;
	}

	@Override
	public String getValueDisplayText(Object value) {
		return (value != null? value.toString() : "-");
	}

	@Override
	public Integer compareValues(Object val1, Object val2) {
		if ((val1 == null) && (val2 == null))
			return 0;
		
		if (val1 == null)
			return 1;
		
		if (val2 == null)
			return -1;
		
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
