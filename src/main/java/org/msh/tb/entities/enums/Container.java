package org.msh.tb.entities.enums;


public enum Container  {
	BOX,
	BOTTLE;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
