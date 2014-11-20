package org.msh.tb.ng.entities.enums;

public enum SuspectType {
SUSPECT_TYPE1;

public String getKey() {
	return getClass().getSimpleName().concat("." + name());
}
}
