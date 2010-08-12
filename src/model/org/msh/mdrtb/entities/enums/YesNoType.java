package org.msh.mdrtb.entities.enums;

public enum YesNoType {
	YES ("global.yes"),
	NO ("global.no");
	
	private final String messageKey;

	YesNoType(String msg) {
		messageKey = msg;
	}
	
	public String getKey() {
		return messageKey;
	}
}
