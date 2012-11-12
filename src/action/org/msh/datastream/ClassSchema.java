package org.msh.datastream;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Store information about how to marshal the structure of the classes, like 
 * fields to be ignored, properties to get reference, etc.
 * @author Ricardo Memoria
 *
 */
public class ClassSchema {

	private String nodeName;
	private String objectClassName;
	private String keyProperty;
	private List<String> ignoreProperties;
	private List<String> referenceProperties;
//	private String parentProperty;
	private List<Field> fields = new ArrayList<Field>();
	private Map<String, String> objectLinks;


	/**
	 * Check if a field must be serialized
	 * @param name
	 * @return
	 */
	public boolean isFieldSerializable(String name) {
/*		if (name.equals(parentProperty))
			return false;
		
*/		if ((ignoreProperties != null) && (ignoreProperties.contains(name)))
			return false;
		
		return true;
	}

	
	/**
	 * Define a link between a property childField of an object refereed in the property propertyName. The property childField 
	 * will be set to the instance of the class schema when reconstructed, and ignored when serialized 
	 * @param field
	 * @param childField
	 */
	public void addObjectLink(String propertyName, String childField) {
		if (objectLinks == null)
			objectLinks = new HashMap<String, String>();
		
		objectLinks.put(propertyName, childField);
	}

	
	/**
	 * Return the child field of the object in the property <code>propertyName</code> that links to this class  
	 * @param propertyName
	 * @return
	 */
	public String getChildPropertyLink(String propertyName) {
		return objectLinks == null? null : objectLinks.get(propertyName);
	}

	/**
	 * Indicate if field must be represented just by its reference
	 * @param name
	 * @return
	 */
	public boolean isFieldReference(String name) {
		if (referenceProperties == null)
			return false;
		
		return referenceProperties.contains(name);
	}


	/**
	 * Check if key property is informed
	 * @return
	 */
	public boolean isKeyDefined() {
		return (keyProperty != null) && (!keyProperty.isEmpty());
	}

	
	/**
	 * Create a new instance of the object class
	 * @return
	 */
	public Object createObjectInstance() {
		try {
			return getObjectClass().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Return information about a field by its name
	 * @param name
	 * @return
	 */
	public Field fieldByName(String name) {
		for (Field field: fields)
			if (field.getName().equals(name))
				return field;
		return null;
	}

	
	/**
	 * @return the objectClassName
	 */
	public String getObjectClassName() {
		return objectClassName;
	}

	/**
	 * @param objectClassName the objectClassName to set
	 */
	public void setObjectClassName(String objectClassName) {
		this.objectClassName = objectClassName;
	}

	/**
	 * @return the objectClass
	 */
	public Class getObjectClass() {
		try {
			return Class.forName(objectClassName);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @return the ignoreProperties
	 */
	public List<String> getIgnoreProperties() {
		return ignoreProperties;
	}
	/**
	 * @param ignoreProperties the ignoreProperties to set
	 */
	public void setIgnoreProperties(List<String> ignoreProperties) {
		this.ignoreProperties = ignoreProperties;
	}
	/**
	 * @return the referenceProperties
	 */
	public List<String> getReferenceProperties() {
		return referenceProperties;
	}
	/**
	 * @param referenceProperties the referenceProperties to set
	 */
	public void setReferenceProperties(List<String> referenceProperties) {
		this.referenceProperties = referenceProperties;
	}
	/**
	 * @return the parentMapping
	 */
	/**
	 * @return the keyProperty
	 */
	public String getKeyProperty() {
		return keyProperty;
	}
	/**
	 * @param keyProperty the keyProperty to set
	 */
	public void setKeyProperty(String keyProperty) {
		this.keyProperty = keyProperty;
	}
	/**
	 * @return the fields
	 */
	public List<Field> getFields() {
		return fields;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ClassSchema [node=" + nodeName + ", objectClassName="
				+ objectClassName + "]";
	}


	/**
	 * @return the nodeName
	 */
	public String getNodeName() {
		return nodeName;
	}


	/**
	 * @param nodeName the nodeName to set
	 */
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
}
