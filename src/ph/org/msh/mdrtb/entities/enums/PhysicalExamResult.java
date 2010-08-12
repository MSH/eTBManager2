package org.msh.mdrtb.entities.enums;

public enum PhysicalExamResult {
	NOT_DONE,
	NORMAL,
	ABNORMAL;

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}

}
