package org.msh.mdrtb.entities.enums;


public enum Container  {
	BOX,
	BOTTLE;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
