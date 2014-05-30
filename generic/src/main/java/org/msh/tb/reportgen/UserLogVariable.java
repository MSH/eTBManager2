package org.msh.tb.reportgen;

import org.jboss.seam.international.Messages;
import org.msh.utils.reportgen.ReportQuery;
import org.msh.utils.reportgen.Variable;

public class UserLogVariable implements Variable {

	private static String fields[] = {"userlog.name"};
	
	@Override
	public String getTitle() {
		return Messages.instance().get("User");
	}

	@Override
	public String[] createSQLSelectFields(ReportQuery reportQuery) {
		reportQuery.addJoinMasterTable("userlog", "id", "userlog_id");
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
		return value.toString();
	}

	@Override
	public Integer compareValues(Object val1, Object val2) {
		return val1.toString().compareToIgnoreCase(val2.toString());
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
