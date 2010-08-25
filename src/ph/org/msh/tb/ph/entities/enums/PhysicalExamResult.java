package org.msh.tb.ph.entities.enums;

public enum PhysicalExamResult {
	NOT_DONE,
	NORMAL,
	ABNORMAL;

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}

}
