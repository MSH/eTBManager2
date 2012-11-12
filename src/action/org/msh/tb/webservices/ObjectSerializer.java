package org.msh.tb.webservices;

import java.io.InputStream;

import org.msh.datastream.SchemaManager;
import org.msh.datastream.XmlDeserializer;
import org.msh.datastream.XmlSerializer;
import org.msh.datastream.XmlDeserializer.ObjectReferenceable;

/**
 * Serialize and deserialize objects to and from XML
 * @author Ricardo Memoria
 *
 */
public class ObjectSerializer {

	private static ObjectSerializer _instance;

	// instance of the class schema manager in use
	private SchemaManager schemaManager;
	
	/**
	 * Private constructor of the class
	 */
	private ObjectSerializer() {
		InputStream in = getClass().getClassLoader().getResourceAsStream("\\WEB-INF\\classes\\org\\msh\\tb\\webservices\\objectstream.ini");
		if (in == null)
			throw new IllegalAccessError("No stream found containing object mapping");
		schemaManager = new SchemaManager(in); 
		_instance = this;
	}
	
	/**
	 * Initialize instance of {@link ObjectSerializer} class, running inside a synchronized thread
	 */
	protected static synchronized void createInstance() {
		if (_instance == null)
			_instance = new ObjectSerializer();
	}
	
	/**
	 * Return singleton instance of {@link ObjectSerializer} class
	 * @return
	 */
	public static ObjectSerializer instance() {
		if (_instance == null) {
			createInstance();
		}
		return _instance;
	}
	
	/**
	 * Serialize an object to an XML representation
	 * @param obj
	 * @return
	 */
	public static String serializeToXml(Object obj) {
		XmlSerializer ser = new XmlSerializer(instance().getSchemaManager());
		return ser.serializeToXml(obj);
	}
	
	
	/**
	 * @param xml
	 * @param clazz
	 * @return
	 */
	public static <E> E deserializeFromXml(String xml, Class<E> clazz, ObjectReferenceable ref) {
		XmlDeserializer deser = new XmlDeserializer(instance().getSchemaManager());
		return (E)deser.deserialize(xml, ref);
	}

	/**
	 * @return the schemaManager
	 */
	public SchemaManager getSchemaManager() {
		return schemaManager;
	}
}
