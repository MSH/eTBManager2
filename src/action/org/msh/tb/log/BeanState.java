package org.msh.tb.log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Embeddable;

import org.apache.commons.beanutils.PropertyUtils;
import org.msh.mdrtb.entities.LogValue;

public class BeanState {

	private boolean newBean;
	private Object bean;
	private Map<String, Object> state = new HashMap<String, Object>();
	private List<String> extraNestedProperties;

	public BeanState(Object bean, boolean newBean, List<String> extraNestedProperties) {
		super();
		this.bean = bean;
		this.newBean = newBean;
		this.extraNestedProperties = extraNestedProperties;
		
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
			} catch (Exception e) {
				e.printStackTrace();
			}

			// compara valores anterior e atual da propriedade
			boolean bChanged;
			if ((oldValue != null) && (newValue != null)) {
				if (oldValue instanceof Date) {
					bChanged = ((Date)oldValue).compareTo((Date)newValue) != 0;
				}
				else bChanged = !oldValue.toString().equals(newValue.toString());
			}
			else bChanged = oldValue != newValue;
			
			// propriedade foi modificada ?
			if (bChanged) {
				lst.add(createValue(prop, oldValue, newValue));
			}
		}
		
		return lst;
	}

	
	protected LogValue createValue(String key, Object prevValue, Object newValue) {
		LogValue val = new LogValue();
		
		Object obj = (prevValue != null? prevValue: newValue);

		if ((obj instanceof String) || (obj instanceof Number) || 
			(obj instanceof Boolean) ||	(obj instanceof Date)) {
			key = translateKey(key);
		}
		else
		if (obj instanceof Enum)
			key = obj.getClass().getSimpleName();

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
	protected String translateKey(String key) {
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
	

	
	/**
	 * Add extra nested properties to the state
	 * @param obj
	 */
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

	
	/**
	 * Map the properties of the bean to the propstate Map
	 * @param obj
	 * @param propstate
	 * @param propertyName
	 */
	protected void addProperties(Object obj, String propertyName) {
		if (propertyName.isEmpty())
			addExtraNestedProperties(obj);
		
		try {
			Map<String, Object> vals = PropertyUtils.describe(obj);

			// remove default properties
			vals.remove("class");
			vals.remove("id");

			for (String key: vals.keySet()) {
				if (PropertyUtils.isWriteable(obj, key)) {
					Object value = vals.get(key);
					
					// check for specific log instructions
					org.msh.tb.log.LogValue logValAnot;
					try {
						Field fld = obj.getClass().getDeclaredField(key);						
						logValAnot = (org.msh.tb.log.LogValue)fld.getAnnotation(org.msh.tb.log.LogValue.class);
					} catch (Exception e) {
						logValAnot = null;
					}

					// should log for this property be ignored
					boolean ignore = false;
					if (logValAnot != null)
						ignore = logValAnot.ignore();
					
					Class cl = PropertyUtils.getPropertyType(obj, key);
					
					if (!ignore) {
						if (cl.getAnnotation(Embeddable.class) != null) {
							if (value != null)
								addProperties(value, key + ".");
						}
						else state.put(propertyName + key, value);						
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public boolean isNewBean() {
		return newBean;
	}



	/**
	 * @return the extraNestedProperties
	 */
	public List<String> getExtraNestedProperties() {
		return extraNestedProperties;
	}


	/**
	 * @param extraNestedProperties the extraNestedProperties to set
	 */
	public void setExtraNestedProperties(List<String> extraNestedProperties) {
		this.extraNestedProperties = extraNestedProperties;
	}

}
