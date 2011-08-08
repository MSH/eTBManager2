package org.msh.tb.entities.enums;


public enum OrderStatus {
	WAITINGAUT,
	WAITSHIPMENT,
	SHIPPED,
	RECEIVED,
	CANCELLED,
	PREPARINGSHIPMENT;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
