package org.msh.tb.entities.enums;


public enum OrderStatus {
	WAITINGAUT,
	AUTHORIZED,
	SHIPPED,
	RECEIVED,
	CANCELLED;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
