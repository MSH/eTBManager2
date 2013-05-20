package org.msh.tb.reports2.variables;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.core.Expressions;
import org.jboss.seam.international.Messages;
import org.msh.reports.filters.FilterOption;
import org.msh.tb.entities.enums.MessageKey;
import org.msh.tb.reports2.VariableImpl;


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
			return Messages.instance().get("global.notdef");

		if (key instanceof Enum) {
			String msgkey = key.getClass().getSimpleName() + "." + key.toString();
			return Messages.instance().get(msgkey);
		}
		else return key.toString();
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#createKey(java.lang.Object)
	 */
	@Override
	public Object createKey(Object value) {
		if (value == null)
			return null;

		Enum[] names = enumClass.getEnumConstants();

		int index;
		if (value instanceof Long)
			 index = ((Long)value).intValue();
		else index = (Integer)value;

		if ((names == null) || (index > names.length))
			return null;
		
		return names[index];
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
		int val = Integer.parseInt(value);
		
		return enumClass.getEnumConstants()[val];
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#compareValues(java.lang.Object, java.lang.Object)
	 */
/*	@Override
	public int compareValues(Object val1, Object val2) {
	}
*/	

}
