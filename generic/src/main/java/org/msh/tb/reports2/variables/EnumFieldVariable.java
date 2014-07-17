package org.msh.tb.reports2.variables;

import org.jboss.seam.core.Expressions;
import org.jboss.seam.international.Messages;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.FilterOption;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.entities.enums.MessageKey;
import org.msh.tb.reports2.VariableImpl;

import java.util.ArrayList;
import java.util.List;


/**
 * Variable that handles fields of type enumeration
 *  
 * @author Ricardo Memoria
 *
 */
public class EnumFieldVariable extends VariableImpl {
	
	private Class<? extends Enum> enumClass;
	private Enum[] options;
	private String optionsExpression;

	public EnumFieldVariable(String id, String keylabel, String fieldName, Class<? extends Enum> enumClass) {
		super(id, keylabel, fieldName);
		this.enumClass = enumClass;
	}


	public EnumFieldVariable(String id, String keylabel, String fieldName, Class<? extends Enum> enumClass, Enum[] options) {
		super(id, keylabel, fieldName);
		this.enumClass = enumClass;
		this.options = options;
	}
	

	public EnumFieldVariable(String id, String keylabel, String fieldName, Class<? extends Enum> enumClass, String optionsExpression) {
		super(id, keylabel, fieldName);
		this.enumClass = enumClass;
		this.optionsExpression = optionsExpression;
	}
	

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getDisplayText(java.lang.Object)
	 */
	@Override
	public String getDisplayText(Object key) {
		if (key == null)
			return "null";
		
		if (KEY_NULL.equals(key))
			return Messages.instance().get("global.notdef");

		Enum val = null;
		Enum[] options = getEnumClass().getEnumConstants();

		if (key instanceof Number) {
			int index = ((Number)key).intValue();
			val = options[index];
		}
		else if (key instanceof Enum) {
			val = (Enum)key;
		}

		if (val == null)
			return key.toString();

		String msgkey = val.getClass().getSimpleName() + "." + val.toString();
		return Messages.instance().get(msgkey);
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#createKey(java.lang.Object)
	 */
	@Override
	public Object createKey(Object value) {
		if (value == null)
			return KEY_NULL;

		if (value instanceof Number)
			return ((Number)value).intValue();
		
		return super.createKey(value);
	}
	
	/**
	 * Return the list of enumeration values available
	 * @return
	 */
	protected Enum[] getEnumValues() {
		// list of options was given ?
		if (options != null)
			return options;

		// an expression to return the options was given ?
		if (optionsExpression != null) {
			return (Enum[])Expressions.instance().createValueExpression(optionsExpression).getValue();
		}

		if (enumClass == null)
			return null;

		return enumClass.getEnumConstants();
	}

	/**
	 * @return the enumClass
	 */
	public Class<? extends Enum> getEnumClass() {
		return enumClass;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getFilterOptions()
	 */
	@Override
	public List<FilterOption> getFilterOptions(Object param) {
		Enum[] vals = getEnumValues();
		
		List<FilterOption> lst = new ArrayList<FilterOption>();
		for (Enum val: vals) {
			String key;
			// get the display name of the label
			if (val instanceof MessageKey)
				 key = ((MessageKey)val).getMessageKey();
			else key = val.getClass().getSimpleName() + "." + val.toString();
			String label = Messages.instance().get(key);
			// add in the list of options
			lst.add(new FilterOption(val.ordinal(), label));
		}
		return lst;
	}


	/**
	 * @return the options
	 */
	public Enum[] getOptions() {
		return options;
	}


	/**
	 * @param options the options to set
	 */
	public void setOptions(Enum[] options) {
		this.options = options;
	}


	/**
	 * @return the optionsExpression
	 */
	public String getOptionsExpression() {
		return optionsExpression;
	}


	/**
	 * @param optionsExpression the optionsExpression to set
	 */
	public void setOptionsExpression(String optionsExpression) {
		this.optionsExpression = optionsExpression;
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#filterValueFromString(java.lang.String)
	 */
	@Override
	public Object filterValueFromString(String value) {
		if ((value == null) || (KEY_NULL.equals(value)))
			return null;

		int val = Integer.parseInt(value);
		
		if (KEY_NULL.equals(val))
			return KEY_NULL;
		
		return enumClass.getEnumConstants()[val];
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
	 */
	@Override
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper, Object value) {
		if (value instanceof String)
			value = filterValueFromString((String)value);
		if (value instanceof Enum)
			value = ((Enum)value).ordinal();
		
		if (KEY_NULL.equals(value))
			value = null;

		super.prepareFilterQuery(def, oper, value);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareValues(Object val1, Object val2) {
		if (val1 == val2)
			return 0;
		
		if (KEY_NULL.equals(val1))
			return 1;
		
		if (KEY_NULL.equals(val2))
			return -1;
		
		return super.compareValues(val1, val2);
	}


	/** {@inheritDoc}
	 */
	@Override
	public Object[] getDomain() {
		return super.getDomain();
/*		Enum[] enums = getEnumValues();
		Object[] values = new Object[enums.length];
		int index = 0;
		for (Enum it: enums) {
			values[index++] = it.ordinal();
		}
		return values;
*/	}
	

}
