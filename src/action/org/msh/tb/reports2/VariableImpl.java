package org.msh.tb.reports2;

import org.jboss.seam.international.Messages;
import org.msh.reports.filters.Filter;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.query.SQLDefs;
import org.msh.reports.variables.Variable;

/**
 * Default implementation of a variable that stores the id, the
 * message key and the field name (with optional table alias)
 *  
 * @author Ricardo Memoria
 *
 */
public class VariableImpl implements Variable, Filter {

	private String id;
	private String keylabel;
	private String fieldName;
	
	public VariableImpl(String id, String keylabel, String fieldName) {
		this.id = id;
		this.fieldName = fieldName;
		this.keylabel = keylabel;
	}
	
	/* (non-Javadoc)
	 * @see org.msh.reports.variables.Variable#getName()
	 */
	@Override
	public String getName() {
		return fieldName;
	}

	/* (non-Javadoc)
	 * @see org.msh.reports.variables.Variable#getLabel()
	 */
	@Override
	public String getLabel() {
		return Messages.instance().get(keylabel);
	}

	/* (non-Javadoc)
	 * @see org.msh.reports.variables.Variable#prepareVariableQuery(org.msh.reports.query.SQLDefs)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def) {
		def.addField(fieldName);
	}



	/* (non-Javadoc)
	 * @see org.msh.reports.variables.Variable#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareValues(Object val1, Object val2) {
		if (val1 instanceof Long)
			return ((Long)val1).compareTo((Long)val2);

		return ((Integer)val1).compareTo((Integer)val2);
	}


	/* (non-Javadoc)
	 * @see org.msh.reports.variables.Variable#getDomain()
	 */
	@Override
	public Object[] getDomain() {
		return null;
	}
	

	/* (non-Javadoc)
	 * @see org.msh.reports.filters.Filter#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
	 */
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


	/* (non-Javadoc)
	 * @see org.msh.reports.variables.Variable#getDisplayText(java.lang.Object)
	 */
	@Override
	public String getDisplayText(Object key) {
		if (key == null)
			return Messages.instance().get("global.notdef");

		return key.toString();
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

	@Override
	public Object createKey(Object values) {
		return values;
	}
}
