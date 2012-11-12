package org.msh.datastream;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import org.apache.commons.beanutils.PropertyUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.hibernate.Hibernate;

public class XmlSerializer {

	private SchemaManager schemaManager;

	// list of parents as a queue
	private Stack parents = new Stack();


	public XmlSerializer(SchemaManager schemaManager) {
		this.schemaManager = schemaManager;
	}
	
	/**
	 * Serialize the entity to an XML object
	 * @param entity
	 * @return
	 */
	public String serializeToXml(Object entity) {
		parents.clear();
		return serialize(entity).asXML();
	}
	

	/**
	 * Serialize an object to its XML representation based on its schema defined in the <code>schemaManager</code>
	 * @param object
	 * @return
	 */
	public Element serialize(Object object) {
		return serialize(object, null);
	}
	
	
	/**
	 * Serialize the object to an XML {@link Element}
	 * @param object is the object to be serialized
	 * @param parentProperty is the property of the object that is a reference to the parent object, and, in this case
	 * this property won't be serialized 
	 * @return
	 */
	protected Element serialize(Object object, String parentProperty) {
		Class entityClass = Hibernate.getClass(object);
		ClassSchema schema = schemaManager.findSchema(entityClass);

		if (schema == null)
			throw new IllegalArgumentException("No schema found for class " + entityClass.toString());

		Element elem = DocumentHelper.createElement(schema.getNodeName());

		// entity was already serialized ?
		if (isObjectAlreadySeriazlized(object)) {
			Object key = getObjectKey(object, schema);
			elem.setText( StringConverter.instance().toString(key) );
		}
		else {
			parents.push(object);
			serializeProperties(object, elem, schema, parentProperty);
			parents.remove(object);
		}

		return elem;
	}


	/**
	 * Serialize the attributes of the object
	 * @param entity
	 * @param parent
	 * @param fields
	 */
	private void serializeProperties(Object entity, Element parentElement, ClassSchema schema, String parentProperty) {
		List<Field> fields = schema.getFields();

		for (Field fld: fields) {
			if ((!fld.getName().equals(parentProperty)) && (schema.isFieldSerializable(fld.getName()))) {
				Element elem = serializeProperty(entity, schema, fld);
				parentElement.add(elem);
			}
		}
	}


	/**
	 * Serialize the property to an XML element
	 * @param entity
	 * @param schema
	 * @param fld
	 * @return
	 */
	private Element serializeProperty(Object entity, ClassSchema schema, Field fld) {
		String name = fld.getName();

		Element elem = DocumentHelper.createElement(name);

		// get property value
		Object obj;
		try {
			obj = PropertyUtils.getProperty(entity, name);
		} catch (Exception e) {
			throw new IllegalAccessError("Value of field " + name + " was not found");
		}

		// if property is null, return an empty element
		if (obj == null)
			return elem;
		
		// property is a collection of other objects ?
		if (Collection.class.isAssignableFrom( fld.getType() )) {
			serializeCollection(elem, (Collection)obj, schema.getChildPropertyLink(fld.getName()));
			return elem;
		}

		boolean includeTypeAttr;
		Class clazz = null;
		if (fld.getType() != obj.getClass()) {
			includeTypeAttr = true;
			clazz = obj.getClass();
		}
		else {
			includeTypeAttr = false;
			clazz = fld.getType();
		}

//		ClassSchema typeSchema = schemaManager.findSchema(fld.getType());
		ClassSchema typeSchema = schemaManager.findSchema(clazz);

		// schema was defined ?
		if (typeSchema != null) {
			if (includeTypeAttr)
				elem.addAttribute("type", typeSchema.getNodeName());

			// it's a field reference ?
			if ((schema.isFieldReference(name)) || (isObjectAlreadySeriazlized(obj))) {
				Object key = getObjectKey(obj, schema);
				elem.setText( StringConverter.instance().toString(key) );
			}
			else {
				parents.push(obj);
				serializeProperties(obj, elem, typeSchema, schema.getChildPropertyLink(fld.getName()));
				parents.pop();
			}
		}
		else {
			// if schema was not defined, try to serialize it to string
			String s = StringConverter.instance().toString(obj);
			elem.setText(s);
		}

		return elem;
	}

	/**
	 * Handle a collection of objects
	 * @param lst
	 */
	private void serializeCollection(Element parent, Collection lst, String parentProperty) {
		for (Object obj: lst) {
			Element elem = serialize(obj, parentProperty);
			parent.add(elem);
		}
	}


	/**
	 * Return an entity key value. If the property that holds the key is not defined
	 * in the class schema, an {@link IllegalAccessError} exception will be thrown.
	 * @param entity
	 * @param schema
	 * @return
	 */
	private Object getObjectKey(Object entity, ClassSchema schema) {
		String key = schema.getKeyProperty();
		
		if ((key == null) || (key.isEmpty()))
			throw new IllegalAccessError("Key is not defined to class " + entity.getClass().toString());
		
		try {
			return PropertyUtils.getProperty(entity, key);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	/**
	 * Check if an object was already serialized, in order to avoid circular reference
	 * @param obj
	 * @return
	 */
	protected boolean isObjectAlreadySeriazlized(Object obj) {
		for (Object item: parents) {
			if (item == obj)
				return true;
			
			if ((item.getClass().isAssignableFrom(obj.getClass())) && 
				(item.equals(obj))) {
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * Store object information
	 * @author Ricardo Memoria
	 *
	 */
	protected class ObjectInfo {
		private Object parent;
		private String childProperty;
		private String parentProperty;

		public ObjectInfo(Object parent, String childProperty,
				String parentProperty) {
			super();
			this.parent = parent;
			this.childProperty = childProperty;
			this.parentProperty = parentProperty;
		}
		/**
		 * @return the parent
		 */
		public Object getParent() {
			return parent;
		}
		/**
		 * @return the childProperty
		 */
		public String getChildProperty() {
			return childProperty;
		}
		/**
		 * @return the parentProperty
		 */
		public String getParentProperty() {
			return parentProperty;
		}
	}
}
