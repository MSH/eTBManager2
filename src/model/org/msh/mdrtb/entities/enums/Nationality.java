package org.msh.mdrtb.entities.enums;

public enum Nationality {

	NATIVE,
	FOREIGN,
	
	//Kenya Workspace
	KENYA,
	BURUNDI,
	ETHIOPIA,
	RWANDA,
	SOMALIA,
	SUDAN,
	TANZANIA,
	UGANDA,
	OTHER;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}

}
