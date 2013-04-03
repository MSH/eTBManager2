package org.msh.tb.reports2;

import org.jboss.seam.international.Messages;
import org.msh.reports.filters.Filter;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.query.SQLDefs;
import org.msh.reports.variables.Variable;

public class VariableImpl implements Variable, Filter {

	private String id;
	private String keylabel;
	private String fieldName;
	private Object[] names;
	
	public VariableImpl(String id, String keylabel, String fieldName, Object[] names) {
		this.id = id;
		this.fieldName = fieldName;
		this.keylabel = keylabel;
		this.names = names;
	}
	
	@Override
	public String getName() {
		return fieldName;
	}

	@Override
	public String getLabel() {
		return Messages.instance().get(keylabel);
	}

	@Override
	public void prepareVariableQuery(SQLDefs def) {
		def.addField(fieldName);
	}

	
	@Override
	public Object createKey(Object value) {
		if (value == null)
			return null;

		Object[] names = getNames();

		int index;
		if (value instanceof Long)
			 index = ((Long)value).intValue();
		else index = (Integer)value;

		if ((names == null) || (index > names.length))
			return value.toString();
		
		return names[index];
	}


	@Override
	public int compareValues(Object val1, Object val2) {
		if (val1 instanceof Long)
			return ((Long)val1).compareTo((Long)val2);

		return ((Integer)val1).compareTo((Integer)val2);
	}


	@Override
	public Object[] getDomain() {
		return null;
	}
	
	
	protected Object[] getNames() {
		return names;
	}

	@Override
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper, Object value) {
		String s = fieldName.replace('.', '_');
		
		String soper;
		switch (oper) {
		case DIFFERENT: soper = "<>";
			break;
		case GREATER: soper = ">";
			break;
		case GREATER_EQUALS: soper = ">=";
			break;
		case LESS: soper = "<";
			break;
		case LESS_EQUALS: soper = "<=";
			break;
		default: soper = "=";
		}
		def.addRestriction(fieldName + soper + " :" + s);
		def.addParameter(s, value);
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the fieldName
	 */
	public String getFieldName() {
		return fieldName;
	}

	@Override
	public String getDisplayText(Object key) {
		if (key == null)
			return Messages.instance().get("global.notdef");

		if (key instanceof Enum) {
			String msgkey = key.getClass().getSimpleName() + "." + key.toString();
			return Messages.instance().get(msgkey);
		}
		else return key.toString();
	}

	/* (non-Javadoc)
	 * @see org.msh.reports.variables.Variable#isGrouped()
	 */
	@Override
	public boolean isGrouped() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.msh.reports.variables.Variable#createGroupKey(java.lang.Object)
	 */
	@Override
	public Object createGroupKey(Object values) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.msh.reports.variables.Variable#getGroupDisplayText(java.lang.Object)
	 */
	@Override
	public String getGroupDisplayText(Object key) {
		return key != null? key.toString(): "";
	}
}
