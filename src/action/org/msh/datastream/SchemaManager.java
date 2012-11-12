package org.msh.datastream;

import java.io.InputStream;
import java.util.List;

public class SchemaManager {

	private List<ClassSchema> schemas;
	
	public SchemaManager(InputStream in) {
		SchemaParser parser = new SchemaParser();
		schemas = parser.parse(in);
	}
	

	/**
	 * Return the schemas of the classes
	 * @return the schemas
	 */
	public List<ClassSchema> getSchemas() {
		return schemas;
	}

	
	/**
	 * Find a schema by its class
	 * @param clazz
	 * @return
	 */
	public ClassSchema findSchema(Class clazz) {
		for (ClassSchema sc: schemas)
			if (sc.getObjectClass() == clazz)
				return sc;
		return null;
	}
	
	
	/**
	 * Search for a class schema by its node name, i.e, the name of the node representing the class
	 * in the serialized document
	 * @param nome
	 * @return an instance of {@link ClassSchema}, or null if it was not found
	 */
	public ClassSchema findSchemaByNode(String node) {
		for (ClassSchema sc: schemas)
			if (sc.getNodeName().equals(node))
				return sc;
		return null;
	}
}
