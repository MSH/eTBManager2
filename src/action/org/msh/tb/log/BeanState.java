package org.msh.tb.log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.Hibernate;
import org.msh.tb.entities.LogValue;

public class BeanState {

	private boolean newBean;
	private Object bean;
	private Map<String, Object> state = new HashMap<String, Object>();
	private Map<String, FieldLog> fieldsLog = new HashMap<String, FieldLog>();

	public BeanState(Object bean, boolean newBean) {
		super();
		this.bean = bean;
		this.newBean = newBean;
		
		if (!this.newBean)
			addProperties(bean, "");
	}

		
	/**
	 * Generate list of log values
	 * @return {@link List} of {@link LogValue} objects containing differences between previous state and current state
	 */
	public List<LogValue> generateLogValues() {
		List<LogValue> lst = new ArrayList<LogValue>();

		// is a new bean?
		if (newBean) {
			addProperties(bean, "");
			for (String prop: state.keySet()) {
				LogValue log = new LogValue();
				log.setKey(prop);
				log.setNewObjectValue(state.get(prop));
				lst.add(log);
			}
			return lst;
		}

		// get differences
		for (String prop: state.keySet()) {
			Object oldValue = state.get(prop.toString());
			Object newValue = null;

			// recupera novo valor
			try {
				newValue = PropertyUtils.getProperty(bean, prop);
				if ((newValue != null) && (!isPrimitiveType(newValue)))
					newValue = newValue.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}

			// propriedade foi modificada ?
			if (!isValuesEquals(oldValue, newValue)) {
				lst.add(createValue(prop, oldValue, newValue));
			}
		}
		
		return lst;
	}

	
	/**
	 * Compare previous and new values of a log record
	 * @param val1
	 * @param val2
	 * @return
	 */
	public boolean isValuesEquals(Object val1, Object val2) {
		if ((val1 instanceof String) && (val1.toString().isEmpty()))
			val1 = null;
		
		if ((val2 instanceof String) && (val2.toString().isEmpty()))
			val2 = null;
		
		if ((val1 == null) && (val2 == null))
			return true;

		if ((val1 != null) && (val2 != null)) {
			if (val1 instanceof Date)
				 return ((Date)val1).compareTo((Date)val2) == 0;
			else return val1.equals(val2);
		}
		else return false;
	}

	
	/**
	 * Create an object {@link LogValue} to be registered in the logging system
	 * @param key
	 * @param prevValue
	 * @param newValue
	 * @return
	 */
	protected LogValue createValue(String key, Object prevValue, Object newValue) {
		// translate key according to values and context
		FieldLog fieldLog = fieldsLog.get(key);
		if ((fieldLog != null) && (!fieldLog.key().isEmpty())) {
			key = fieldLog.key();
		}
		else {
			// check if it's an enumeration
			Object obj = (prevValue != null ? prevValue : newValue);
			
			if (obj instanceof Enum) {
				key = obj.getClass().getSimpleName(); 
			}
			else {
				// check if key is nested property  
				int pos = key.lastIndexOf('.');
				if (pos >= 0) {
					String prop1 = key.substring(0, pos);
					String prop2 = key.substring(pos, key.length());
					try {
						Object val = PropertyUtils.getProperty(bean, prop1);
						Class clazz = Hibernate.getClass(val);
						key = clazz.getSimpleName() + prop2;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else key = '.' + key;
			}
		}
		
		LogValue val = new LogValue();
		val.setKey(key);
		val.setPrevObjectValue(prevValue);
		val.setNewObjectValue(newValue);
		
		return val;
	}
	
	
	
	/**
	 * Translate the names to the corresponding names in the list of messages
	 * @param key
	 * @return
	 */
/*	protected String translateKey(String key) {
		if (key.equals("name.name1"))
			return "form.name";
		
		if (key.equals("name.name2"))
			return "form.name2";
		
		if (key.equals("abbrevName.name1"))
			return "form.abbrevName";
		
		if (key.equals("abbrevName.name2"))
			return "form.abbrevName2";
		
		return "." + key;
	}
*/
	/**
	 * Return the bean with state mapped
	 * @return
	 */
	public Object getBean() {
		return bean;
	}
	
	
	public void setBean(Object bean) {
		this.bean = bean;
	}

	
	/**
	 * Return the state of the bean mapped
	 * @return
	 */
	public Map<String, Object> getState() {
		return state;
	}
	
/*
	
	*//**
	 * Add extra nested properties to the state
	 * @param obj
	 *//*
	protected void addExtraNestedProperties(Object obj) {
		List<String> props = getExtraNestedProperties();
		if (props == null)
			return;
		
		for (String propName: props) {
			try {
				Object property = PropertyUtils.getNestedProperty(obj, propName);
				state.put(propName, property);
			} catch (Exception e) {
			}			
		}
	}
*/
	
	/**
	 * Return the annotation {@link FieldLog} associated to the field
	 * @param obj
	 * @param propertyName
	 * @return
	 */
	public FieldLog getFieldLog(Object obj, String propertyName) {
		try {
			Field fld = obj.getClass().getDeclaredField(propertyName);						
			return (FieldLog)fld.getAnnotation(FieldLog.class);
		} catch (Exception e) {
			return null;
		}
	}


	private Field findField(Object obj, String fieldName) {
		Class clazz = obj.getClass();
		while (clazz != null) {
			try {
				return clazz.getDeclaredField(fieldName);
			} catch (SecurityException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			} catch (NoSuchFieldException e) {
				clazz = clazz.getSuperclass();
				// is the root class ?
				if (clazz == Object.class)
					break;
			}
		}
		return null;
	}


	/**
	 * Map the properties of the bean to the propstate Map
	 * @param obj
	 * @param propstate
	 * @param propertyName
	 */
	protected void addProperties(Object obj, String propertyName) {
		try {
			Map<String, Object> vals = PropertyUtils.describe(obj);

			// remove default properties
			vals.remove("class");
			vals.remove("id");

			for (String key: vals.keySet()) {
				Field fld = findField(obj, key);
				if (fld != null) {
					Object value = vals.get(key);
					
					// check for specific log instructions
					FieldLog logValAnot = getFieldLog(obj, key);

					// shall logging for this property be ignored
					boolean ignore = false;
					if (logValAnot != null) {
						ignore = logValAnot.ignore();
						fieldsLog.put(key, logValAnot);
					}
					
					if (!ignore) {
						boolean logEntityFields = (logValAnot != null) && (logValAnot.logEntityFields());
						if (logEntityFields)
							 addProperties(value, key + ".");
						else {
							// check if it's not a primitive type, if so, the string representation is returned
							if ((value != null) && (!isPrimitiveType(value)))
								value = value.toString();
							state.put(propertyName + key, value);
						}
					}
				}
			}
			
		} catch (Exception e) {
			System.out.println("Error reading bean state: " + (obj != null? obj.getClass().toString(): null) + " = " + obj);
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Check if value is a primitive type and can be directly compared to each other using method equals()
	 * @param obj
	 * @return
	 */
	public boolean isPrimitiveType(Object obj) {
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
	
	public boolean isNewBean() {
		return newBean;
	}

}
