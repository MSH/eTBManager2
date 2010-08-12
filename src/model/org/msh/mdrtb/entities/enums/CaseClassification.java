package org.msh.mdrtb.entities.enums;

public enum CaseClassification {
	TB_DOCUMENTED,
	MDRTB_DOCUMENTED;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
