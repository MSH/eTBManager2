package org.msh.tb.na.entities.enums;


public enum DotOptions {
	HF_DOTS,
	GB_DOTS, 
	COMM_HEALTH_WORKER_DOTS,
	SELF_ADMIN,
	WORKPLACE;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}

