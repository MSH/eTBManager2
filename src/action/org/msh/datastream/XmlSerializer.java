package org.msh.datastream;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.hibernate.Hibernate;

public class XmlSerializer {

	private SchemaManager schemaManager;
	private List parentList = new ArrayList();
	
	public XmlSerializer(SchemaManager schemaManager) {
		this.schemaManager = schemaManager;
	}
	
	/**
	 * Serialize the entity to an XML object
	 * @param entity
	 * @return
	 */
	public String serializeToXml(Object entity) {
		parentList.clear();
		return serialize(entity).asXML();
	}
	
	/**
	 * Serialize the entity to an XML {@link Element}
	 * @param entity
	 * @return
	 */
	public Element serialize(Object entity) {
		Class entityClass = Hibernate.getClass(entity);
		ClassSchema schema = schemaManager.findSchema(entityClass);

		if (schema == null)
			throw new IllegalArgumentException("No schema found for class " + entityClass.toString());

		Element elem = DocumentHelper.createElement(schema.getAlias());

		// entity was already serialized ?
		if (isObjectAlreadySeriazlized(entity)) {
			Object key = getObjectKey(entity, schema);
			elem.setText( StringConverter.instance().toString(key) );
		}
		else {
			parentList.add(entity);
			mountAttributes(entity, elem, schema);
			parentList.remove(entity);
		}

		return elem;
	}


	/**
	 * @param entity
	 * @param parent
	 * @param fields
	 */
	private void mountAttributes(Object entity, Element parent, ClassSchema schema) {
		List<Field> fields = schema.getFields();

		for (Field fld: fields) {
			if (schema.isFieldSerializable(fld.getName())) {
				Element elem = createFieldElement(entity, schema, fld);
				parent.add(elem);
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
	private Element createFieldElement(Object entity, ClassSchema schema, Field fld) {
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
			handleCollection(elem, (Collection)obj);
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
				elem.addAttribute("type", typeSchema.getAlias());

			// it's a field reference ?
			if (schema.isFieldReference(name)) {
				Object key = getObjectKey(obj, schema);
				elem.setText( StringConverter.instance().toString(key) );
			}
			else mountAttributes(obj, elem, typeSchema);
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
	private void handleCollection(Element parent, Collection lst) {
		for (Object obj: lst) {
			Element elem = serialize(obj);
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
		for (Object item: parentList) {
			if (item == obj)
				return true;
			
			if ((item.getClass().isAssignableFrom(obj.getClass())) && 
				(item.equals(obj))) {
				return true;
			}
		}
		
		return false;
	}
}
