package org.msh.tb.br;

import static org.jboss.seam.ScopeType.APPLICATION;

import java.io.Serializable;
import java.util.Date;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;

@Name("controller")
@Scope(APPLICATION)
@AutoCreate
@Startup
public class ScheduleController implements Serializable {

	private static final long serialVersionUID = 7609983147081676186L;
	
	@In
	ScheduleProcessor processor;
		
	private static String MIDNIGHT = "0 0 0/24 * * ?";
	private static Date DEFAULT_INI_DATE = new Date();
	
	@Create
	public void scheduleTimer() {
//		processor.updateTagsTrigger(DEFAULT_INI_DATE, MIDNIGHT);
	}
}
