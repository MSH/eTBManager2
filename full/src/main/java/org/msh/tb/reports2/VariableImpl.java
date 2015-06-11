package org.msh.tb.reports2;

import org.jboss.seam.core.Expressions;
import org.jboss.seam.international.Messages;
import org.msh.reports.filters.Filter;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.FilterOption;
import org.msh.reports.filters.ValueHandler;
import org.msh.reports.query.SQLDefs;
import org.msh.reports.variables.Variable;

import java.text.MessageFormat;
import java.util.List;

/**
 * Default implementation of a variable that stores the id, the
 * message key and the field name (with optional table alias)
 *  
 * @author Ricardo Memoria
 *
 */
public abstract class VariableImpl implements Variable, Filter {

	public static final String KEY_NULL = "null";

	/**
	 * Available unit types to measure the numbers returned by the indicator
	 * @author Ricardo Memoria
	 *
	 */
	public enum UnitType { DEFAULT, EXAM_CULTURE, EXAM_MICROSCOPY, EXAM_DST, EXAM_XPERT, EXAM_HIV, EXAMS_ALL, CASE_ONLY };
	
	private String id;
	private String keylabel;
	private String fieldName;
	private UnitType unitType;
	
	public VariableImpl(String id, String keylabel, String fieldName) {
		this.id = id;
		this.fieldName = fieldName;
		this.keylabel = keylabel;
		this.unitType = UnitType.DEFAULT;
	}
	
	public VariableImpl() {
		super();
	}
	
	public VariableImpl(String id, String keyLabel, String fieldName, UnitType unitType) {
		this.id = id;
		this.fieldName = fieldName;
		this.keylabel = keyLabel;
		this.unitType = unitType;
	}
	
	
//	public VariableImpl(String id, String keylabel, String fieldName)
	
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
		def.select(fieldName);
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
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper, ValueHandler value) {
		if ((value == null) || (KEY_NULL.equals(value))) {
			def.addRestriction(fieldName + " is null");
			return;
		}
		
		if (value.isArray()) {
            String s = value.mapSqlIN(null);
/*
            Object[] items = (Object[])value;
            String s = "";
            for (Object val: items) {
                if (!s.isEmpty()) {
                    s += ",";
                }
                if (val instanceof Enum) {
                    s += Integer.toString(((Enum)val).ordinal());
                }
                else {
                    s += val.toString();
                }
            }
*/
            s = fieldName + " " + s;
            def.addRestriction(s);
        }
        else {
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
            def.addParameter(s, value.asInteger());
        }
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
		if (isMultiSelection() && value.indexOf(";") >= 0) {
            String[] s = value.split(";");
            return s.length > 1? s: s[0];
        }
        else {
            return value;
        }
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
	
	/**
	 * Return the display text of the units being used to measure the numbers calculated
	 * by the indicator (example, number of patients, number of tests, etc)
	 * @return String with the display text of the units used in the variable, or null
	 * if it is the default unit type label
	 */
	public String getUnitTypeLabel() {
		if ((unitType == UnitType.DEFAULT) || (unitType == null))
			return null;

		return Messages.instance().get("manag.reportgen.unit.numerexams");
	}
	
	/**
	 * Return the unit type to measure the numbers returned by the variable. This
	 * is an object that will be used to compare the units of one variable to
	 * another
	 * @return The object identifying the unit type, or null if it's a default unit type
	 */
	public UnitType getUnitType() {
		return (unitType == UnitType.DEFAULT ? null: unitType);
	}

	/**
	 * @param unitType the unitType to set
	 */
	public void setUnitType(UnitType unitType) {
		this.unitType = unitType;
	}

    /**
     * Convert a filter value in a string representation to an enum value, or an
     * array of enums, if values are separated by semi-commas (;)
     * @param value the string representation of the filter value
     * @param enumClass
     * @return
     */
	protected Object convertEnumFilter(String value, Class enumClass) {
        if ((value == null) || (KEY_NULL.equals(value))) {
            return null;
        }

        if (KEY_NULL.equals(value))
            return KEY_NULL;

        if (value.indexOf(";") == -1) {
            Object[] vals = enumClass.getEnumConstants();
            int index = Integer.parseInt(value);
            return ((Enum)vals[index]);
        }
        else {
            String[] vals = value.split(";");
            Enum[] res = new Enum[vals.length];
            for (int i = 0; i < vals.length; i++) {
                res[i] = (Enum)convertEnumFilter(vals[i], enumClass);
            }
            return res;
        }
    }

    /**
     * Convert a string representation of a filter value into an integer
     * or an array of integer (when values separated by semi-comma ';')
     * @param s the filter value in string format
     * @return
     */
    protected Object convertIntFilter(String s) {
        if ((s == null) || (KEY_NULL.equals(s))) {
            return null;
        }

        if (s.indexOf(";") == -1) {
            return Integer.parseInt(s);
        }
        else {
            String[] vals = s.split(";");
            Integer[] res = new Integer[vals.length];
            for (int i = 0; i < vals.length; i++) {
                res[i] = (Integer)convertIntFilter(vals[i]);
            }
            return res;
        }
    }

    /**
     * Return true if filter accept multiple selection
     * @return boolean value
     */
    @Override
    public boolean isMultiSelection() {
        return true;
    }
}
