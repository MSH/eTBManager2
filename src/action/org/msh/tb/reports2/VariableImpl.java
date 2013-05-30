package org.msh.tb.reports2;

import java.text.MessageFormat;
import java.util.List;

import org.jboss.seam.core.Expressions;
import org.jboss.seam.international.Messages;
import org.msh.reports.filters.Filter;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.FilterOption;
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

	public static final String KEY_NULL = "null";

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
		// check if key label is an expression
		if (keylabel.contains("#{"))
			 return (String)Expressions.instance().createValueExpression(keylabel).getValue();
		else return Messages.instance().get(keylabel);
	}

	/* (non-Javadoc)
	 * @see org.msh.reports.variables.Variable#prepareVariableQuery(org.msh.reports.query.SQLDefs)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		def.addField(fieldName);
	}


	/* (non-Javadoc)
	 * @see org.msh.reports.variables.Variable#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareValues(Object val1, Object val2) {
		if (val1 == val2)
			return 0;

		if ((val1 == null) || (KEY_NULL.equals(val1)))
			return 1;
		
		if ((val2 == null) || (KEY_NULL.equals(val2)))
			return -1;

		if (val1 instanceof Comparable)
			return ((Comparable)val1).compareTo(val2);
		
		return 0;
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
		if ((value == null) || (KEY_NULL.equals(value))) {
			def.addRestriction(fieldName + " is null");
			return;
		}
		
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
	 * Return a localized message by its key message and replace its arguments inside the text 
	 * @param keymsg the key inside the resource bundle message file
	 * @param arg the arguments to be replaced inside the messages
	 * @return the message text in the selected locale
	 */
	protected String formatMessage(String keymsg, Object... arg) {
		String msg = Messages.instance().get(keymsg);
		return MessageFormat.format(msg, arg);
	}

	
	/**
	 * Create a new instance of {@link FilterOption} from a key. The label of the
	 * option will be generated from the method <code>getDisplayText(Object)</code>
	 * @param key is a variable key
	 * @return instance of {@link FilterOption} using the key as the value
	 */
	public FilterOption createFilterOption(Object key) {
		return new FilterOption(key, getDisplayText(key));
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
		if ((key == null) || (KEY_NULL.equals(key)))
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
		if (values == null)
			return KEY_NULL;
		return values;
	}

	@Override
	public int getIteractionCount() {
		return 1;
	}

	/**
	 * @return the keylabel
	 */
	public String getKeylabel() {
		return keylabel;
	}

	/**
	 * @param keylabel the keylabel to set
	 */
	public void setKeylabel(String keylabel) {
		this.keylabel = keylabel;
	}

	/**
	 * @param fieldName the fieldName to set
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	/* (non-Javadoc)
	 * @see org.msh.reports.variables.Variable#compareGroupValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareGroupValues(Object val1, Object val2) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.msh.reports.filters.Filter#getFilterType()
	 */
	@Override
	public String getFilterType() {
		// return the default filter
		return FilterType.OPTIONS;
	}

	/**
	 * Set the value of a filter from a string type
	 * @param value
	 */
	public Object filterValueFromString(String value) {
		return value;
	}
	
	/**
	 * Return the value of a filter in a string type
	 * @return
	 */
	public String filterValueToString(Object value) {
		return (value == null? null: value.toString());
	}
	
	/* (non-Javadoc)
	 * @see org.msh.reports.filters.Filter#isFilterLazyInitialized()
	 */
	@Override
	public boolean isFilterLazyInitialized() {
		return (FilterType.REMOTE_OPTIONS != null && FilterType.REMOTE_OPTIONS.equals(getFilterType()));
	}
	
	/* (non-Javadoc)
	 * @see org.msh.reports.filters.Filter#getFilterOptions(java.lang.Object)
	 */
	@Override
	public List<FilterOption> getFilterOptions(Object param) {
		return null;
	}

	@Override
	public boolean isTotalEnabled() {
		return true;
	}
}
