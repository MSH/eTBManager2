package org.msh.tb.reports2.variables;

import java.util.Date;

import org.jboss.seam.international.Messages;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.reports2.VariableImpl;

public class DateFieldVariable extends VariableImpl {

	private boolean yearOnly;


	public DateFieldVariable(String id, String label, String fieldName, boolean yearOnly) {
		super(id, label, fieldName, null);
		this.yearOnly = yearOnly;
	}

	@Override
	public void prepareVariableQuery(SQLDefs def) {
		def.addField("year(" + getFieldName() + ")");
		if (!yearOnly)
			def.addField("month(" + getFieldName() + ")");
		def.addRestriction(getFieldName() + " is not null");
	}

	@Override
	public Object createKey(Object values) {
/*		if (yearOnly) {
			Integer year = (Integer)values;
			Integer[] res = new Integer[2];
			res[0] = year;
			res[1] = year - (Math.round(year / 100) * 100); 
			return res;
		}
*/
		return values;
	}


	/**
	 * @return the yearOnly
	 */
	public boolean isYearOnly() {
		return yearOnly;
	}


	/**
	 * @param yearOnly the yearOnly to set
	 */
	public void setYearOnly(boolean yearOnly) {
		this.yearOnly = yearOnly;
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareValues(Object val1, Object val2) {
		return ((Date)val1).compareTo((Date)val2);
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getDomain()
	 */
	@Override
	public Object[] getDomain() {
		return null;
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
	 */
	@Override
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper, Object value) {
		String p = getFieldName().replace('.', '_');
		def.addRestriction(getFieldName() + "=:" + p);
		def.addParameter(p, value);
	}

	@Override
	public String getDisplayText(Object key) {
		if (key == null)
			return Messages.instance().get("global.notdef");

		return key.toString();
	}

}
