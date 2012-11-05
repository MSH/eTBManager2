package org.msh.datastream;

public class EnumStringConverter implements TypeConverter {

	@Override
	public boolean isSupportedType(Class classType) {
		return Enum.class.isAssignableFrom(classType);
	}

	@Override
	public String toString(Object obj) {
		return obj.toString();
	}

	@SuppressWarnings("hiding")
	@Override
	public <Enum> Enum fromString(String s, Class<Enum> classType) {
		Enum[] vals = ((Class<Enum>)classType).getEnumConstants();
		for (Enum val: vals)
			if (val.toString().equals(s))
				return val;
		
		throw new IllegalArgumentException("Value " + s + " is not valid for " + classType.toString());
	}

}
