/**
 * 
 */
package org.msh.tb.login;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import java.util.HashMap;
import java.util.Map;

/**
 * Store temporary and very tiny objects into the user session. This component
 * is intended to store just primitive values, and it's an attempt to replace
 * the conversation scope, that store heavy objects into user session
 * 
 * @author Ricardo Memoria
 *
 */
@Name(SessionData.compName)
@Scope(ScopeType.SESSION)
@BypassInterceptors
public class SessionData {

	protected static final String compName = "sessionData";
	
	private Map<Object, Object> values;

	/**
	 * Get a value from the session data
	 * @param key the key where data is stored
	 * @return value corresponding to the give key
	 */
	public Object getValue(Object key) {
		if (values == null)
			return null;
		return values.get(key);
	}
	
	
	/**
	 * Set a value to be stored to a given key
	 * @param key is the key where data is stored
	 * @param value is the value to be stored
	 */
	public void setValue(Object key, Object value) {
		if (values == null) {
			if (value == null)
				return;
			values = new HashMap<Object, Object>();
		}
		values.put(key, value);
	}

	
	/**
	 * Return the singleton instance of this component
	 * @return instance of {@link SessionData}
	 */
	public static SessionData instance() {
		return (SessionData)Component.getInstance(compName);
	}

}
