package org.msh.tb.entities.enums;

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
	OTHER,
	
	//Nigeria Workspace
	NIGERIA;
	
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}

}
