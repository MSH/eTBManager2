package org.msh.tb.transactionlog;

import java.util.Date;

import org.apache.commons.beanutils.PropertyUtils;

/**
 * Store information about a property value of an object that is properties must be logged
 * @author Ricardo Memoria
 *
 */
public class PropertyValue {

	private Object entity;
	private PropertyMapping mapping;
	private String name;
	private Object value;

	public PropertyValue(Object entity, PropertyMapping mapping, String name, Object value) {
		super();
		this.entity = entity;
		this.mapping = mapping;
		this.name = name;
		this.value = normalizeValue( value );
	}

	
	/**
	 * Read the value of the entity indicated by the property 
	 * @return
	 */
	public Object getEntityNewValue() {
		try {
			return PropertyUtils.getProperty(entity, name);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Check if property value has changed
	 * @return
	 */
	public boolean isValueChanged() {
		Object newvalue = normalizeValue( getEntityNewValue() );
		if ((newvalue == null) && (value == null))
			return false;

		if ((newvalue == null) || (value == null))
			return true;
		
		return !value.equals(newvalue);
	}

	/**
	 * @return the mapping
	 */
	public PropertyMapping getMapping() {
		return mapping;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}
	
	/**
	 * Check if value is a primitive type and can be directly compared to each other using method equals()
	 * @param obj
	 * @return
	 */
	public static boolean isPrimitiveType(Object obj) {
		Class clazz = obj.getClass();
		return clazz.equals(String.class) || 
			clazz.equals(Boolean.class) || 
        	clazz.equals(Integer.class) ||
        	(obj instanceof Date) ||
        	(obj instanceof Number) ||
        	(obj instanceof Enum) ||
        	clazz.equals(Byte.class) ||
        	clazz.equals(Short.class) ||
        	clazz.equals(Double.class) ||
        	clazz.equals(Long.class) ||
        	clazz.equals(Character.class) ||
        	clazz.equals(Float.class);
	}
	
	
	public static boolean isPrimitiveType(Class clazz) {
		return clazz.equals(String.class) || 
			clazz.equals(Boolean.class) || 
			clazz.equals(Integer.class) ||
			clazz.equals(Date.class) ||
			clazz.isPrimitive() ||
			clazz.equals(Byte.class) ||
			clazz.equals(Short.class) ||
			clazz.equals(Double.class) ||
			clazz.equals(Long.class) ||
			clazz.equals(Character.class) ||
			clazz.equals(Float.class);
	}
	
	
	/**
	 * If the value is not a primitive value, the value is converted to its string representation
	 * @param value
	 * @return
	 */
	protected Object normalizeValue(Object value) {
		if (value == null)
			return null;

		if (isPrimitiveType(value))
			return value;

		return value.toString();
	}
}
