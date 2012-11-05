package org.msh.datastream;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Serializer to most of the common java primitive types, including date and string
 * @author Ricardo Memoria
 *
 */
public class PrimitiveStringConverter implements TypeConverter {

	private static final SimpleDateFormat dtformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
	
	/* (non-Javadoc)
	 * @see org.msh.datastream.TypeSerializer#isSupportedType(java.lang.Class)
	 */
	@Override
	public boolean isSupportedType(Class classType) {
		return (classType == int.class) || 
				(classType == Integer.class) ||
				(classType == long.class) ||
				(classType == Long.class) ||
				(classType == char.class) ||
				(classType == Character.class) ||
				(classType == String.class) ||
				(classType == boolean.class) ||
				(classType == Boolean.class) ||
				(Date.class.isAssignableFrom(classType));
	}


	/* (non-Javadoc)
	 * @see org.msh.datastream.TypeSerializer#serialize(java.lang.Object)
	 */
	@Override
	public String toString(Object obj) {
		if (obj == null)
			return "";

		Class classType = obj.getClass();

		// check if it's a string
		if (classType == String.class)
			return (String)obj;
		
		// check if it's an integer type
		if ((classType == int.class) || (classType == Integer.class))
			return Integer.toString((Integer)obj);

		// check if it's a long type
		if ((classType == long.class) || (classType == Long.class))
			return Long.toString((Long)obj);

		// check if it's a character type
		if ((classType == char.class) || (classType == Character.class))
			return ((Character)obj).toString();

		// check if it's a boolean type
		if ((classType == boolean.class) || (classType == Boolean.class))
			return (Boolean)obj ? "1": "0";

		// check if it's a date-time type
		if (Date.class.isAssignableFrom(classType))
			return serializeDate((Date)obj);
		
		throw new IllegalArgumentException("Class " + classType.toString() + " not supported for serialization");
	}

	/**
	 * Serialize a {@link Date} object to a string
	 * @param obj
	 * @return
	 */
	private String serializeDate(Date obj) {
		Date dt = (Date)obj;
		return dtformat.format(dt);
	}

	/* (non-Javadoc)
	 * @see org.msh.datastream.TypeSerializer#deserialize(java.lang.String, java.lang.Class)
	 */
	@Override
	public Object fromString(String s, Class classType) {
		if (s == null)
			return null;

		// check if it's a string
		if (classType == String.class)
			return s;
		
		// check if it's an integer type
		if ((classType == int.class) || (classType == Integer.class))
			return Integer.parseInt(s);

		// check if it's a long type
		if ((classType == long.class) || (classType == Long.class))
			return Long.parseLong(s);

		// check if it's a character type
		if ((classType == char.class) || (classType == Character.class)) {
			if (s.length() > 0)
				return s.charAt(0);
			raiseConvertionError(s, classType);
		}

		// check if it's a boolean type
		if ((classType == boolean.class) || (classType == Boolean.class)) {
			if ((s.equals("1")) || (s.equalsIgnoreCase("true")))
				return true;
			if ((s.equals("0")) || (s.equalsIgnoreCase("false")))
				return false;
			raiseConvertionError(s, classType);
		}

		// check if it's a date-time type
		if (Date.class.isAssignableFrom(classType))
			return deserializeDate(s);
		return null;
	}

	/**
	 * Deserialize a {@link Date} object from a string 
	 * @param s
	 * @return
	 */
	private Object deserializeDate(String s) {
		try {
			return dtformat.parseObject(s);
		} catch (ParseException e) {
			raiseConvertionError(s, Date.class);
		}
		return null;
	}


	/**
	 * Throw default conversion error exception
	 * @param s
	 * @param classType
	 */
	protected void raiseConvertionError(String s, Class classType) {
		throw new IllegalArgumentException("Value " + s + " cannot be converted to " + classType.toString());
	}
}
