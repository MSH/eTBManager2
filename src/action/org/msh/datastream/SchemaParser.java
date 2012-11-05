package org.msh.datastream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Parse a property file containing information on how to marshal/unmarshal objects of specific classes.
 * The parser also describe all properties that will be stored, considering name, properties to be ignored, etc.
 * @author Ricardo Memoria
 *
 */
public class SchemaParser {

	private ClassSchema classSchema;
	private List<ClassSchema> schemas = new ArrayList<ClassSchema>();
	
	public List<ClassSchema> parse(InputStream in) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		schemas.clear();
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				if (line != null) {
					line = line.trim();
					int pos = line.indexOf('=');
					if ((!line.startsWith(";")) && (pos > 0)) {
						String key = line.substring(0, pos);
						String value = line.substring(pos + 1);
						if ((!key.isEmpty()) && (!value.isEmpty()))
							parseValue(key, value);
						else System.out.println("Invalid key value pair " + key + "..." + value);
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		if (schemas.size() > 0)
			describeClassProperties(schemas.get(schemas.size() - 1));
		
		return schemas;
	}
	
	
	/**
	 * Create the list of properties to be marshaled/unmarshaled in a class schema
	 * @param schema
	 */
	private void describeClassProperties(ClassSchema schema) {
		Class clazz = schema.getObjectClass();

		addFields(clazz);
	}


	/**
	 * Add the fields from a given class that must be marshaled / unmarshaled
	 * @param clazz
	 * @param lst
	 */
	private void addFields(Class clazz) {
		Field[] fields = clazz.getDeclaredFields();
		for (Field field: fields) {
			if (isValidField(field))
				classSchema.getFields().add( field );
		}
	}



	private boolean isValidField(Field field) {
		String name = field.getName();
		
		// field is the class definition
		if (name.equals("class"))
			return false;

		// field is in the ignore list ?
		if ((classSchema.getIgnoreProperties() != null) && (classSchema.getIgnoreProperties().contains(name)))
			return false;
		
		// check if field is readable and writable
		name = Character.toUpperCase( name.charAt(0) ) + name.substring(1);
		
		String getMethod;
		if (field.getType() == boolean.class)
			 getMethod = "is" + name;
		else getMethod = "get" + name;
		String setMethod = "set" + name;
		
		Class clazz = field.getDeclaringClass();

		Class[] param1 = {};
		Class[] param2 = {field.getType()};
		boolean valid;

		try {
			valid = (clazz.getMethod(getMethod, param1) != null) && (clazz.getMethod(setMethod, param2) != null);
		} catch (NoSuchMethodException e) {
			valid = false;
		}
		return valid;
	}


	protected void parseValue(String key, String value) {
		if (key.equals("entity")) {
			if (classSchema != null)
				describeClassProperties(classSchema);
			classSchema = new ClassSchema();
			classSchema.setObjectClassName(value);
			schemas.add(classSchema);
		}

		if (classSchema == null)
			return;
		
		if (key.equals("alias"))
			classSchema.setAlias(value);
		
		if (key.equals("key"))
			classSchema.setKeyProperty(value);
		
		if (key.equals("parent-property"))
			classSchema.setParentProperty(value);
		
		if (key.equals("ignore-list"))
			parseIgnoreList(value);
		
		if (key.equals("entity-ref-list"))
			parseReferenceList(value);
	}
	
	
	/**
	 * Parse ignore list
	 * @param value
	 */
	protected void parseIgnoreList(String value) {
		List<String> lst = parseCommaSeparatedStrings(value);
		
		if (lst.size() > 0)
		 classSchema.setIgnoreProperties(lst);
	}
	
	
	/**
	 * Parse reference list
	 * @param value
	 */
	protected void parseReferenceList(String value) {
		List<String> lst = parseCommaSeparatedStrings(value);
		
		if (lst.size() > 0)
		 classSchema.setReferenceProperties(lst);
	}


	/**
	 * Parse string separated by comma
	 * @param value
	 * @return
	 */
	protected List<String> parseCommaSeparatedStrings(String value) {
		String[] props = value.split(",");
		List<String> lst = new ArrayList<String>();

		for (String prop: props) {
			lst.add(prop);
		}
		return lst;
	}

}
