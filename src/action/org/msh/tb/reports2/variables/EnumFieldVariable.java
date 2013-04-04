package org.msh.tb.reports2.variables;

import org.jboss.seam.international.Messages;
import org.msh.tb.reports2.VariableImpl;


public class EnumFieldVariable extends VariableImpl {

	private Class<? extends Enum> enumClass;

	public EnumFieldVariable(String id, String keylabel, String fieldName, Class<? extends Enum> enumClass) {
		super(id, keylabel, fieldName);
		this.enumClass = enumClass;
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

		Enum[] names = getEnumValues();

		int index;
		if (value instanceof Long)
			 index = ((Long)value).intValue();
		else index = (Integer)value;

		if ((names == null) || (index > names.length))
			return value.toString();
		
		return names[index];
	}
	
	/**
	 * Return the list of enumeration values available
	 * @return
	 */
	protected Enum[] getEnumValues() { 
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
	

}
