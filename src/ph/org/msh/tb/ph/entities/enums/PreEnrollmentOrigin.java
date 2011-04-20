package org.msh.tb.ph.entities.enums;

public enum PreEnrollmentOrigin {

	DSTRESULT,
	CONSILIUMDATE;

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
