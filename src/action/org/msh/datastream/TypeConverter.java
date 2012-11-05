package org.msh.datastream;

public interface TypeConverter {

	boolean isSupportedType(Class classType);
	
	String toString(Object obj);
	
	<E> E fromString(String s, Class<E> classType);
}
