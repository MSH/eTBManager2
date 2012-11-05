package org.msh.datastream;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class XmlDeserializer {

	private SchemaManager schemaManager;
	
	public XmlDeserializer(SchemaManager schemaManager) {
		this.schemaManager = schemaManager;
	}
	
	public Object deserialize(String xml) {
		Document doc;
		try {
			doc = DocumentHelper.parseText(xml);
		} catch (DocumentException e) {
			throw new IllegalArgumentException(e);
		}
		Element elem = doc.getRootElement();

		ClassSchema schema = schemaManager.findSchemaByAlias(elem.getName());
		
		if (schema == null)
			throw new IllegalArgumentException("No class found for node " + elem.getName());

		return readObject(schema, elem);
	}

	/**
	 * Deserialize object by its content in XML format
	 * @param schema
	 * @param elem
	 * @return
	 */
	private Object readObject(ClassSchema schema, Element elem) {
		List<Element> children = elem.elements();

		// retrieve key property
		String keyProp = schema.isKeyDefined()? schema.getKeyProperty(): null;
		
		Object entity = null;

		// key was set to object ?
		if (keyProp != null) {
			Object keyValue = null;
			// entity properties were defined ?
			if (children.size() > 0) {
				Element elemKey = elem.element(keyProp);
				if (elemKey != null)
					keyValue = convertFieldValue(schema, keyProp, elemKey.getText());
			}
			else {
				keyValue = convertFieldValue(schema, keyProp, elem.getText());
			}
			
			if (keyValue != null)
				entity = recoverEntityByKey(schema, keyValue);
		}

		if (entity == null)
			entity = schema.createObjectInstance();

		// read entity attributes
		for (Element elfield: children) {
			String fname = elfield.getName();
			Field field = schema.fieldByName(fname);
			if (field == null)
				throw new IllegalArgumentException("Field " + fname + " is invalid");

			// avoid reading the key property again
			if ((keyProp == null) || ((keyProp != null) && (!keyProp.equals(fname))))
					readField(schema, entity, field, elfield);
		}
		
		return entity;
	}


	/**
	 * Read the content of a field of a given entity
	 * @param entity
	 * @param field
	 * @param elem
	 */
	private void readField(ClassSchema schema, Object entity, Field field, Element elem) {
		Class type = field.getType();
		String name = field.getName();

		if (Collection.class.isAssignableFrom( type )) {
			readCollection(entity, field, elem);
			return;
		}

		ClassSchema fieldSchema;
		String typeAlias = elem.attributeValue("type");
		if (typeAlias != null)
			 fieldSchema = schemaManager.findSchemaByAlias(typeAlias);
		else fieldSchema = schemaManager.findSchema(field.getType());

		if (fieldSchema != null) {
			Object val = readObject(fieldSchema, elem);
			setValue(entity, name, val);
			
			if (fieldSchema.getParentProperty() != null) {
				setValue(val, fieldSchema.getParentProperty(), entity);
			}
			
			return;
		}
		
		Object val = StringConverter.instance().fromString(elem.getText(), field.getType());
		setValue(entity, name, val);
	}

	/**
	 * Read a collection field
	 * @param entity
	 * @param field
	 * @param elem
	 */
	private void readCollection(Object entity, Field field, Element elem)  {
		Collection lst = (Collection)getValue(entity, field.getName());

		// collection is null ?
		if (lst == null) {
			// is a concrete class ?
			if (!field.getType().isInterface()) {
				// create an instance of this class
				try {
					field.getType().newInstance();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			else {
				// create a standard list
				if (List.class.isAssignableFrom(field.getType()))
					lst = new ArrayList();
				else
				if (Set.class.isAssignableFrom(field.getType()))
					lst = new HashSet();
			}
		}
		
		if (lst == null)
			throw new IllegalAccessError("It was not possible to create the collection in use in " + field.toString());
		
		lst.clear();

		// browse the children
		for (Element childElem: (List<Element>)elem.elements()) {
			ClassSchema schema = schemaManager.findSchemaByAlias(childElem.getName());
			
			Object child = readObject(schema, childElem);
			
			if (schema.getParentProperty() != null) {
				setValue(child, schema.getParentProperty(), entity);
			}
			lst.add( child );
		}
	}
	
	
	/**
	 * Return an object property value
	 * @param obj
	 * @param fieldName
	 * @return
	 */
	protected Object getValue(Object obj, String fieldName) {
		try {
			return PropertyUtils.getProperty(obj, fieldName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Set an object value
	 * @param obj
	 * @param fieldName
	 * @param value
	 */
	protected void setValue(Object obj, String fieldName, Object value) {
		try {
			PropertyUtils.setProperty(obj, fieldName, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Return an instance of the entity by its key value
	 * @param schema
	 * @param keyValue
	 * @return
	 */
	private Object recoverEntityByKey(ClassSchema schema, Object keyValue) {
		Object entity = null;

		try {
			// create new instance
			entity = schema.createObjectInstance();
			
			// set property key
			PropertyUtils.setProperty(entity, schema.getKeyProperty(), keyValue);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return entity;
	}

	
	protected void setFieldValue(Object entity, ClassSchema schema, String fieldname, Object value) {
		Field fld = schema.fieldByName(fieldname);
		
		if (fld == null)
			throw new IllegalArgumentException("Field " + fieldname + " was not found");
		
		try {
			// set property key
			PropertyUtils.setProperty(entity, schema.getKeyProperty(), value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	

	/**
	 * Convert a field value from string to its type
	 * @param schema
	 * @param propName
	 * @param value
	 * @return
	 */
	private Object convertFieldValue(ClassSchema schema, String propName, String value) {
		Field field = schema.fieldByName(propName);
		if (field == null)
			throw new IllegalAccessError("Property " + propName + " not found in schema " + schema.getObjectClassName());
		
		return StringConverter.instance().fromString(value, field.getType());
	}

	/**
	 * @return the schemaManager
	 */
	public SchemaManager getSchemaManager() {
		return schemaManager;
	}
}
