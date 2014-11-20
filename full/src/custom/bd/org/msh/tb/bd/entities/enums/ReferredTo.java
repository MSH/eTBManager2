package org.msh.tb.bd.entities.enums;

public enum ReferredTo {
	PP,
	GFS,
	NON_PP,
	SS,
	VD, 
	CV;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
