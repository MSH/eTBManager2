package org.msh.datastream;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic singleton serializer and deserialize of basic types like int, boolean, char and 
 * other primitive types to string type.
 * Custom serializers can be included by implementing the {@link TypeConverter} interface
 * and registering it calling the method <code>addSerializer</code>.
 * @author Ricardo Memoria
 *
 */
public class StringConverter {

	private static final StringConverter serializer = new StringConverter();
	
	private List<TypeConverter> serializers = new ArrayList<TypeConverter>();
	
	/**
	 * Protected constructor to make it singleton
	 */
	protected StringConverter() {
		addConverter(new PrimitiveStringConverter());
		addConverter(new EnumStringConverter());
	}
	
	
	/**
	 * Add a custom serializer that implements the interface {@link TypeConverter}.
	 * The serializer for the given type will be automatically available when the method serialize is called.
	 * @param dataSer
	 */
	public void addConverter(TypeConverter dataSer) {
		serializers.add(dataSer);
	}
	
	
	/**
	 * Serialize an object to its string representation
	 * @param obj
	 * @return
	 */
	public String toString(Object obj) {
		if (obj == null)
			return "";
		
		TypeConverter ser = findConverter(obj.getClass());
		if (ser == null)
			throw new IllegalArgumentException("Unable to convert object of type " + obj.getClass().toString() + " to string. No TypeSerializer found");

		return ser.toString(obj);
	}
	
	
	/**
	 * Convert a string to an object of a given type 
	 * @param data
	 * @param type
	 * @return
	 */
	public <E> E fromString(String data, Class<E> type) {
		if ((data == null) || (data.isEmpty()))
			return null;
		
		TypeConverter ser = findConverter(type);
		if (ser == null)
			throw new IllegalArgumentException("Unable to convert object of type " + type.toString() + ". No TypeSerializer found");

		return ser.fromString(data, type);
	}


	/**
	 * Search for a serializer for a given type. 
	 * @param clazz
	 * @return
	 */
	public TypeConverter findConverter(Class clazz) {
		for (TypeConverter ser: serializers)
			if (ser.isSupportedType(clazz))
				return ser;
		return null;
	}
	
	public static StringConverter instance() {
		return serializer;
	}
}
