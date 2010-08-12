package org.msh.mdrtb.entities.enums;

public enum TransferStatus {
	WAITING_RECEIVING,
	DONE,
	CANCELLED;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}

}
