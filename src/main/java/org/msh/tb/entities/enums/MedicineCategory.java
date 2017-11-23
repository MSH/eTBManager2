package org.msh.tb.entities.enums;

public enum MedicineCategory {
	INJECTABLE,
	ORAL;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
